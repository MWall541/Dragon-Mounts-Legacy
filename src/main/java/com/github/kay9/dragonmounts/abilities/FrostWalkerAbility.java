package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.function.Supplier;

public class FrostWalkerAbility implements Ability
{
    // memoize: we only ever need one instance, data is constant!
    public static final Codec<Supplier<FrostWalkerAbility>> CODEC = Codec.FLOAT
            .xmap(f -> Suppliers.memoize(() -> new FrostWalkerAbility(f)), a -> a.get().radiusMultiplier);

    private final float radiusMultiplier;

    public FrostWalkerAbility(float radiusMultiplier)
    {
        this.radiusMultiplier = radiusMultiplier;
    }

    @Override
    public void initialize(TameableDragon dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, 0);
    }

    @Override
    public void close(TameableDragon dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, BlockPathTypes.WATER.getMalus());
    }

    @Override
    public void onMove(TameableDragon dragon)
    {
        if (!dragon.level.isClientSide() && dragon.isAdult())
            FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(radiusMultiplier * dragon.getScale(), 1));
    }
}
