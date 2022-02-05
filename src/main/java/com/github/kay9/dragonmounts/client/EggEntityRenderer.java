package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.opengl.GL11;

public class EggEntityRenderer extends EntityRenderer<DragonEgg>
{
    public EggEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public void render(DragonEgg entity, float yaw, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light)
    {
        BlockPos blockpos = entity.blockPosition();
        BlockState state = DMLRegistry.EGG_BLOCK.get().defaultBlockState();

        if (state != entity.level.getBlockState(blockpos))
        {
            DragonEggRenderer.renderEgg(ps, buffer.getBuffer(RenderType.solid()), light, entity.breed, true);
//            DragonBreed transitioningBreed = entity.transitioner.transitioningBreed;
            super.render(entity, yaw, partialTicks, ps, buffer, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEgg entity)
    {
        return null;
    }
}
