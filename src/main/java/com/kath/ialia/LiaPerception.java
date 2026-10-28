package com.kath.ialia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.Comparator;

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
            entity ->
                    entity != lia
                            && canSeeEntity(lia, entity)
    );
}
private static boolean canSeeEntity(
        LiaEntity lia,
        Entity entity
) {

    var start = lia.getEyePosition();
    var end = entity.getEyePosition();

    var result = lia.level().clip(
            new net.minecraft.world.level.ClipContext(
                    start,
                    end,
                    net.minecraft.world.level.ClipContext.Block.VISUAL,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    lia
            )
    );

    return result.getType() ==
            net.minecraft.world.phys.HitResult.Type.MISS;
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
            boolean tookDamage,
            boolean playerJumping
    ) {

        boolean playerNearby = false;
        boolean animalNearby = false;
        boolean monsterNearby = false;



        boolean projectileNearby =
                isProjectileNearby(lia, distance);

        boolean shieldAvailable =
                isShieldAvailable(lia);

        boolean woodNearby =
                isWoodNearby(lia, (int) distance);

        boolean itemNearby =
                !getNearbyItems(lia, distance).isEmpty();





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
                itemNearby,
                getNearestPlayerDistance(lia, distance),
                getNearestMonsterDistance(lia, distance),
                tookDamage,
                woodNearby,
                playerJumping,
                projectileNearby,
                shieldAvailable
        );
    }

    public static double getNearestMonsterDistance(
            LiaEntity lia,
            double distance
    ) {

        Entity nearestMonster =
                getNearbyEntities(lia, distance).stream()
                        .filter(entity -> entity instanceof Monster)
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

        // Observa los bloques alrededor de LIA.
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

    public static boolean isWoodNearby(
            LiaEntity lia,
            int radius
    ) {

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

                    if (blockState.is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static List<ItemEntity> getNearbyItems(
            LiaEntity lia,
            double distance
    ) {
        return getNearbyEntities(lia, distance).stream()
                .filter(entity -> entity instanceof ItemEntity)
                .map(entity -> (ItemEntity) entity)
                .filter(item ->
                        !(item.getItem().getItem()
                                instanceof net.minecraft.world.item.ArrowItem)
                )
                .toList();
    }
    public static String identifyItem(ItemEntity itemEntity) {
        return BuiltInRegistries.ITEM
                .getKey(itemEntity.getItem().getItem())
                .getPath();
    }

    public static boolean isShieldAvailable(LiaEntity lia) {

        /* revisa las manos */
        if (lia.getItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND
        ).getItem() instanceof net.minecraft.world.item.ShieldItem) {
            return true;
        }

        if (lia.getItemInHand(
                net.minecraft.world.InteractionHand.OFF_HAND
        ).getItem() instanceof net.minecraft.world.item.ShieldItem) {
            return true;
        }

        /* revisa el inventario */
        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            if (lia.getInventory()
                    .getItem(slot)
                    .getItem()
                    instanceof net.minecraft.world.item.ShieldItem) {
                return true;
            }
        }

        return false;
    }













//busca el monstruo mas cercano y devuelve el nombre real.
    public static String getNearestMonsterType(
            LiaEntity lia,
            double distance
    ) {

        Entity nearestMonster =
                getNearbyEntities(lia, distance).stream()
                        .filter(entity -> entity instanceof Monster)
                        .min((entity1, entity2) ->
                                Double.compare(
                                        lia.distanceTo(entity1),
                                        lia.distanceTo(entity2)
                                )
                        )
                        .orElse(null);

        if (nearestMonster == null) {
            return "none";
        }

        return identifySpecificEntity(nearestMonster);
    }

// detectar proyectiles
public static boolean isProjectileNearby(
        LiaEntity lia,
        double distance
) {
    return lia.level().getEntities(
            lia,
            lia.getBoundingBox().inflate(distance),
            entity ->
                    entity instanceof net.minecraft.world.entity.projectile.Projectile
    ).stream().findAny().isPresent();
}





    public static boolean isPlayerJumping(
            LiaEntity lia,
            double distance
    ) {

        Player nearestPlayer =
                getNearbyEntities(lia, distance).stream()
                        .filter(entity -> entity instanceof Player)
                        .map(entity -> (Player) entity)
                        .min((player1, player2) ->
                                Double.compare(
                                        lia.distanceTo(player1),
                                        lia.distanceTo(player2)
                                )
                        )
                        .orElse(null);

        if (nearestPlayer == null) {
            return false;
        }


        return !nearestPlayer.onGround()
                && nearestPlayer.getDeltaMovement().y > 0;
    }







    /*  Busca el tronco más cercano dentro del radio indicado Devuelve null si no encuentra ninguno. */
    public static BlockPos getNearestLog(
            LiaEntity lia,
            int radius
    ) {
        BlockPos nearestLog = null;
        double nearestDistance = Double.MAX_VALUE;

        BlockPos origin = lia.blockPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 4; y++) {
                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos = origin.offset(x, y, z);

                    if (!lia.level().getBlockState(pos)
                            .is(BlockTags.LOGS)) {
                        continue;
                    }

                    double distance = lia.distanceToSqr(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5
                    );

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestLog = pos;
                    }
                }
            }
        }

        return nearestLog;
    }












}