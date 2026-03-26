package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.BlackFireDragonBreathBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BlackFireBreathRenderer extends GenericFireballRenderer<BlackFireDragonBreathBall> {

    private static final ResourceLocation TEXTURE = DragonMountsLegacy.id("textures/entity/projectiles/black_fireball.png");

    public BlackFireBreathRenderer(EntityRendererProvider.Context context) {
        super(context, 1.5F);
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull BlackFireDragonBreathBall entity) { return TEXTURE; }
}