package com.kath.ialia;



/*Estado actual de LIA / situacin actual del entorno. */

public class LiaState {

    private boolean playerNearby;
    private boolean animalNearby;
    private boolean monsterNearby;

    public LiaState(
            boolean playerNearby,
            boolean animalNearby,
            boolean monsterNearby
    ) {
        this.playerNearby = playerNearby;
        this.animalNearby = animalNearby;
        this.monsterNearby = monsterNearby;
    }

    public boolean isPlayerNearby() {
        return playerNearby;
    }

    public boolean isAnimalNearby() {
        return animalNearby;
    }

    public boolean isMonsterNearby() {
        return monsterNearby;
    }

    @Override
    public String toString() {
        return "LiaState{" +
                "playerNearby=" + playerNearby +
                ", animalNearby=" + animalNearby +
                ", monsterNearby=" + monsterNearby +
                '}';
    }
}