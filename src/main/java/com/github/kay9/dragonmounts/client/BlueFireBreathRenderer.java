package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.BlueFireDragonBreathBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BlueFireBreathRenderer extends GenericFireballRenderer<BlueFireDragonBreathBall> {

    private static final ResourceLocation TEXTURE = DragonMountsLegacy.id("textures/entity/projectiles/blue_fireball.png");

    public BlueFireBreathRenderer(EntityRendererProvider.Context context) {
        super(context, 1.5F);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull BlueFireDragonBreathBall entity) { return TEXTURE; }
}