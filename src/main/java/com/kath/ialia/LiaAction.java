package com.kath.ialia;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;

public class LiaAction {

    public static String execute(
            LiaEntity lia,
            LiaDecision.Action action
    ) {

        String pickedItem = null;

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

            case APPROACH_ITEM:

                ItemEntity item = LiaPerception.getNearbyItems(lia, 8)
                        .stream()
                        .min((item1, item2) ->
                                Double.compare(
                                        lia.distanceTo(item1),
                                        lia.distanceTo(item2)
                                )
                        )
                        .orElse(null);

                if (item != null) {

                    double distance = lia.distanceTo(item);

                    Ia_lia.LOGGER.info(
                            "LIA intenta acercarse a: {} | Distancia: {}",
                            LiaPerception.identifyItem(item),
                            distance
                    );

                    if (distance <= 1.5) {

                        pickedItem =
                                LiaPerception.identifyItem(item);

                        Ia_lia.LOGGER.info(
                                "LIA recogio: {}",
                                pickedItem
                        );

                        item.discard();

                    } else {

                        lia.getNavigation().moveTo(
                                item,
                                0.7
                        );
                    }
                }

                break;



            case REACT_FLEE_PLAYER:

                Player fleeingPlayer = lia.level().getNearestPlayer(
                        lia,
                        8
                );

                if (fleeingPlayer != null) {

                    Ia_lia.LOGGER.info(
                            "LIA reacciona al salto alejandose del jugador."
                    );

                    double dx =
                            lia.getX() - fleeingPlayer.getX();

                    double dz =
                            lia.getZ() - fleeingPlayer.getZ();

                    double distance =
                            Math.sqrt(dx * dx + dz * dz);

                    if (distance > 0) {

                        double targetX =
                                lia.getX()
                                        + (dx / distance) * 8;

                        double targetZ =
                                lia.getZ()
                                        + (dz / distance) * 8;

                        lia.getNavigation().moveTo(
                                targetX,
                                lia.getY(),
                                targetZ,
                                0.8
                        );
                    }
                }

                break;

            case REACT_IDLE:

                Ia_lia.LOGGER.info(
                        "LIA reacciona al salto quedandose quieta."
                );

                lia.getNavigation().stop();

                break;


            case FLEE_MONSTER:

                Entity monster = lia.level().getEntities(
                                lia,
                                lia.getBoundingBox().inflate(8),
                                entity ->
                                        entity instanceof net.minecraft.world.entity.monster.Monster
                        ).stream()
                        .min((entity1, entity2) ->
                                Double.compare(
                                        lia.distanceTo(entity1),
                                        lia.distanceTo(entity2)
                                )
                        )
                        .orElse(null);

                if (monster != null) {

                    double dx =
                            lia.getX() - monster.getX();

                    double dz =
                            lia.getZ() - monster.getZ();

                    double distance =
                            Math.sqrt(dx * dx + dz * dz);

                    if (distance > 0) {

                        double targetX =
                                lia.getX()
                                        + (dx / distance) * 8;

                        double targetZ =
                                lia.getZ()
                                        + (dz / distance) * 8;

                        lia.getNavigation().moveTo(
                                targetX,
                                lia.getY(),
                                targetZ,
                                0.8
                        );
                    }
                }

                break;

            case JUMP:

                Ia_lia.LOGGER.info(
                        "LIA decidio saltar por su cuenta."
                );

                lia.getJumpControl().jump();

                break;


        }

        return pickedItem;
    }
}