package com.github.kay9.dragonmounts.mixins.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartAccess
{
    @Unique public float dm_xScale = 1;
    @Unique public float dm_yScale = 1;
    @Unique public float dm_zScale = 1;

    @Inject(method = "translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(value = "TAIL"))
    public void scalePoseStack(PoseStack pPoseStack, CallbackInfo cbi)
    {
        pPoseStack.scale(dm_xScale, dm_yScale, dm_zScale);
    }

    @Override
    public float getXScale()
    {
        return dm_xScale;
    }

    @Override
    public float getYScale()
    {
        return dm_yScale;
    }

    @Override
    public float getZScale()
    {
        return dm_zScale;
    }

    @Override
    public void setXScale(float x)
    {
        this.dm_xScale = x;
    }

    @Override
    public void setYScale(float y)
    {
        this.dm_yScale = y;
    }

    @Override
    public void setZScale(float z)
    {
        this.dm_zScale = z;
    }
}
