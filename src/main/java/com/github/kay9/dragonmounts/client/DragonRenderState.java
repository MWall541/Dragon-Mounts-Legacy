package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

public class DragonRenderState extends LivingEntityRenderState
{
    @Nullable
    public Holder<DragonBreed> breed;
    @Nullable
    public DragonAnimator animator;
    public boolean isSaddled;
    public float maxDeathTime = 120;
    public float modelPitch;
    public float modelOffsetX;
    public float modelOffsetY;
    public float modelOffsetZ;
}
