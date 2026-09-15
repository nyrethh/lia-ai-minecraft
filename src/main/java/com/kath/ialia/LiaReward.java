package com.kath.ialia;


/*recompensas de LIA. */
public class LiaReward {


    /* calcular la accion de lia */
    public static int calculate(
            LiaState state,
            LiaDecision.Action action /*la decision qe lia decido realizar */
    ) {

        /*si decidió huir entonces tiene una recompensa de 10pts */
        if (state.isMonsterNearby()
                && action == LiaDecision.Action.FLEE_MONSTER) {

            return 10;
        }
/*si decide quedarse quieta al ver un mounstro entonces es negativo */
        if (state.isMonsterNearby()
                && action != LiaDecision.Action.FLEE_MONSTER) {

            return -10;
        }

        if (state.isPlayerNearby()
                && action == LiaDecision.Action.APPROACH_PLAYER) {

            return 5;
        }

        return 0;
    }
}