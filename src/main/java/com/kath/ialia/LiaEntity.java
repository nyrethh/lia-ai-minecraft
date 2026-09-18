package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class LiaEntity extends PathfinderMob {

    private final LiaMemory memory = new LiaMemory();

    private final LiaQTable qTable = new LiaQTable();

    private final LiaLearning learning = new LiaLearning(qTable);

    private LiaState previousState = null;

    private LiaDecision.Action previousAction = null;

    private double previousMonsterDistance = -1;

    private boolean tookDamage = false;

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
                new RandomStrollGoal(this, 0.5)
        ); // movimiento 0.5 es lo normal.
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.tickCount % 10 == 0) {

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

            // Obtener el estado actual de LIA.
            LiaState state =
                    LiaPerception.getCurrentState(this, 8);

            // Obtener la distancia al monstruo más cercano.
            double monsterDistance =
                    LiaPerception.getNearestMonsterDistance(this, 8);

            Ia_lia.LOGGER.info(
                    "LIA | Distancia al monstruo más cercano: {}",
                    monsterDistance
            );

            /*
             * LIA aprende de la experiencia anterior.
             */
            if (previousState != null && previousAction != null) {

                int reward = LiaReward.calculate(
                        previousState,
                        previousAction,
                        previousMonsterDistance,
                        monsterDistance,
                        tookDamage
                );

                learning.learn(
                        previousState.toKey(),
                        previousAction,
                        reward,
                        state.toKey()
                );
            }

            /*
             * El daño ya fue utilizado para calcular
             * la recompensa de esta experiencia.
             *
             * Ahora lo reiniciamos para la siguiente.
             */
            tookDamage = false;

            /*
             * LIA decide qué hacer según lo aprendido.
             */
            LiaDecision.Action action =
                    learning.chooseAction(state.toKey());

            Ia_lia.LOGGER.info(
                    "LIA decidio: {}",
                    action
            );

            /*
             * LIA ejecuta la decisión.
             */
            LiaAction.execute(this, action);

            // Guardamos la experiencia actual.
            previousState = state;
            previousAction = action;
            previousMonsterDistance = monsterDistance;
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
                "LIA recibió daño: {}",
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
}