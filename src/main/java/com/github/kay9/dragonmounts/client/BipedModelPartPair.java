package com.github.kay9.dragonmounts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public class BipedModelPartPair
{
    private final ModelPart rightLimb;
    private final ModelPart leftLimb;
    private final float xOffset, yOffset, zOffset;

    public BipedModelPartPair(ModelPart rightLimb, ModelPart leftLimb, float xOffset, float yOffset, float zOffset)
    {
        this.rightLimb = rightLimb;
        this.leftLimb = leftLimb;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public void render(PoseStack pPoseStack, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        rightLimb.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        leftLimb.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    public void rotate(float xRot, float yRot, float zRot)
    {
        rightLimb.xRot = xRot;
        rightLimb.yRot = yRot;
        rightLimb.zRot = zRot;

        leftLimb.xRot = rightLimb.xRot * xOffset;
        leftLimb.yRot = rightLimb.yRot * yOffset;
        leftLimb.zRot = rightLimb.zRot * zOffset;
    }
}
