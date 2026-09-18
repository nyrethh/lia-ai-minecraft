package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class LiaAction {

    public static void execute(
            LiaEntity lia,
            LiaDecision.Action action
    ) {

        switch (action) {

            case IDLE:
                break;

            case WANDER:
                // RandomStrollGoal se encarga de este caso.
                break;

            case APPROACH_PLAYER:

                Player player = lia.level().getNearestPlayer(
                        lia,
                        8
                );

                if (player != null) {
                    lia.getNavigation().moveTo(
                            player,
                            0.7
                    );
                }

                break;

            case FLEE_MONSTER:

                Entity monster = lia.level().getEntities(
                                lia,
                                lia.getBoundingBox().inflate(8),
                                entity -> entity instanceof net.minecraft.world.entity.monster.Monster
                        ).stream()
                        .min((entity1, entity2) ->
                                Double.compare(
                                        lia.distanceTo(entity1),
                                        lia.distanceTo(entity2)
                                )
                        )
                        .orElse(null);

                if (monster != null) {

                    double dx = lia.getX() - monster.getX();
                    double dz = lia.getZ() - monster.getZ();

                    double distance = Math.sqrt(
                            dx * dx + dz * dz
                    );

                    if (distance > 0) {

                        double targetX =
                                lia.getX() + (dx / distance) * 8;

                        double targetZ =
                                lia.getZ() + (dz / distance) * 8;

                        lia.getNavigation().moveTo(
                                targetX,
                                lia.getY(),
                                targetZ,
                                0.8
                        );
                    }
                }

                break;
        }
    }
}