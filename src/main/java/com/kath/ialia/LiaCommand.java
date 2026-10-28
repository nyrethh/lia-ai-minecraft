package com.kath.ialia;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;

public class LiaCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext, environment) -> {

                    dispatcher.register(
                            Commands.literal("lia")

                                    /* /lia give */
                                    .then(
                                            Commands.literal("give")
                                                    .then(
                                                            Commands.argument(
                                                                            "item",
                                                                            StringArgumentType.word()
                                                                    )
                                                                    .executes(context -> {

                                                                        String itemName =
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "item"
                                                                                );

                                                                        return giveItem(
                                                                                context.getSource(),
                                                                                itemName
                                                                        );
                                                                    })
                                                    )
                                    )

                                    /* /lia come  here*/
                                    .then(
                                            Commands.literal("come")
                                                    .executes(context ->
                                                            bringLia(
                                                                    context.getSource()
                                                            )
                                                    )
                                    )

                                    /* /lia inventory */
                                    .then(
                                            Commands.literal("inventory")
                                                    .executes(context ->
                                                            showInventory(
                                                                    context.getSource()
                                                            )
                                                    )
                                    )

                                    /* /lia clear */

                                    .then(
                                            Commands.literal("clear")
                                                    .executes(context ->
                                                            clearInventory(context.getSource())
                                                    )
                                    )

                    );
                }
        );
    }

    /* le da un objeto a Lia */
    private static int giveItem(
            net.minecraft.commands.CommandSourceStack source,
            String itemName
    ) {

        ServerLevel level =
                source.getLevel();

        if (source.getEntity() == null) {

            source.sendFailure(
                    Component.literal(
                            "Este comando debe ejecutarlo un jugador."
                    )
            );

            return 0;
        }

        var lias =
                level.getEntitiesOfClass(
                        LiaEntity.class,
                        source.getEntity()
                                .getBoundingBox()
                                .inflate(16)
                );

        if (lias.isEmpty()) {

            source.sendFailure(
                    Component.literal(
                            "No hay una LIA cerca."
                    )
            );

            return 0;
        }

        LiaEntity lia =
                lias.stream()
                        .min((lia1, lia2) ->
                                Double.compare(
                                        source.getEntity()
                                                .distanceTo(lia1),
                                        source.getEntity()
                                                .distanceTo(lia2)
                                )
                        )
                        .orElse(null);

        if (lia == null) {

            source.sendFailure(
                    Component.literal(
                            "No encontre a LIA."
                    )
            );

            return 0;
        }

        Identifier id;

        try {

            id = Identifier.parse(
                    itemName.contains(":")
                            ? itemName
                            : "minecraft:" + itemName
            );

        } catch (Exception e) {

            source.sendFailure(
                    Component.literal(
                            "Ese objeto no existe."
                    )
            );

            return 0;
        }

        var itemReference =
                BuiltInRegistries.ITEM.get(id);

        if (itemReference.isEmpty()) {

            source.sendFailure(
                    Component.literal(
                            "No encontre el objeto: "
                                    + itemName
                    )
            );

            return 0;
        }

        Item item =
                itemReference.get().value();

        ItemStack stack =
                new ItemStack(item);

        ItemStack remaining =
                lia.getInventory().addItem(stack);

        if (!remaining.isEmpty()) {

            source.sendFailure(
                    Component.literal(
                            "El inventario de LIA esta lleno."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Le diste "
                                + itemName
                                + " a LIA."
                ),
                false
        );

        Ia_lia.LOGGER.info(
                "Comando /lia give | LIA recibio: {}",
                itemName
        );

        return 1;
    }

    /* muestra el inventario de Lia */
    private static int showInventory(
            net.minecraft.commands.CommandSourceStack source
    ) {

        if (source.getEntity() == null) {

            source.sendFailure(
                    Component.literal(
                            "Este comando debe ejecutarlo un jugador."
                    )
            );

            return 0;
        }

        ServerLevel level =
                source.getLevel();

        LiaEntity lia =
                level.getEntitiesOfClass(
                                LiaEntity.class,
                                source.getEntity()
                                        .getBoundingBox()
                                        .inflate(16)
                        )
                        .stream()
                        .min((lia1, lia2) ->
                                Double.compare(
                                        source.getEntity()
                                                .distanceTo(lia1),
                                        source.getEntity()
                                                .distanceTo(lia2)
                                )
                        )
                        .orElse(null);

        if (lia == null) {

            source.sendFailure(
                    Component.literal(
                            "No encontre a LIA."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "=== Inventario de LIA ==="
                ),
                false
        );

        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            ItemStack stack =
                    lia.getInventory().getItem(slot);

            String itemName;

            if (stack.isEmpty()) {
                itemName = "vacio";
            } else {
                itemName =
                        stack.getItem().toString()
                                + " x"
                                + stack.getCount();
            }

            final int currentSlot = slot;
            final String currentItemName = itemName;

            source.sendSuccess(
                    () -> Component.literal(
                            "Slot "
                                    + currentSlot
                                    + ": "
                                    + currentItemName
                    ),
                    false
            );
        }

        return 1;
    }

    /* trae a Lia hasta el jugador */
    private static int bringLia(
            net.minecraft.commands.CommandSourceStack source
    ) {

        if (source.getEntity() == null) {

            source.sendFailure(
                    Component.literal(
                            "Este comando debe ejecutarlo un jugador."
                    )
            );

            return 0;
        }

        ServerLevel level =
                source.getLevel();

        LiaEntity lia =
                level.getEntitiesOfClass(
                                LiaEntity.class,
                                source.getEntity()
                                        .getBoundingBox()
                                        .inflate(1000)
                        )
                        .stream()
                        .min((lia1, lia2) ->
                                Double.compare(
                                        source.getEntity()
                                                .distanceTo(lia1),
                                        source.getEntity()
                                                .distanceTo(lia2)
                                )
                        )
                        .orElse(null);

        if (lia == null) {

            source.sendFailure(
                    Component.literal(
                            "No encontre a LIA."
                    )
            );

            return 0;
        }

        lia.teleportTo(
                source.getPosition().x(),
                source.getPosition().y(),
                source.getPosition().z()
        );

        source.sendSuccess(
                () -> Component.literal(
                        "LIA regreso contigo."
                ),
                false
        );

        Ia_lia.LOGGER.info(
                "LIA fue traida hasta el jugador."
        );

        return 1;
    }

    /* Vacía el inventario de LIA y entrega los objetos al jugador */
    private static int clearInventory(
            net.minecraft.commands.CommandSourceStack source
    ) {

        if (source.getEntity() == null) {
            source.sendFailure(
                    Component.literal(
                            "Este comando debe ejecutarlo un jugador."
                    )
            );
            return 0;
        }

        var player = source.getPlayer();

        if (player == null) {
            source.sendFailure(
                    Component.literal(
                            "Este comando debe ejecutarlo un jugador."
                    )
            );
            return 0;
        }

        ServerLevel level = source.getLevel();

        LiaEntity lia = level.getEntitiesOfClass(
                        LiaEntity.class,
                        player.getBoundingBox().inflate(16)
                )
                .stream()
                .min((lia1, lia2) ->
                        Double.compare(
                                player.distanceTo(lia1),
                                player.distanceTo(lia2)
                        )
                )
                .orElse(null);

        if (lia == null) {
            source.sendFailure(
                    Component.literal(
                            "No encontre a LIA cerca."
                    )
            );
            return 0;
        }

        int transferred = 0;
        int dropped = 0;

        for (int slot = 0;
             slot < lia.getInventory().getContainerSize();
             slot++) {

            ItemStack stack = lia.getInventory().getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            // Copia los objetos para conservarlos durante la transferencia.
            ItemStack remaining = stack.copy();

            // Intenta añadirlos al inventario del jugador.
            player.getInventory().add(remaining);

            // Calcula cuántos objetos pudo recibir el jugador.
            int given = stack.getCount() - remaining.getCount();
            transferred += given;

            // Si quedan objetos, los suelta en la posición de LIA.
            if (!remaining.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        lia.getX(),
                        lia.getY(),
                        lia.getZ(),
                        remaining.copy()
                );

                if (level.addFreshEntity(itemEntity)) {
                    dropped += remaining.getCount();
                } else {
                    // Si no se pudo crear la entidad, conserva los objetos en LIA.
                    continue;
                }
            }

            // Vacía el slot solamente si los objetos fueron transferidos
            // o la entidad de objetos se creó correctamente.
            lia.getInventory().setItem(slot, ItemStack.EMPTY);
        }

        final int totalTransferred = transferred;
        final int totalDropped = dropped;

        source.sendSuccess(
                () -> Component.literal(
                        "Inventario de LIA vaciado. "
                                + totalTransferred
                                + " objetos entregados a tu inventario y "
                                + totalDropped
                                + " objetos dejados en el suelo."
                ),
                false
        );

        Ia_lia.LOGGER.info(
                "Inventario de LIA vaciado. Entregados: {}, dejados en el suelo: {}",
                totalTransferred,
                totalDropped
        );

        return 1;
    }



}