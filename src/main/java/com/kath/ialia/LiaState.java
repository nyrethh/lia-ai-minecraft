package com.kath.ialia;

public class LiaState {

    private final boolean playerNearby;
    private final boolean animalNearby;
    private final boolean monsterNearby;
    private final boolean itemNearby;

    private final double playerDistance;
    private final double monsterDistance;

    private final boolean tookDamage;
    private final boolean woodNearby;

    private final boolean playerJumping;

    public LiaState(
            boolean playerNearby,
            boolean animalNearby,
            boolean monsterNearby,
            boolean itemNearby,
            double playerDistance,
            double monsterDistance,
            boolean tookDamage,
            boolean woodNearby,
            boolean playerJumping
    ) {
        this.playerNearby = playerNearby;
        this.animalNearby = animalNearby;
        this.monsterNearby = monsterNearby;
        this.itemNearby = itemNearby;

        this.playerDistance = playerDistance;
        this.monsterDistance = monsterDistance;

        this.tookDamage = tookDamage;
        this.woodNearby = woodNearby;

        this.playerJumping = playerJumping;
    }

    /* Los getters */

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

    public boolean isItemNearby() {
        return itemNearby;
    }

    public boolean isWoodNearby() {
        return woodNearby;
    }

    public boolean isPlayerJumping() {
        return playerJumping;
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
                + (tookDamage ? "1" : "0")
                + "|"
                + (woodNearby ? "1" : "0")
                + "|"
                + (itemNearby ? "1" : "0")
                + "|"
                + (playerJumping ? "1" : "0");
    }

    private String getDistanceKey(double distance) {

        if (distance < 0) {
            return "NONE";
        }

        if (distance <= 3) {
            return "NEAR";
        }

        if (distance <= 6) {
            return "MEDIUM";
        }

        return "FAR";
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
                ", woodNearby=" + woodNearby +
                ", playerJumping=" + playerJumping +
                '}';
    }
}