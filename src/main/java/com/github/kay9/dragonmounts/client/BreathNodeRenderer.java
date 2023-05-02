package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.entity.breath.BreathNode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class BreathNodeRenderer extends EntityRenderer<BreathNode>
{
    private static final Random QUAD_RANDOM = new Random();

    private final ResourceLocation texture;
    private final RenderType renderType;
    private final boolean quadTexture;
    private final boolean fullBright;

    public BreathNodeRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture, boolean quadTexture, boolean fullBright)
    {
        super(ctx);
        this.texture = texture;
        this.renderType = RenderType.entityCutoutNoCull(texture);
        this.quadTexture = quadTexture;
        this.fullBright = fullBright;
    }

    @Override
    public ResourceLocation getTextureLocation(BreathNode pEntity)
    {
        return texture;
    }

    @Override
    protected int getBlockLightLevel(BreathNode pEntity, BlockPos pPos)
    {
        return fullBright? 15 : super.getBlockLightLevel(pEntity, pPos);
    }

    @Override
    public void render(BreathNode pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        var scale = pEntity.getBbWidth();

        pPoseStack.pushPose();
        pPoseStack.scale(scale, scale, scale);
        pPoseStack.mulPose(entityRenderDispatcher.cameraOrientation());

        var last = pPoseStack.last();
        var pos = last.pose();
        var normal = last.normal();
        var consumer = pBuffer.getBuffer(renderType);

        var minU = 0f;
        var minV = 0f;
        var maxU = 1f;
        var maxV = 1f;

        QUAD_RANDOM.setSeed(pEntity.getId());
        if (QUAD_RANDOM.nextBoolean()) // flip x
        {
            minU = 1f;
            maxU = 0f;
        }
        if (QUAD_RANDOM.nextBoolean()) // flip y
        {
            minV = 1f;
            maxV = 0f;
        }

        if (quadTexture)
        {
            minU *= 0.5f;
            minV *= 0.5f;
            maxU *= 0.5f;
            maxV *= 0.5f;
        }

        vertex(consumer, pos, normal, 0.5f, 1, maxU, minV, pPackedLight);
        vertex(consumer, pos, normal, 0.5f, 0, maxU, maxV, pPackedLight);
        vertex(consumer, pos, normal, -0.5f, 0, minU, maxV, pPackedLight);
        vertex(consumer, pos, normal, -0.5f, 1, minU, minV, pPackedLight);
        pPoseStack.popPose();
    }

    private static void vertex(VertexConsumer vertex, Matrix4f pos, Matrix3f normal, float x, float y, float u, float v, int lightCords)
    {
        vertex.vertex(pos, x, y, 0)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(lightCords)
                .normal(normal, 0, 1, 0)
                .endVertex();
    }
}
