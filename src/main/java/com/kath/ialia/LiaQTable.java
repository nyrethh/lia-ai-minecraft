package com.kath.ialia;

/* Q-Table */
import java.util.HashMap;
import java.util.Map;

public class LiaQTable {

    /* Estado > Acción > Valor */
    private final Map<String, Map<LiaDecision.Action, Double>> table =
            new HashMap<>();

    /* busca que tan buena es una acción para una situacion determinada. */
    public double getQValue(String state, LiaDecision.Action action) {

        Map<LiaDecision.Action, Double> actions =
                table.computeIfAbsent(state, key -> new HashMap<>());

        return actions.getOrDefault(action, 0.0);
    }

    /* Cambia el valor de una acción para una situaciin determinada. */
    public void setQValue(
            String state,
            LiaDecision.Action action,
            double value
    ) {

        Map<LiaDecision.Action, Double> actions =
                table.computeIfAbsent(state, key -> new HashMap<>());

        actions.put(action, value);
    }

    /* Busca el valor + alto entre todas las acciones para un estado. */
    public double getMaxQValue(String state) {

        double maxValue = Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action : LiaDecision.Action.values()) {

            double value = getQValue(state, action);

            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }


    public LiaDecision.Action getBestAction(String state) {

        LiaDecision.Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action : LiaDecision.Action.values()) {

            double value = getQValue(state, action);

            if (value > bestValue) {
                bestValue = value;
                bestAction = action;
            }
        }

        return bestAction;
    }

}