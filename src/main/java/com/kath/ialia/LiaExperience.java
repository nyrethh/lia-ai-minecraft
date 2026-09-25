package com.kath.ialia;

public class LiaExperience {

    private final String entity;
    private final LiaDecision.Action action;
    private final boolean tookDamage;
    private final double distance;
    private final double reward;

    public LiaExperience(
            String entity,
            LiaDecision.Action action,
            boolean tookDamage,
            double distance,
            double reward
    ) {
        this.entity = entity;
        this.action = action;
        this.tookDamage = tookDamage;
        this.distance = distance;
        this.reward = reward;
    }

    public String getEntity() {
        return entity;
    }

    public LiaDecision.Action getAction() {
        return action;
    }

    public boolean tookDamage()
    {
        return tookDamage;
    }

    public double getDistance()
    {
        return distance;
    }

    public double getReward() {
        return reward;
    }

    @Override
    public String toString() {

        return "LiaExperience{" +
                "entity='" + entity + '\'' +
                ", action=" + action +
                ", tookDamage=" + tookDamage +
                ", distance=" + distance +
                ", reward=" + reward +
                '}';
    }
}