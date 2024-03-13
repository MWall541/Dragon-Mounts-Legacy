package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.function.Supplier;


public class FrostWalkerAbility implements Ability
{
    public static final Codec<Factory> CODEC = Codec.FLOAT
            .xmap(Factory::new, Factory::radiusMultiplier)
            .fieldOf("radius_multiplier")
            .codec();

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

    public record Factory(float radiusMultiplier) implements Ability.Factory<FrostWalkerAbility>
    {
        @Override
        public FrostWalkerAbility create()
        {
            return new FrostWalkerAbility(radiusMultiplier());
        }

        @Override
        public ResourceLocation type()
        {
            return FROST_WALKER;
        }
    }
}
