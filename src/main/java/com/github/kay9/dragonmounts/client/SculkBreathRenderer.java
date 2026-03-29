package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.SculkDragonBreathBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SculkBreathRenderer extends GenericFireballRenderer<SculkDragonBreathBall> {

    private static final ResourceLocation TEXTURE = DragonMountsLegacy.id("textures/entity/projectiles/sculk_breath.png");

    public SculkBreathRenderer(EntityRendererProvider.Context context) {
        super(context, 1.5F);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull SculkDragonBreathBall entity) { return TEXTURE; }
}