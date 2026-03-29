package com.github.kay9.dragonmounts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// This mimics DragonFireballRenderer but works for ANY entity T
public abstract class GenericFireballRenderer<T extends Entity> extends EntityRenderer<T> {
    private final float scale;

    public GenericFireballRenderer(EntityRendererProvider.Context context, float scale) {
        super(context);
        this.scale = scale;
    }

    @Override
    public void render(@NotNull T entity, float yaw, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light) {
        ps.pushPose();
        // We ignore the 'light' parameter passed by the method and use FULL_BRIGHT instead
        int emissiveLight = LightTexture.FULL_BRIGHT;
        ps.scale(this.scale, this.scale, this.scale);
        ps.mulPose(this.entityRenderDispatcher.cameraOrientation());
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose lastPose = ps.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));

        // Helper to produce the "Cross" faces
        vertex(vertexconsumer, matrix4f, matrix3f, emissiveLight, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, emissiveLight, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, emissiveLight, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, matrix4f, matrix3f, emissiveLight, 0.0F, 1, 0, 0);

        ps.popPose();
        super.render(entity, yaw, partialTicks, ps, buffer, light);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light, float x, int y, int u, int v) {
        consumer.vertex(pose, x - 0.5F, (float)y - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)u, (float)v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}