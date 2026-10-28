package com.kath.ialia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiaLearning {

    private final LiaQTable qTable;

    private final double learningRate = 0.1;
    private final double discountFactor = 0.9;

    private final double explorationRate = 0.1;

    /* Durante combate LIA explora mas. */
    private final double combatExplorationRate = 0.35;

    private final Random random = new Random();

    private long decisionCount = 0;





    public LiaLearning(LiaQTable qTable) {
        this.qTable = qTable;
    }


    public void learn(
            String state,
            LiaDecision.Action action,
            double reward,
            String nextState
    ) {

        double currentQ =
                qTable.getQValue(
                        state,
                        action
                );

        double maxNextQ =
                getMaxValidQValue(nextState);

        double newQ =
                currentQ
                        + learningRate
                        * (
                        reward
                                + discountFactor * maxNextQ
                                - currentQ
                );

        qTable.setQValue(
                state,
                action,
                newQ
        );

        qTable.save();

        Ia_lia.LOGGER.info(
                "LIA aprende | Estado: {} | Acción: {} | Recompensa: {} | Nuevo Q: {}",
                state,
                action,
                reward,
                newQ
        );
    }


    public LiaDecision.Action chooseAction(LiaState state) {
        return chooseAction(state, 0.0);
    }

    public LiaDecision.Action chooseAction(
            LiaState state,
            double learnedItemValue
    ) {
        decisionCount++;

        List<LiaDecision.Action> validActions =
                getValidActions(state);

        String stateKey = state.toKey();

        if (state.isMonsterNearby()) {
            List<LiaDecision.Action> combatActions =
                    getCombatActions(state);

            if (!combatActions.isEmpty()) {
                if (random.nextDouble() < combatExplorationRate) {
                    if (random.nextDouble() < 0.5) {
                        return LiaDecision.Action.ATTACK_MONSTER;
                    }

                    return combatActions.get(
                            random.nextInt(combatActions.size())
                    );
                }

                return chooseBestAction(
                        stateKey,
                        combatActions,
                        learnedItemValue
                );
            }
        }

        // Empieza explorando más y reduce gradualmente la exploración.
        double explorationRateNow = Math.max(
                0.05,
                0.30 * Math.exp(-decisionCount / 500.0)
        );

        if (!qTable.hasState(stateKey)
                || random.nextDouble() < explorationRateNow) {
            return validActions.get(
                    random.nextInt(validActions.size())
            );
        }

        return chooseBestAction(
                stateKey,
                validActions,
                learnedItemValue
        );
    }


    private LiaDecision.Action chooseBestAction(
            String stateKey,
            List<LiaDecision.Action> actions,
            double learnedItemValue
    ) {
        List<LiaDecision.Action> bestActions = new ArrayList<>();
        double bestValue = Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action : actions) {
            double value = qTable.getQValue(stateKey, action);

            // El conocimiento de objetos influye en la decisión,
            // pero no sustituye lo que LIA aprende mediante Q-learning.
            if (action == LiaDecision.Action.APPROACH_ITEM) {
                value += Math.max(
                        0.0,
                        Math.min(3.0, learnedItemValue * 0.1)
                );
            }

            if (value > bestValue) {
                bestValue = value;
                bestActions.clear();
                bestActions.add(action);
            } else if (Double.compare(value, bestValue) == 0) {
                bestActions.add(action);
            }
        }

        return bestActions.get(random.nextInt(bestActions.size()));
    }


    private double getMaxValidQValue(
            String stateKey
    ) {

        LiaState state =
                stateFromKey(stateKey);

        List<LiaDecision.Action> validActions =
                getValidActions(state);

        double maxValue =
                Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action :
                validActions) {

            double value =
                    qTable.getQValue(
                            stateKey,
                            action
                    );

            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }


    private LiaState stateFromKey(
            String key
    ) {

        String[] parts =
                key.split("\\|");

        boolean playerNearby =
                parts[0].charAt(0) == '1';

        boolean animalNearby =
                parts[0].charAt(1) == '1';

        boolean monsterNearby =
                parts[0].charAt(2) == '1';

        boolean tookDamage =
                parts.length > 3
                        && parts[3].equals("1");

        boolean woodNearby =
                parts.length > 4
                        && parts[4].equals("1");

        boolean itemNearby =
                parts.length > 5
                        && parts[5].equals("1");

        boolean playerJumping =
                parts.length > 6
                        && parts[6].equals("1");

        boolean projectileNearby =
                parts.length > 7
                        && parts[7].equals("1");

        boolean shieldAvailable =
                parts.length > 8
                        && parts[8].equals("1");

        return new LiaState(
                playerNearby,
                animalNearby,
                monsterNearby,
                itemNearby,
                -1,
                -1,
                tookDamage,
                woodNearby,
                playerJumping,
                projectileNearby,
                shieldAvailable
        );
    }


    private List<LiaDecision.Action> getValidActions(
            LiaState state
    ) {

        List<LiaDecision.Action> actions =
                new ArrayList<>();

        actions.add(
                LiaDecision.Action.IDLE
        );

        actions.add(
                LiaDecision.Action.WANDER
        );

        actions.add(
                LiaDecision.Action.JUMP
        );

        if (state.isPlayerNearby()) {

            actions.add(
                    LiaDecision.Action.APPROACH_PLAYER
            );
        }

        // Si LIA detecta madera, puede intentar talar.
        if (state.isWoodNearby()) {
            actions.add(
                    LiaDecision.Action.CHOP_TREE
            );
        }


        if (state.isPlayerJumping()) {

            actions.add(
                    LiaDecision.Action.REACT_APPROACH_PLAYER
            );

            actions.add(
                    LiaDecision.Action.REACT_FLEE_PLAYER
            );

            actions.add(
                    LiaDecision.Action.REACT_IDLE
            );
        }

        if (state.isItemNearby()) {

            actions.add(
                    LiaDecision.Action.APPROACH_ITEM
            );
        }

        if (state.isMonsterNearby()) {

            actions.add(
                    LiaDecision.Action.FLEE_MONSTER
            );

            actions.add(
                    LiaDecision.Action.ATTACK_MONSTER
            );

            if (state.isProjectileNearby()) {

                actions.add(
                        LiaDecision.Action.CROUCH
                );

                actions.add(
                        LiaDecision.Action.HIDE
                );

                if (state.isShieldAvailable()) {

                    actions.add(
                            LiaDecision.Action.BLOCK
                    );
                }
            }
        }

        Ia_lia.LOGGER.info(
                "LIA acciones disponibles: {}",
                actions
        );

        return actions;
    }


    /* Acciones importantes durante combate.*/
    private List<LiaDecision.Action> getCombatActions(
            LiaState state
    ) {

        List<LiaDecision.Action> actions =
                new ArrayList<>();

        actions.add(
                LiaDecision.Action.ATTACK_MONSTER
        );

        actions.add(
                LiaDecision.Action.FLEE_MONSTER
        );


        if (state.isMonsterNearby()) {
            actions.add(LiaDecision.Action.FLEE_MONSTER);
            actions.add(LiaDecision.Action.ATTACK_MONSTER);
        }

        if (state.isProjectileNearby()) {
            actions.add(LiaDecision.Action.CROUCH);
            actions.add(LiaDecision.Action.HIDE);

            if (state.isShieldAvailable()) {
                actions.add(LiaDecision.Action.BLOCK);
            }
        }


        return actions;
    }

}