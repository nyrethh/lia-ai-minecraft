package com.kath.ialia;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<LiaEntity> LIA = EntityType.Builder
            .of(LiaEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.8f)
            .build(ResourceKey.create(
                    BuiltInRegistries.ENTITY_TYPE.key(),
                    Identifier.fromNamespaceAndPath(Ia_lia.MOD_ID, "lia")
            ));

    public static void register() {
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Ia_lia.MOD_ID, "lia"),
                LIA
        );

        FabricDefaultAttributeRegistry.register(
                LIA,
                LiaEntity.createMobAttributes().build()
        );

        Ia_lia.LOGGER.info("LIA ha sido registrada.");
    }
}