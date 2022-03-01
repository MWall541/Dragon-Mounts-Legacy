package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;

public enum FrostWalkerAbility implements Ability
{
    INSTANCE;

    public static final Codec<FrostWalkerAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void tick(TameableDragon dragon)
    {
        if (dragon.level.isClientSide()) return;
        if (!dragon.isAdult()) return;
        if (dragon.xOld == dragon.getBlockX() && dragon.zOld == dragon.getBlockZ()) return;
        FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(5 * dragon.getScale(), 1));
    }

    @Override
    public String type()
    {
        return Ability.FROST_WALKER;
    }
}
