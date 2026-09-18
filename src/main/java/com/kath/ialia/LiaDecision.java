package com.kath.ialia;

public class LiaDecision {

    public enum Action {
        IDLE, /* quedarse quieto*/
        WANDER, /*deambular*/
        APPROACH_PLAYER, /* acercarse al jugador */
        FLEE_MONSTER  /* huir*/
    }

    public static Action decide(LiaState state) {

        if (state.isMonsterNearby()) {
            return Action.FLEE_MONSTER;
        }

        if (state.isPlayerNearby()) {
            return Action.APPROACH_PLAYER;
        }

        if (state.isAnimalNearby()) {
            return Action.WANDER;
        }

        return Action.IDLE;
    }
}