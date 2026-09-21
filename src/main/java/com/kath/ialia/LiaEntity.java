package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster; //clase monster de mc

public class LiaEntity extends PathfinderMob {

    private final LiaMemory memory = new LiaMemory();
    private final LiaKnowledge knowledge = new LiaKnowledge();

    private final LiaQTable qTable = new LiaQTable();

    private final LiaLearning learning = new LiaLearning(qTable);

    private LiaState previousState = null;

    private LiaDecision.Action previousAction = null;

    private double previousMonsterDistance = -1;
    private String previousEntity = "none";
    private double previousDistance = -1;


    private boolean tookDamage = false;
    private double previousPlayerDistance = -1;

    public LiaEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level world
    ) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();


        this.goalSelector.addGoal(
                2,
                new RandomStrollGoal(this, 0.5) /*movimiento*/
        );
    }

    /* sirve para buscar monstruos cercanos y hace que lia sea su objetivo*/
    private void makeNearbyMonstersTargetLia() {

        /* busca todas las entidades que esten a un maximo de 8bloques*/
        var monsters = this.level().getEntities(
                this,
                this.getBoundingBox().inflate(8),
                entity -> entity instanceof Monster
        );


        for (var entity : monsters) {

            //convertir la entidad encontrada en monster. o sea,malo
            Monster monster = (Monster) entity;

            //si el malo no tiene un objetivo entonces se hace que LIA si lo sea.
            if (monster.getTarget() == null) {
                monster.setTarget(this);
            }
        }
    }


    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.tickCount % 10 == 0) {


            makeNearbyMonstersTargetLia();

            var entidades = LiaPerception.getNearbyEntities(this, 8);

            if (entidades.isEmpty()) {

                Ia_lia.LOGGER.info(
                        "LIA: No veo nada cerca."
                );

            } else {

                for (Entity entidad : entidades) {

                    String tipo =
                            LiaPerception.identifyEntity(entidad);

                    Ia_lia.LOGGER.info(
                            "LIA: He detectado un {}.",
                            tipo
                    );

                    memory.remember(entidad);
                }
            }

            // Obtiene el estado actual de LIA.
            LiaState state =
                    LiaPerception.getCurrentState(
                            this,
                            8,
                            tookDamage
                    );

            double monsterDistance =
                    LiaPerception.getNearestMonsterDistance(this, 8);

            double playerDistance =
                    LiaPerception.getNearestPlayerDistance(this, 8);



            Ia_lia.LOGGER.info(
                    "LIA | Distancia al monstruo mas cercano: {}",
                    monsterDistance
            );

            Ia_lia.LOGGER.info(
                    "LIA | Distancia al jugador mas cercano: {}",
                    playerDistance
            );



            LiaWorldObservation observation =
                    LiaPerception.observeWorld(this, 4);

            Ia_lia.LOGGER.info(
                    "LIA observa bloques: {}",
                    observation.getBlocks()
            );

            Ia_lia.LOGGER.info(
                    "LIA observa entidades: {}",
                    observation.getEntities()
            );

// LIA aprende los bloques que descubre.
            for (String block : observation.getBlocks()) {
                knowledge.learnBlock(block);
            }

// LIA aprende las entidades que descubre.
            for (String entity : observation.getEntities()) {
                knowledge.learnEntity(entity);
            }





            /*LIA aprende de la experiencia anterior*/
            if (previousState != null && previousAction != null) {

                int reward = LiaReward.calculate(
                        previousState,
                        previousAction,
                        previousMonsterDistance,
                        monsterDistance,
                        previousPlayerDistance,
                        playerDistance,
                        tookDamage
                );

                learning.learn(
                        previousState.toKey(),
                        previousAction,
                        reward,
                        state.toKey()
                );

                // Guarda la experiencia que acaba de vivir LIA.
                // Guarda la experiencia que acaba de vivir LIA.
   /* double distance = -1;

                if (monsterDistance >= 0) {
                    distance = monsterDistance;

                } else if (playerDistance >= 0) {
                    distance = playerDistance;
                }*/

                knowledge.rememberExperience(
                        previousEntity,
                        previousAction,
                        tookDamage,
                        previousDistance,
                        reward
                );
            }

            /* la recompensa de esta experiencia*/
            tookDamage = false;

            /* LIA recibe el estado completo */
            LiaDecision.Action action =
                    learning.chooseAction(state);

            Ia_lia.LOGGER.info(
                    "LIA decidio: {}",
                    action
            );


            String currentEntity = "none";
            double currentDistance = -1;

            var nearbyEntities =
                    LiaPerception.getNearbyEntities(this, 8);

            if (action == LiaDecision.Action.FLEE_MONSTER) {

                var nearestMonster = nearbyEntities.stream()
                        .filter(entity -> entity instanceof Monster)
                        .min((entity1, entity2) ->
                                Double.compare(
                                        this.distanceTo(entity1),
                                        this.distanceTo(entity2)
                                ))
                        .orElse(null);

                if (nearestMonster != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(nearestMonster);

                    currentDistance =
                            this.distanceTo(nearestMonster);
                }

            } else if (action == LiaDecision.Action.APPROACH_PLAYER) {

                var nearestPlayer = nearbyEntities.stream()
                        .filter(entity -> entity instanceof net.minecraft.world.entity.player.Player)
                        .min((entity1, entity2) ->
                                Double.compare(
                                        this.distanceTo(entity1),
                                        this.distanceTo(entity2)
                                ))
                        .orElse(null);

                if (nearestPlayer != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(nearestPlayer);

                    currentDistance =
                            this.distanceTo(nearestPlayer);
                }

            } else {

                var nearestEntity = nearbyEntities.stream()
                        .min((entity1, entity2) ->
                                Double.compare(
                                        this.distanceTo(entity1),
                                        this.distanceTo(entity2)
                                ))
                        .orElse(null);

                if (nearestEntity != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(nearestEntity);

                    currentDistance =
                            this.distanceTo(nearestEntity);
                }
            }


            /* LIA ejecuta lo que decidio. */
            LiaAction.execute(this, action);

            // Guarda la experiencia actual.
            previousState = state;
            previousAction = action;
            previousMonsterDistance = monsterDistance;
            previousPlayerDistance = playerDistance;
            previousEntity = currentEntity;
            previousDistance = currentDistance;
        }
    }

    @Override
    public boolean hurtServer(
            ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            float amount
    ) {

        tookDamage = true;

        Ia_lia.LOGGER.info(
                "LIA recibio un golpe : {}",
                amount
        );

        return super.hurtServer(
                level,
                source,
                amount
        );
    }

    public LiaMemory getMemory() {

        return memory;
    }



    public LiaKnowledge getKnowledge()
    {
        return knowledge;
    }
}


