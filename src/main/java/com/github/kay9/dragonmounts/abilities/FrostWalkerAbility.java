package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;

public class FrostWalkerAbility implements Ability
{
    public static final FrostWalkerAbility INSTANCE = new FrostWalkerAbility();
    public static final Codec<FrostWalkerAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void onMove(TameableDragon dragon)
    {
        if (!dragon.level.isClientSide() && dragon.isAdult())
            FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(3 * dragon.getScale(), 1));
    }

    @Override
    public String type()
    {
        return Ability.FROST_WALKER;
    }
}
