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

        boolean playerNearby =
                key.charAt(0) == '1';

        boolean animalNearby =
                key.charAt(1) == '1';

        boolean monsterNearby =
                key.charAt(2) == '1';

        return new LiaState(
                playerNearby,
                animalNearby,
                monsterNearby,
                -1,
                -1,
                false
        );
    }

    private List<LiaDecision.Action> getValidActions(
            LiaState state
    ) {

        List<LiaDecision.Action> actions =
                new ArrayList<>();

        // Estas dos siempre tienen sentido.
        actions.add(LiaDecision.Action.IDLE);
        actions.add(LiaDecision.Action.WANDER);

        // Solo acercarse si hay un jugador.
        if (state.isPlayerNearby()) {

            actions.add(
                    LiaDecision.Action.APPROACH_PLAYER
            );
        }

        // Solo huir si hay un monstruo.
        if (state.isMonsterNearby()) {

            actions.add(
                    LiaDecision.Action.FLEE_MONSTER
            );
        }

        return actions;
    }
}