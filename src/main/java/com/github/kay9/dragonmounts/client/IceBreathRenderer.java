package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.IceDragonBreathBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IceBreathRenderer extends GenericFireballRenderer<IceDragonBreathBall> {

    private static final ResourceLocation TEXTURE = DragonMountsLegacy.id("textures/entity/projectiles/ice_breath.png");

    public IceBreathRenderer(EntityRendererProvider.Context context) {
        super(context, 1.5F);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull IceDragonBreathBall entity) { return TEXTURE; }
}