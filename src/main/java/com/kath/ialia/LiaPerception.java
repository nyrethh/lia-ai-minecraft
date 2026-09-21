package com.kath.ialia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class LiaPerception {

    public static List<Entity> getNearbyEntities(
            LiaEntity lia,
            double distance
    ) {

        return lia.level().getEntities(
                lia,
                lia.getBoundingBox().inflate(distance),
                entity -> entity != lia
        );
    }

    public static String identifyEntity(Entity entity) {

        if (entity instanceof Player) {
            return "jugador";
        }

        if (entity instanceof Monster) {
            return "monstruo";
        }

        if (entity.getType().getCategory().isFriendly()) {
            return "animal";
        }

        return "otra entidad";
    }

    public static String identifySpecificEntity(Entity entity) {

        return BuiltInRegistries.ENTITY_TYPE
                .getKey(entity.getType())
                .getPath();
    }
    public static LiaState getCurrentState(
            LiaEntity lia,
            double distance,
            boolean tookDamage
    ) {

        boolean playerNearby = false;
        boolean animalNearby = false;
        boolean monsterNearby = false;

        List<Entity> entities =
                getNearbyEntities(lia, distance);

        for (Entity entity : entities) {

            String type =
                    identifyEntity(entity);

            switch (type) {

                case "jugador":
                    playerNearby = true;
                    break;

                case "animal":
                    animalNearby = true;
                    break;

                case "monstruo":
                    monsterNearby = true;
                    break;
            }
        }

        return new LiaState(
                playerNearby,
                animalNearby,
                monsterNearby,
                getNearestPlayerDistance(lia, distance),
                getNearestMonsterDistance(lia, distance),
                tookDamage
        );
    }

    public static double getNearestMonsterDistance(
            LiaEntity lia,
            double distance
    ) {

        Entity nearestMonster =
                lia.level().getEntities(
                                lia,
                                lia.getBoundingBox().inflate(distance),
                                entity -> entity instanceof Monster
                        ).stream()
                        .min((entity1, entity2) ->
                                Double.compare(
                                        lia.distanceTo(entity1),
                                        lia.distanceTo(entity2)
                                )
                        )
                        .orElse(null);

        if (nearestMonster == null) {
            return -1;
        }

        return lia.distanceTo(nearestMonster);
    }

    public static double getNearestPlayerDistance(
            LiaEntity lia,
            double distance
    ) {

        Entity nearestPlayer =
                getNearbyEntities(lia, distance).stream()
                        .filter(entity -> entity instanceof Player)
                        .min((entity1, entity2) ->
                                Double.compare(
                                        lia.distanceTo(entity1),
                                        lia.distanceTo(entity2)
                                )
                        )
                        .orElse(null);

        if (nearestPlayer == null) {
            return -1;
        }

        return lia.distanceTo(nearestPlayer);
    }

    public static LiaWorldObservation observeWorld(
            LiaEntity lia,
            int radius
    ) {

        List<String> blocks =
                new ArrayList<>();

        List<String> entities =
                new ArrayList<>();

        // Observamos los bloques alrededor de LIA.
        for (int x = -radius; x <= radius; x++) {

            for (int y = -1; y <= 1; y++) {

                for (int z = -radius; z <= radius; z++) {

                    BlockPos position =
                            lia.blockPosition().offset(
                                    x,
                                    y,
                                    z
                            );

                    BlockState blockState =
                            lia.level().getBlockState(position);

                    String blockName =
                            BuiltInRegistries.BLOCK
                                    .getKey(
                                            blockState.getBlock()
                                    )
                                    .getPath();

                    if (!blocks.contains(blockName)) {
                        blocks.add(blockName);
                    }
                }
            }
        }

        // Observamos las entidades cercanas.
        List<Entity> nearbyEntities =
                getNearbyEntities(lia, radius);

        for (Entity entity : nearbyEntities) {

            String entityName =
                    identifySpecificEntity(entity);

            if (!entities.contains(entityName)) {
                entities.add(entityName);
            }
        }

        return new LiaWorldObservation(
                blocks,
                entities
        );
    }
}