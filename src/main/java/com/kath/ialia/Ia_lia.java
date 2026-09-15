package com.kath.ialia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ia_lia implements ModInitializer {
	public static final String MOD_ID = "ia_lia";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("LIA ha despertado.");
		ModEntities.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					net.minecraft.commands.Commands.literal("lia")
							.executes(context -> {
								var source = context.getSource();
								var player = source.getPlayerOrException();

								LiaEntity lia = ModEntities.LIA.create(
										player.level(),
										net.minecraft.world.entity.EntitySpawnReason.COMMAND);

								if (lia != null) {
									lia.setPos(
											player.getX() + 2,
											player.getY(),
											player.getZ());

									player.level().addFreshEntity(lia);
								}

								return 1;
							}));
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
