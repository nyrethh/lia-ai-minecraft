package com.kath.ialia.client;

import com.kath.ialia.ModEntities;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class Ia_liaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(
                ModEntities.LIA,
                context -> new HumanoidMobRenderer<>(
                        context,
                        new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                        0.5f
                ) {
                    @Override
                    public HumanoidRenderState createRenderState() {
                        return new HumanoidRenderState();
                    }

                    @Override
                    public Identifier getTextureLocation(HumanoidRenderState state) {
                        return Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");
                    }
                }
        );
    }
}