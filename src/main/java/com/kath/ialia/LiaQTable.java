package com.kath.ialia;

/*Q-Table */
import java.util.HashMap;
import java.util.Map;

public class LiaQTable {

    /* Estado > Accin > Valor */
    private final Map<String, Map<LiaDecision.Action, Double>> table = new HashMap<>();

    /*  Busca que tan buena es una acción para una situacion determinada. */
    public double getQValue(String state, LiaDecision.Action action) {

        Map<LiaDecision.Action, Double> actions = table.computeIfAbsent(state, key -> new HashMap<>());

        return actions.getOrDefault(action, 0.0);
    }


    /*Cambia el valor de una accion para una situacion determinada. */
    public void setQValue(
        String state,
        LiaDecision.Action action,
        double value
) {

    Map<LiaDecision.Action, Double> actions =
            table.computeIfAbsent(state, key -> new HashMap<>());

    actions.put(action, value);
}



}
