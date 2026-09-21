package com.kath.ialia;

public class LiaState {

    private final boolean playerNearby;
    private final boolean animalNearby;
    private final boolean monsterNearby;

    private final double playerDistance;
    private final double monsterDistance;

    private final boolean tookDamage;

    public LiaState(
            boolean playerNearby,
            boolean animalNearby,
            boolean monsterNearby,
            double playerDistance,
            double monsterDistance,
            boolean tookDamage
    ) {
        this.playerNearby = playerNearby;
        this.animalNearby = animalNearby;
        this.monsterNearby = monsterNearby;

        this.playerDistance = playerDistance;
        this.monsterDistance = monsterDistance;

        this.tookDamage = tookDamage;
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

    public double getPlayerDistance() {
        return playerDistance;
    }

    public double getMonsterDistance() {
        return monsterDistance;
    }

    public boolean tookDamage() {
        return tookDamage;
    }

    public String toKey() {

        return (playerNearby ? "1" : "0")
                + (animalNearby ? "1" : "0")
                + (monsterNearby ? "1" : "0")
                + "|"
                + getDistanceKey(playerDistance)
                + "|"
                + getDistanceKey(monsterDistance)
                + "|"
                + (tookDamage ? "1" : "0");
    }

    private String getDistanceKey(double distance) {

        if (distance < 0) {
            return "none";
        }

        return String.valueOf(
                (int) Math.round(distance)
        );
    }

    @Override
    public String toString() {

        return "LiaState{" +
                "playerNearby=" + playerNearby +
                ", animalNearby=" + animalNearby +
                ", monsterNearby=" + monsterNearby +
                ", playerDistance=" + playerDistance +
                ", monsterDistance=" + monsterDistance +
                ", tookDamage=" + tookDamage +
                '}';
    }
}