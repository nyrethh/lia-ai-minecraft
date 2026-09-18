package com.kath.ialia;


/*recompensas de LIA. */
public class LiaReward {


    public static int calculate(
            LiaState state,
            LiaDecision.Action action,
            double previousDistance,
            double currentDistance,
            boolean tookDamage
    ) {



        // Recibir daño siempre es una experiencia negativa.
        if (tookDamage) {
            return -20;
        }

        if (action == LiaDecision.Action.FLEE_MONSTER) {

            // Si no podemos comparar las distancias
            // no damos recompensa
            if (previousDistance < 0 || currentDistance < 0) {
                return 0;
            }

            // el mounstruo esta mas lejos
            if (currentDistance > previousDistance) {
                return 10;
            }

            // esta mas cerca el mounstruo
            if (currentDistance < previousDistance) {
                return -10;
            }
        }

        return 0;
    }
}