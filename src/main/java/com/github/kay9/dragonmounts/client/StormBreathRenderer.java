package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.StormDragonBreathBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class StormBreathRenderer extends GenericFireballRenderer<StormDragonBreathBall> {

    private static final ResourceLocation TEXTURE = DragonMountsLegacy.id("textures/entity/projectiles/storm_breath.png");

    public StormBreathRenderer(EntityRendererProvider.Context context) {
        super(context, 1.5F);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull StormDragonBreathBall entity) { return TEXTURE; }
}