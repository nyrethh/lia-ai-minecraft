package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;



import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class LiaEntity extends PathfinderMob
        implements InventoryCarrier {

    private final LiaMemory memory = new LiaMemory();
    private final LiaKnowledge knowledge = new LiaKnowledge();

    private final LiaQTable qTable = new LiaQTable();

    private final LiaLearning learning =
            new LiaLearning(qTable);

    private final LiaItemKnowledge itemKnowledge =
            new LiaItemKnowledge();

    private LiaState previousState = null;

    private LiaDecision.Action previousAction = null;


    private double previousMonsterDistance = -1;
    private double previousItemDistance = -1;
    private String previousEntity = "none";
    private double previousDistance = -1;

    private boolean previousPlayerJumping = false;

    private int pendingActionReward = 0;
    private boolean attackSuccessful = false;
    private boolean monsterKilled = false;






    private boolean tookDamage = false;
    private boolean blockedAttack = false;
    private double previousPlayerDistance = -1;
    private int shieldBlockTicks = 0;

    private static final int INVENTORY_SIZE = 27;

    private final SimpleContainer inventory =
            new SimpleContainer(INVENTORY_SIZE);
    public LiaEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level world
    ) {
        super(entityType, world);
    }






    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }


    @Override
    protected void addAdditionalSaveData(
            ValueOutput output
    ) {

        super.addAdditionalSaveData(output);

        writeInventoryToTag(output);
    }


    @Override
    protected void readAdditionalSaveData(
            ValueInput input
    ) {

        super.readAdditionalSaveData(input);

        readInventoryFromTag(input);
    }




    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(
                2,
                new RandomStrollGoal(this, 0.5)
        );
    }



    @Override
    public void tick() {

        super.tick();

        LiaAction.tick(this);

        if (!this.level().isClientSide() && this.tickCount % 10 == 0) {

         /*   makeNearbyMonstersTargetLia();*/

            var entidades =
                    LiaPerception.getNearbyEntities(this, 8);

            var items =
                    LiaPerception.getNearbyItems(this, 8);

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





            boolean playerJumping =
                    LiaPerception.isPlayerJumping(this, 8);

            boolean newPlayerJump =
                    playerJumping && !previousPlayerJumping;

            // Obtiene el estado actual de LIA.
            LiaState state =
                    LiaPerception.getCurrentState(
                            this,
                            8,
                            tookDamage,
                            newPlayerJump
                    );

            double monsterDistance =
                    LiaPerception.getNearestMonsterDistance(
                            this,
                            8
                    );

            String monsterType =
                    LiaPerception.getNearestMonsterType(
                            this,
                            8
                    );

            boolean projectileNearby =
                    LiaPerception.isProjectileNearby(
                            this,
                            8
                    );

            Ia_lia.LOGGER.info(
                    "LIA | Proyectil cerca: {}",
                    projectileNearby
            );



            Ia_lia.LOGGER.info(
                    "LIA | Monstruo detectado: {}",
                    monsterType
            );


            double itemDistance = items.stream()
                    .min((item1, item2) ->
                            Double.compare(
                                    this.distanceTo(item1),
                                    this.distanceTo(item2)
                            )
                    )
                    .map(item -> (double) this.distanceTo(item))
                    .orElse(-1.0);

            double playerDistance =
                    LiaPerception.getNearestPlayerDistance(
                            this,
                            8
                    );

            double nearestItemValue = items.stream()
                    .min((item1, item2) ->
                            Double.compare(
                                    this.distanceTo(item1),
                                    this.distanceTo(item2)
                            )
                    )
                    .map(item -> itemKnowledge.getValue(
                            LiaPerception.identifyItem(item)
                    ))
                    .orElse(0.0);


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

            // LIA consulta lo que ha aprendido sobre cada objeto.
            for (var item : items) {

                String itemName =
                        LiaPerception.identifyItem(item);

                double value =
                        itemKnowledge.getValue(itemName);

                Ia_lia.LOGGER.info(
                        "LIA: He detectado un objeto: {} | Valor aprendido: {}",
                        itemName,
                        value
                );
            }

            /* LIA aprende de la experiencia anterior. */
            if (previousState != null && previousAction != null) {

                int reward = LiaReward.calculate(
                        previousState,
                        previousAction,
                        previousMonsterDistance,
                        monsterDistance,
                        previousItemDistance,
                        itemDistance,
                        previousPlayerDistance,
                        playerDistance,
                        tookDamage,
                        attackSuccessful,
                        monsterKilled,
                        blockedAttack
                );
                reward += pendingActionReward;
                pendingActionReward = 0;
                attackSuccessful = false;
                monsterKilled = false;
                blockedAttack = false;

                learning.learn(
                        previousState.toKey(),
                        previousAction,
                        reward,
                        state.toKey()
                );



                knowledge.rememberExperience(
                        previousEntity,
                        previousAction,
                        tookDamage,
                        previousDistance,
                        reward
                );
            }

            // La recompensa de esta experiencia.
            tookDamage = false;

            LiaDecision.Action action =
                    learning.chooseAction(state, nearestItemValue);



            Ia_lia.LOGGER.info(
                    "LIA decidio: {}",
                    action
            );

            String currentEntity = "none";
            double currentDistance = -1;

            var nearbyEntities =
                    LiaPerception.getNearbyEntities(this, 8);

            if (action == LiaDecision.Action.FLEE_MONSTER) {

                var nearestMonster =
                        nearbyEntities.stream()
                                .filter(entity ->
                                        entity instanceof Monster)
                                .min((entity1, entity2) ->
                                        Double.compare(
                                                this.distanceTo(entity1),
                                                this.distanceTo(entity2)
                                        ))
                                .orElse(null);

                if (nearestMonster != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(
                                    nearestMonster
                            );

                    currentDistance =
                            this.distanceTo(nearestMonster);
                }

            } else if (action == LiaDecision.Action.APPROACH_ITEM) {

                var nearestItem =
                        items.stream()
                                .min((item1, item2) ->
                                        Double.compare(
                                                this.distanceTo(item1),
                                                this.distanceTo(item2)
                                        ))
                                .orElse(null);

                if (nearestItem != null) {

                    currentEntity =
                            LiaPerception.identifyItem(nearestItem);

                    currentDistance =
                            this.distanceTo(nearestItem);

                    Ia_lia.LOGGER.info(
                            "LIA intenta acercarse a: {} | Valor aprendido: {}",
                            currentEntity,
                            itemKnowledge.getValue(currentEntity)
                    );
                }

            } else if (
                    action == LiaDecision.Action.APPROACH_PLAYER
            ) {

                var nearestPlayer =
                        nearbyEntities.stream()
                                .filter(entity ->
                                        entity instanceof net.minecraft.world.entity.player.Player)
                                .min((entity1, entity2) ->
                                        Double.compare(
                                                this.distanceTo(entity1),
                                                this.distanceTo(entity2)
                                        ))
                                .orElse(null);

                if (nearestPlayer != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(
                                    nearestPlayer
                            );

                    currentDistance =
                            this.distanceTo(nearestPlayer);
                }

            } else {

                var nearestEntity =
                        nearbyEntities.stream()
                                .min((entity1, entity2) ->
                                        Double.compare(
                                                this.distanceTo(entity1),
                                                this.distanceTo(entity2)
                                        ))
                                .orElse(null);

                if (nearestEntity != null) {

                    currentEntity =
                            LiaPerception.identifySpecificEntity(
                                    nearestEntity
                            );

                    currentDistance =
                            this.distanceTo(nearestEntity);
                }
            }
            /* LIA ejecuta lo que decidió. */
            String pickedItem =
                    LiaAction.execute(this, action);

            if (pickedItem != null) {

                Ia_lia.LOGGER.info(
                        "LIA completo la recogida de: {}",
                        pickedItem
                );

                // Recoger correctamente tiene una recompensa positiva.
                itemKnowledge.learn(
                        pickedItem,
                        20);

                pendingActionReward += 20;


            }

            // Guarda la experiencia actual.
            previousState = state;
            previousAction = action;
            previousMonsterDistance = monsterDistance;
            previousItemDistance = itemDistance;
            previousPlayerDistance = playerDistance;
            previousEntity = currentEntity;
            previousDistance = currentDistance;
            previousPlayerJumping = playerJumping;

        }
    }


    @Override
    public boolean hurtServer(
            ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            float amount
    ) {

        if (this.isUsingItem()
                && this.getUseItem().getItem()
                instanceof net.minecraft.world.item.ShieldItem) {

            Ia_lia.LOGGER.info(
                    "LIA bloqueo el ataque con el escudo."
            );

            blockedAttack = true;

            return false;
        }

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

    public LiaKnowledge getKnowledge() {
        return knowledge;
    }

    public LiaItemKnowledge getItemKnowledge() {
        return itemKnowledge;
    }


    public SimpleContainer getInventory() {
        return inventory;
    }




    public void setAttackSuccessful(boolean value) {
        attackSuccessful = value;
    }


    public void setMonsterKilled(boolean value) {
        monsterKilled = value;
    }

    public boolean wasAttackBlocked() {
        return blockedAttack;
    }


    /* Guarda los objetos que caben y devuelve la cantidad guardada. */



    public int addItemToInventory(ItemStack offeredStack) {
        if (offeredStack.isEmpty()) {
            return 0;
        }

        int originalCount = offeredStack.getCount();

        // addItem devuelve la pila que no pudo guardar.
        ItemStack remaining = inventory.addItem(offeredStack.copy());

        return originalCount - remaining.getCount();
    }



    /* Saca objetos del inventario para equiparlos o utilizarlos. */
    public ItemStack removeItemFromInventory(int slot, int amount) {
        if (slot < 0 || slot >= inventory.getContainerSize() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        return inventory.removeItem(slot, amount);
    }




}


