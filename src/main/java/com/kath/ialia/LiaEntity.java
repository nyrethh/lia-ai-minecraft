package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;

public class LiaEntity extends PathfinderMob {

    private final LiaMemory memory = new LiaMemory();

    private final LiaQTable qTable = new LiaQTable();

    private final LiaLearning learning = new LiaLearning(qTable);

    private LiaState previousState = null;

    private LiaDecision.Action previousAction = null;


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
                1,
                new RandomStrollGoal(this, 0.5)); // movimiento 0.5 es lo normal.
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.tickCount % 40 == 0) {

            var entidades = LiaPerception.getNearbyEntities(this, 8);

            if (entidades.isEmpty()) {

                Ia_lia.LOGGER.info(
                        "LIA: No veo nada cerca.");

            } else {

                for (Entity entidad : entidades) {

                    String tipo = LiaPerception.identifyEntity(entidad);

                    Ia_lia.LOGGER.info(
                            "LIA: He detectado un {}.",
                            tipo);

                    memory.remember(entidad);
                }
            }

            // obtener el estado actual de LIA
            LiaState state = LiaPerception.getCurrentState(this, 8);

            /* el bot aprende de la experiencia anterior*/
            if (previousState != null && previousAction != null) {

                int reward = LiaReward.calculate(
                        previousState,
                        previousAction
                );

                learning.learn(
                        previousState.toKey(),
                        previousAction,
                        reward,
                        state.toKey()
                );
            }



            // decidir qu hacer segn el estado
/*
            LiaDecision.Action action = LiaDecision.decide(state);
*/

               /* aqui el bot ahora decide que hacer segun lo aprendido.*/
            LiaDecision.Action action = learning.chooseAction(state.toKey());

            Ia_lia.LOGGER.info(
                    "LIA decidio: {}",
                    action);


            /* el bot ejecuta la decision*/
            LiaAction.execute(this, action);

            //guardamos la experiencia actual.
            previousState = state;
            previousAction = action;

        }
    }



    public LiaMemory getMemory()
    {


        return memory;
    }
}