package com.kath.ialia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiaLearning {

    private final LiaQTable qTable;

    private final double learningRate = 0.1;
    private final double discountFactor = 0.9;
    private final double explorationRate = 0.1;

    private final Random random = new Random();


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
                qTable.getQValue(state, action);

        // Solo usamos acciones válidas del siguiente estado.
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

        // Guarda el aprendizaje de LIA
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

        List<LiaDecision.Action> validActions =
                getValidActions(state);

        String stateKey = state.toKey();

        // Si es un estado nuevo, explora una acción válida.
        if (!qTable.hasState(stateKey)) {

            return validActions.get(
                    random.nextInt(validActions.size())
            );
        }

        // 10% de exploración.
        if (random.nextDouble() < explorationRate) {

            return validActions.get(
                    random.nextInt(validActions.size())
            );
        }

        // Busca la mejor acción entre las acciones válidas.
        LiaDecision.Action bestAction = null;

        double bestValue =
                Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action : validActions) {

            double value =
                    qTable.getQValue(
                            stateKey,
                            action
                    );

            if (value > bestValue) {

                bestValue = value;
                bestAction = action;
            }
        }

        return bestAction;
    }

    private double getMaxValidQValue(String stateKey) {

        LiaState state =
                stateFromKey(stateKey);

        List<LiaDecision.Action> validActions =
                getValidActions(state);

        double maxValue =
                Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action : validActions) {

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

    private LiaState stateFromKey(String key) {

        String[] parts = key.split("\\|");

        boolean playerNearby =
                parts[0].charAt(0) == '1';

        boolean animalNearby =
                parts[0].charAt(1) == '1';

        boolean monsterNearby =
                parts[0].charAt(2) == '1';

        boolean tookDamage =
                parts.length > 3 && parts[3].equals("1");

        boolean woodNearby =
                parts.length > 4 && parts[4].equals("1");

        boolean itemNearby =
                parts.length > 5 && parts[5].equals("1");


        boolean playerJumping =
                parts.length > 6 && parts[6].equals("1");

        return new LiaState(
                playerNearby,
                animalNearby,
                monsterNearby,
                itemNearby,
                -1,
                -1,
                tookDamage,
                woodNearby,
                playerJumping
        );
    }





    private List<LiaDecision.Action> getValidActions(LiaState state) {

        List<LiaDecision.Action> actions = new ArrayList<>();

        actions.add(LiaDecision.Action.IDLE);
        actions.add(LiaDecision.Action.WANDER);
        actions.add(LiaDecision.Action.JUMP);

        if (state.isPlayerNearby()) {
            actions.add(LiaDecision.Action.APPROACH_PLAYER);
        }

        if (state.isPlayerJumping()) {
            actions.add(LiaDecision.Action.REACT_APPROACH_PLAYER);
            actions.add(LiaDecision.Action.REACT_FLEE_PLAYER);
            actions.add(LiaDecision.Action.REACT_IDLE);


        }

        if (state.isItemNearby()) {
            actions.add(LiaDecision.Action.APPROACH_ITEM);
        }

        if (state.isMonsterNearby()) {
            actions.add(LiaDecision.Action.FLEE_MONSTER);
        }

        Ia_lia.LOGGER.info(
                "LIA acciones disponibles: {}",
                actions
        );

        return actions;
    }

}