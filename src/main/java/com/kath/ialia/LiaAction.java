
package com.kath.ialia;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class LiaAction {

    private static final java.util.Map<LiaEntity, Integer> crouchTimers =
            new java.util.HashMap<>();

    private static final java.util.Map<LiaEntity, BlockPos> choppingTargets =
            new java.util.HashMap<>();

    private static final java.util.Map<LiaEntity, Float> choppingProgress =
            new java.util.HashMap<>();

    // Tiempo durante el que LIA buscará y recogerá los drops tras talar.
    private static final java.util.Map<LiaEntity, Integer> pickupTimers =
            new java.util.HashMap<>();

    private static final java.util.Map<LiaEntity, ItemEntity> pickupTargets =
            new java.util.HashMap<>();

    public static String execute(
            LiaEntity lia,
            LiaDecision.Action action
    ) {

        String pickedItem = null;

        switch (action) {

            case IDLE:
                break;

            case WANDER:
                /* RandomStrollGoal se encarga de este caso */
                break;

            case APPROACH_PLAYER:

                Player player = lia.level().getNearestPlayer(lia, 8);

                if (player != null) {
                    lia.getNavigation().moveTo(player, 0.7);
                }

                break;

            case CHOP_TREE:

                startChopping(lia);

                break;

            case APPROACH_ITEM:

                // Acercarse a un objeto e intentar guardarlo.
                pickedItem = pickupNearbyItem(lia);

                break;

            case REACT_APPROACH_PLAYER:

                Player approachingPlayer =
                        lia.level().getNearestPlayer(lia, 8);

                if (approachingPlayer != null) {

                    Ia_lia.LOGGER.info(
                            "LIA reacciona al salto acercándose al jugador."
                    );

                    lia.getNavigation().moveTo(
                            approachingPlayer,
                            0.7
                    );
                }

                break;

            case REACT_FLEE_PLAYER:

                Player fleeingPlayer =
                        lia.level().getNearestPlayer(lia, 8);

                if (fleeingPlayer != null) {

                    Ia_lia.LOGGER.info(
                            "LIA reacciona al salto alejándose del jugador."
                    );

                    double dx = lia.getX() - fleeingPlayer.getX();
                    double dz = lia.getZ() - fleeingPlayer.getZ();

                    double distance = Math.sqrt(dx * dx + dz * dz);

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

            case REACT_IDLE:

                Ia_lia.LOGGER.info(
                        "LIA reacciona al salto quedándose quieta."
                );

                lia.getNavigation().stop();

                break;

            case JUMP:

                Ia_lia.LOGGER.info(
                        "LIA decidió saltar por su cuenta."
                );

                lia.getJumpControl().jump();

                break;

            case BLOCK:

                blockWithShield(lia);

                break;

            case HIDE:

                hideFromMonster(lia);

                break;

            case CROUCH:

                crouch(lia);

                break;

            case FLEE_MONSTER:

                Entity monster = getNearestMonster(lia);

                if (monster != null) {

                    double dx = lia.getX() - monster.getX();
                    double dz = lia.getZ() - monster.getZ();

                    double distance = Math.sqrt(dx * dx + dz * dz);

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

            case ATTACK_MONSTER:

                attackMonster(lia);

                break;
        }

        return pickedItem;
    }

    /*busca objetos cercanos a lia*/

    private static String pickupNearbyItem(LiaEntity lia) {

        if (!(lia.level() instanceof ServerLevel)) {
            return null;
        }


        ItemEntity item = LiaPerception.getNearbyItems(lia, 8)
                .stream()
                .min((item1, item2) ->
                        Double.compare(
                                lia.distanceTo(item1),
                                lia.distanceTo(item2)
                        )
                )
                .orElse(null);

        if (item == null || !item.isAlive()) {
            pickupTargets.remove(lia);
            return null;
        }

        double distance = lia.distanceTo(item);

        String itemName = LiaPerception.identifyItem(item);

        // Si todavía está lejos, acercarse sin reiniciar
        // la ruta en cada tick.
        if (distance > 2.0) {

            ItemEntity currentTarget = pickupTargets.get(lia);

            if (currentTarget != item || lia.getNavigation().isDone()) {

                lia.getNavigation().moveTo(item, 1.0);
                pickupTargets.put(lia, item);

                Ia_lia.LOGGER.info(
                        "LIA se acerca a {} | Distancia: {}",
                        itemName,
                        distance
                );
            }

            return null;
        }

        // Ya está suficientemente cerca para recogerlo.
        lia.getNavigation().stop();
        pickupTargets.remove(lia);

        ItemStack groundStack = item.getItem();

        if (groundStack.isEmpty()) {
            return null;
        }

        String displayName = groundStack.getHoverName().getString();


        int currentCount = 0;

        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            ItemStack existing = lia.getInventory().getItem(slot);

            if (!existing.isEmpty()
                    && ItemStack.isSameItem(existing, groundStack)) {

                currentCount += existing.getCount();
            }
        }

// Máximo de 64 unidades de este objeto en total.
        int desiredCount = 64;
        int allowedCount = Math.max(0, desiredCount - currentCount);

        if (allowedCount == 0) {
            return null;
        }






        ItemStack toStore = groundStack.copy();

        toStore.setCount(
                Math.min(allowedCount, groundStack.getCount())
        );

        int storedCount = lia.addItemToInventory(toStore);

        if (storedCount > 0) {

            // Quitar del suelo únicamente lo que se guardó.
            groundStack.shrink(storedCount);

            Ia_lia.LOGGER.info(
                    "LIA guardó {} de {}. Total: {}.",
                    storedCount,
                    displayName,
                    currentCount + storedCount
            );

            sendMessage(
                    lia,
                    "He recogido " + displayName + "."
            );

            if (groundStack.isEmpty()) {
                item.discard();
            }

            return displayName;
        }

// No se pudo guardar ningún objeto.
        return null;

    }


    /* Busca el monstruo más cercano. */
    private static Entity getNearestMonster(LiaEntity lia) {

        return lia.level().getEntities(
                        lia,
                        lia.getBoundingBox().inflate(8),
                        entity -> entity instanceof Monster
                )
                .stream()
                .min((entity1, entity2) ->
                        Double.compare(
                                lia.distanceTo(entity1),
                                lia.distanceTo(entity2)
                        )
                )
                .orElse(null);
    }

    /* Intenta atacar al monstruo. */
    private static void attackMonster(LiaEntity lia) {

        Entity monster = getNearestMonster(lia);

        if (monster == null) {
            return;
        }

        /* Primero revisa si ya tiene un arma en la mano. */
        ItemStack weapon = lia.getItemInHand(InteractionHand.MAIN_HAND);

        if (weapon.isEmpty()
                || !isWeapon(weapon.getItem().toString())) {

            int weaponSlot = findWeapon(lia);

            if (weaponSlot == -1) {

                Ia_lia.LOGGER.info(
                        "LIA quiso atacar pero no tiene un arma."
                );

                sendMessage(
                        lia,
                        "Quiero atacar pero no tengo un arma."
                );

                return;
            }

            weapon = lia.getInventory().removeItem(weaponSlot, 1);

            lia.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    weapon
            );

            Ia_lia.LOGGER.info(
                    "LIA equipó {} para defenderse.",
                    weapon.getHoverName().getString()
            );
        }

        double distance = lia.distanceTo(monster);

        /* Se acerca si está lejos. */
        if (distance > 2.5) {

            Ia_lia.LOGGER.info(
                    "LIA se acerca al monstruo para atacar."
            );

            lia.getNavigation().moveTo(monster, 0.8);

            return;
        }

        /* Ataca. */
        boolean hit = lia.doHurtTarget(
                (ServerLevel) lia.level(),
                monster
        );

        lia.setAttackSuccessful(hit);

        if (hit && !monster.isAlive()) {

            lia.setMonsterKilled(true);

            Ia_lia.LOGGER.info(
                    "LIA eliminó al monstruo."
            );

            sendMessage(lia, "Lo logré. Lo derroté.");

        } else if (hit) {

            Ia_lia.LOGGER.info(
                    "LIA golpeó al monstruo."
            );

            sendMessage(lia, "Le di.");

        } else {

            Ia_lia.LOGGER.info(
                    "LIA falló el ataque."
            );

            sendMessage(lia, "Fallé... Upss");
        }
    }

    /* Busca un arma en el inventario. */
    private static int findWeapon(LiaEntity lia) {

        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            ItemStack stack = lia.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            String itemName = stack.getItem().toString();

            if (isWeapon(itemName)) {
                return slot;
            }
        }

        return -1;
    }

    /* Comprueba si es un arma. */
    private static boolean isWeapon(String itemName) {

        return itemName.endsWith("_sword")
                || itemName.endsWith("_axe")
                || itemName.equals("mace")
                || itemName.equals("trident");
    }

    /* Hace que LIA se agache. */
    private static void crouch(LiaEntity lia) {

        lia.setPose(
                net.minecraft.world.entity.Pose.CROUCHING
        );

        crouchTimers.put(lia, 20);

        Ia_lia.LOGGER.info(
                "LIA decidió agacharse."
        );
    }

    /* Actualiza las tareas continuas de LIA cada tick. */
    public static void tick(LiaEntity lia) {

        // Actualizar el tiempo que permanece agachada.
        Integer timer = crouchTimers.get(lia);

        if (timer != null) {

            if (timer <= 0) {

                lia.setPose(
                        net.minecraft.world.entity.Pose.STANDING
                );

                crouchTimers.remove(lia);

            } else {

                crouchTimers.put(lia, timer - 1);
            }
        }

        // Continuar la tala aunque LIA no esté agachada.
        tickChopping(lia);

        // Después de talar, buscar y recoger los objetos caídos.
        tickAutoPickup(lia);
    }

    /*
     * Mantiene activa la recogida durante un tiempo después
     * de romper un tronco.
     */
    private static void tickAutoPickup(LiaEntity lia) {

        Integer timer = pickupTimers.get(lia);

        if (timer == null) {
            return;
        }

        if (timer <= 0) {
            pickupTimers.remove(lia);
            return;
        }

        pickupTimers.put(lia, timer - 1);

        // Reutilizar el mismo sistema de recogida del inventario.
        pickupNearbyItem(lia);
    }

    /* Intenta protegerse con un escudo. */
    private static void blockWithShield(LiaEntity lia) {

        ItemStack offHand =
                lia.getItemInHand(InteractionHand.OFF_HAND);

        /* Si ya tiene un escudo en la mano. */
        if (!offHand.isEmpty()
                && offHand.getItem()
                instanceof net.minecraft.world.item.ShieldItem) {

            lia.startUsingItem(InteractionHand.OFF_HAND);

            Ia_lia.LOGGER.info(
                    "LIA levantó el escudo para protegerse."
            );

            sendMessage(lia, "Voy a protegerme.");

            return;
        }

        /* Busca un escudo en el inventario. */
        int shieldSlot = findShield(lia);

        if (shieldSlot == -1) {

            Ia_lia.LOGGER.info(
                    "LIA quiso protegerse pero no tiene un escudo."
            );

            return;
        }

        ItemStack shield =
                lia.getInventory().removeItem(shieldSlot, 1);

        lia.setItemInHand(
                InteractionHand.OFF_HAND,
                shield
        );

        lia.startUsingItem(InteractionHand.OFF_HAND);

        Ia_lia.LOGGER.info(
                "LIA equipó un escudo y se protegió."
        );

        sendMessage(
                lia,
                "Tengo un escudo. Voy a protegerme."
        );
    }

    // Dejar de usar el escudo.
    public static void stopBlocking(LiaEntity lia) {

        if (lia.isUsingItem()) {

            lia.stopUsingItem();

            Ia_lia.LOGGER.info(
                    "LIA bajó el escudo."
            );
        }
    }

    /* Busca una posición protegida del monstruo. */
    private static void hideFromMonster(LiaEntity lia) {

        Entity monster = getNearestMonster(lia);

        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };

        BlockPos bestPosition = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        var projectiles = lia.level().getEntities(
                lia,
                lia.getBoundingBox().inflate(12),
                entity -> entity instanceof
                        net.minecraft.world.entity.projectile.Projectile
        );

        for (int[] direction : directions) {

            for (int distance = 2; distance <= 6; distance++) {

                double targetX =
                        lia.getX() + direction[0] * distance;

                double targetZ =
                        lia.getZ() + direction[1] * distance;

                BlockPos targetPos = BlockPos.containing(
                        targetX,
                        lia.getY(),
                        targetZ
                );

                if (!lia.level().getBlockState(targetPos).isAir()
                        || !lia.level().getBlockState(targetPos.above()).isAir()) {
                    continue;
                }

                if (!lia.level().getBlockState(targetPos.below()).isSolid()) {
                    continue;
                }

                double score = 0;

                if (monster != null) {

                    if (hasLineOfSight(lia, targetPos, monster)) {
                        score -= 100;
                    } else {
                        score += 10;
                    }

                    score += lia.distanceToSqr(
                            net.minecraft.world.phys.Vec3.atCenterOf(targetPos)
                    ) * 0.1;
                }

                for (Entity projectile : projectiles) {

                    if (hasLineOfSight(lia, targetPos, projectile)) {
                        score -= 20;
                    } else {
                        score += 5;
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestPosition = targetPos;
                }
            }
        }

        if (bestPosition != null && bestScore > 0) {

            lia.getNavigation().moveTo(
                    bestPosition.getX() + 0.5,
                    bestPosition.getY(),
                    bestPosition.getZ() + 0.5,
                    1.0
            );

            Ia_lia.LOGGER.info(
                    "LIA intenta buscar cobertura contra un proyectil o monstruo."
            );

            return;
        }

        if (monster != null) {
            fleeIfNoCover(lia, monster);
            return;
        }

        /* Sin cobertura ni atacante visible, intenta alejarse del proyectil. */
        Entity nearestProjectile = projectiles.stream()
                .min((a, b) -> Double.compare(
                        lia.distanceToSqr(a),
                        lia.distanceToSqr(b)
                ))
                .orElse(null);

        if (nearestProjectile != null) {

            double dx = lia.getX() - nearestProjectile.getX();
            double dz = lia.getZ() - nearestProjectile.getZ();

            double length = Math.sqrt(dx * dx + dz * dz);

            if (length > 0.001) {

                lia.getNavigation().moveTo(
                        lia.getX() + dx / length * 8,
                        lia.getY(),
                        lia.getZ() + dz / length * 8,
                        1.1
                );
            }

        } else {

            lia.getNavigation().stop();
        }
    }

    /* Comprueba si un bloque tapa la visión hacia una entidad. */
    private static boolean hasLineOfSight(
            LiaEntity lia,
            BlockPos position,
            Entity target
    ) {

        var start = net.minecraft.world.phys.Vec3
                .atCenterOf(position)
                .add(0, 0.5, 0);

        var end = target.getEyePosition();

        var result = lia.level().clip(
                new net.minecraft.world.level.ClipContext(
                        start,
                        end,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        lia
                )
        );

        return result.getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    /* Busca un escudo en el inventario. */
    private static int findShield(LiaEntity lia) {

        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            ItemStack stack = lia.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem()
                    instanceof net.minecraft.world.item.ShieldItem) {

                return slot;
            }
        }

        return -1;
    }

    /* Huye si no encontró dónde esconderse. */
    private static void fleeIfNoCover(
            LiaEntity lia,
            Entity monster
    ) {

        double dx = lia.getX() - monster.getX();
        double dz = lia.getZ() - monster.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance <= 0) {
            return;
        }

        double targetX =
                lia.getX() + (dx / distance) * 8;

        double targetZ =
                lia.getZ() + (dz / distance) * 8;

        lia.getNavigation().moveTo(
                targetX,
                lia.getY(),
                targetZ,
                1.2
        );
    }

    /* Muestra un mensaje al jugador cercano. */
    private static void sendMessage(
            LiaEntity lia,
            String message
    ) {

        Player player = lia.level().getNearestPlayer(lia, 16);

        if (player != null) {

            player.sendSystemMessage(
                    Component.literal("LIA: " + message)
            );
        }
    }

    /* Inicia o continúa la tarea de talar un árbol. */
    private static void startChopping(LiaEntity lia) {

        BlockPos target = choppingTargets.get(lia);

        // Si no hay un objetivo válido, buscar otro tronco.
        if (target == null
                || !lia.level().getBlockState(target).is(BlockTags.LOGS)) {

            target = findNearestLog(lia, 8);

            if (target == null) {

                choppingTargets.remove(lia);
                choppingProgress.remove(lia);

                lia.getNavigation().stop();

                return;
            }

            choppingTargets.put(lia, target);
            choppingProgress.put(lia, 0.0F);

            Ia_lia.LOGGER.info(
                    "LIA encontró un tronco en {}",
                    target
            );
        }

        double distance = lia.distanceToSqr(
                target.getX() + 0.5,
                target.getY() + 0.5,
                target.getZ() + 0.5
        );

        // Acercarse al tronco antes de intentar romperlo.
        if (distance > 16.0) {

            lia.getNavigation().moveTo(
                    target.getX() + 0.5,
                    target.getY(),
                    target.getZ() + 0.5,
                    0.8
            );

        } else {

            lia.getNavigation().stop();
        }
    }

    /* Busca el tronco más cercano dentro del radio. */
    private static BlockPos findNearestLog(
            LiaEntity lia,
            int radius
    ) {

        BlockPos origin = lia.blockPosition();

        BlockPos nearest = null;

        double nearestDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {

            for (int y = -2; y <= 4; y++) {

                for (int z = -radius; z <= radius; z++) {

                    BlockPos pos = origin.offset(x, y, z);

                    if (!lia.level().getBlockState(pos).is(BlockTags.LOGS)) {
                        continue;
                    }

                    double distance = lia.distanceToSqr(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5
                    );

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = pos;
                    }
                }
            }
        }

        return nearest;
    }

    /* Actualiza el progreso de tala en cada tick del servidor. */
    private static void tickChopping(LiaEntity lia) {

        BlockPos target = choppingTargets.get(lia);

        if (target == null) {
            return;
        }

        if (!(lia.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockState state = serverLevel.getBlockState(target);

        // El tronco desapareció o dejó de ser un bloque de madera.
        if (!state.is(BlockTags.LOGS)) {

            choppingTargets.remove(lia);
            choppingProgress.remove(lia);

            return;
        }

        double distance = lia.distanceToSqr(
                target.getX() + 0.5,
                target.getY() + 0.5,
                target.getZ() + 0.5
        );

        // Todavía está lejos: continuar acercándose.
        if (distance > 16.0) {

            lia.getNavigation().moveTo(
                    target.getX() + 0.5,
                    target.getY(),
                    target.getZ() + 0.5,
                    0.8
            );

            return;
        }

        lia.getNavigation().stop();


        float progress = 0.05F;


        float total = choppingProgress.getOrDefault(lia, 0.0F);

        total += progress;

        choppingProgress.put(lia, total);

        // Animar el brazo mientras tala.
        if (((int) (total * 20) % 5) == 0) {
            lia.swing(
                    InteractionHand.MAIN_HAND,
                    net.minecraft.world.item.component.SwingAnimation.DEFAULT
            );
        }


        if (total >= 1.0F) {


            /* particulas x*/
            serverLevel.levelEvent(
                    2001,
                    target,
                    net.minecraft.world.level.block.Block.getId(state)
            );


            // Romper el tronco y generar sus drops.
            boolean destroyed = serverLevel.destroyBlock(
                    target,
                    true,
                    lia
            );

            choppingTargets.remove(lia);
            choppingProgress.remove(lia);

            if (destroyed) {

                // Buscar los objetos caídos durante los siguientes 5 segundos.
                pickupTimers.put(lia, 200);



                sendMessage(lia, "He talado un tronco.");
            }
        }
    }
}
