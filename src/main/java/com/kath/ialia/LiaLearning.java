package com.kath.ialia;
import java.util.Random;

public class LiaLearning {

    private final LiaQTable qTable;
    private final double learningRate = 0.1; /* cuanto aprende de la experiencia nueva*/
    private final double discountFactor = 0.9; /* cuento le importan las recompensas futuras*/
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


        /* cuanto valoraba el bot esta accion antes*/
        double currentQ = qTable.getQValue(state, action);


        /*en el nuevo estado, se pregunta cual es el mejor valor que conoce*/
        double maxNextQ = qTable.getMaxQValue(nextState);

        double newQ = currentQ
                + learningRate
                * (reward
                + discountFactor * maxNextQ
                - currentQ);

        qTable.setQValue(
                state,
                action,
                newQ
        );

        Ia_lia.LOGGER.info(
                "LIA aprende | Estado: {} | Accion: {} | Recompensa: {} | Nuevo Q: {}",
                state,
                action,
                reward,
                newQ
        );

    }

    public LiaDecision.Action chooseAction(String state) {

        // Si es un estado completamente nuevo,
        // LIA explora una acción al azar.
        if (!qTable.hasState(state)) {

            LiaDecision.Action[] actions =
                    LiaDecision.Action.values();

            return actions[random.nextInt(actions.length)];
        }

        // Si ya conoce el estado, aplica epsilon-greedy.
        if (random.nextDouble() < explorationRate) {

            LiaDecision.Action[] actions =
                    LiaDecision.Action.values();

            return actions[random.nextInt(actions.length)];
        }

        return qTable.getBestAction(state);
    }


    private final double explorationRate = 0.1;


}