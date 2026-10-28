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
            boolean tookDamage,
            boolean attackSuccessful,
            boolean monsterKilled,
            boolean blockedAttack
    ) {

        int reward = 0;

        /* Recibir daño siempre es negativo. */
        if (tookDamage) {
            reward -= 20;
        }

        /* Bloquear correctamente es una buena defensa. */
        if (blockedAttack
                && action == LiaDecision.Action.BLOCK) {

            reward += 25;

            Ia_lia.LOGGER.info(
                    "LIA bloqueo un ataque. Recompensa: +25"
            );
        }

        if (action == LiaDecision.Action.FLEE_MONSTER) {

            if (previousMonsterDistance >= 0
                    && currentMonsterDistance >= 0) {

                if (currentMonsterDistance > previousMonsterDistance) {

                    reward += 10;

                    Ia_lia.LOGGER.info(
                            "LIA se alejo del monstruo. Recompensa: +10"
                    );
                }

                if (currentMonsterDistance < previousMonsterDistance) {

                    reward -= 10;

                    Ia_lia.LOGGER.info(
                            "LIA huyo pero se acerco al monstruo. Recompensa: -10"
                    );
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

        if (action == LiaDecision.Action.ATTACK_MONSTER) {

            /* Golpear es una recompensa importante. */
            if (attackSuccessful) {

                reward += 20;

                Ia_lia.LOGGER.info(
                        "LIA golpeo al monstruo. Recompensa: +20"
                );
            }

            /* Eliminar al monstruo tiene una recompensa mayor. */
            if (monsterKilled) {

                reward += 50;

                Ia_lia.LOGGER.info(
                        "LIA elimino al monstruo. Recompensa: +50"
                );
            }

            if (previousMonsterDistance >= 0
                    && currentMonsterDistance >= 0) {

                /* Acercarse mientras intenta atacar es bueno. */
                if (currentMonsterDistance
                        < previousMonsterDistance) {

                    reward += 10;

                    Ia_lia.LOGGER.info(
                            "LIA se acerco al monstruo para atacar. Recompensa: +10"
                    );
                }

                /* Alejarse mientras intenta atacar no ayuda al combate. */
                if (currentMonsterDistance
                        > previousMonsterDistance) {

                    reward -= 5;

                    Ia_lia.LOGGER.info(
                            "LIA se alejo del monstruo al intentar atacar. Recompensa: -5"
                    );
                }
            }
        }

        if (action == LiaDecision.Action.APPROACH_PLAYER) {

            if (previousPlayerDistance >= 0
                    && currentPlayerDistance >= 0) {

                if (currentPlayerDistance
                        < previousPlayerDistance) {

                    reward += 10;
                }

                if (currentPlayerDistance
                        > previousPlayerDistance) {

                    reward -= 10;
                }
            }
        }

        if (action == LiaDecision.Action.APPROACH_ITEM) {

            if (previousItemDistance >= 0
                    && currentItemDistance >= 0) {

                if (currentItemDistance
                        < previousItemDistance) {

                    reward += 5;
                }

                if (currentItemDistance
                        > previousItemDistance) {

                    reward -= 5;
                }
            }
        }

        return reward;
    }
}