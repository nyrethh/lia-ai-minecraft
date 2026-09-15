package com.kath.ialia;

public class LiaDecision {

    public enum Action {
        IDLE,
        WANDER,
        APPROACH_PLAYER,
        FLEE_MONSTER
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