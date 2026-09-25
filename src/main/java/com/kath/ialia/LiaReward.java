package com.kath.ialia;

/* Recompensas de LIA. */
public class LiaReward {

    public static int calculate(
            LiaState state,
            LiaDecision.Action action,
            double previousMonsterDistance,
            double currentMonsterDistance,
            double previousItemDistance,
            double currentItemDistance,
            double previousPlayerDistance,
            double currentPlayerDistance,
            boolean tookDamage
    ) {

        int reward = 0;

        // Si se lastima, es negativo.
        if (tookDamage) {
            reward -= 20;
        }

        if (action == LiaDecision.Action.FLEE_MONSTER) {

            if (previousMonsterDistance >= 0
                    && currentMonsterDistance >= 0) {

                if (currentMonsterDistance > previousMonsterDistance) {
                    reward += 10;
                }

                if (currentMonsterDistance < previousMonsterDistance) {
                    reward -= 10;
                }
            }
        }

        if (action == LiaDecision.Action.JUMP) {

            if (previousMonsterDistance >= 0
                    && currentMonsterDistance >= 0) {

                if (currentMonsterDistance > previousMonsterDistance) {
                    reward += 10;

                    Ia_lia.LOGGER.info(
                            "LIA salto y se alejo del monstruo. Recompensa: +10"
                    );
                }

                if (currentMonsterDistance < previousMonsterDistance) {
                    reward -= 5;

                    Ia_lia.LOGGER.info(
                            "LIA salto pero se acerco al monstruo. Recompensa: -5"
                    );
                }
            }
        }










        if (action == LiaDecision.Action.APPROACH_PLAYER) {

            if (previousPlayerDistance >= 0
                    && currentPlayerDistance >= 0) {

                if (currentPlayerDistance < previousPlayerDistance) {
                    reward += 10;
                }

                if (currentPlayerDistance > previousPlayerDistance) {
                    reward -= 10;
                }
            }
        }



        if (action == LiaDecision.Action.APPROACH_ITEM) {

            if (previousItemDistance >= 0
                    && currentItemDistance >= 0) {

                if (currentItemDistance < previousItemDistance) {
                    reward += 5;
                }

                if (currentItemDistance > previousItemDistance) {
                    reward -= 5;
                }
            }
        }

        return reward;
    }
}