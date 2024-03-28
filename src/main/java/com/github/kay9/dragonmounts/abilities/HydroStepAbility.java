package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.WeatheringCopper;

public class HydroStepAbility extends FootprintAbility implements Ability.Factory<HydroStepAbility>
{
    public static final HydroStepAbility INSTANCE = new HydroStepAbility();
    public static final Codec<HydroStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        ((ServerLevel) level).sendParticles(ParticleTypes.FALLING_WATER, pos.getX(), pos.getY(), pos.getZ(), 10, 0.25, 0, 0.25, 0);

        // moisten farmland
        // soak sponges
        // extinguish fire
        // magmablock -> blackstone
        // copper -> rust

        if (steppingOn.is(Blocks.FARMLAND))
        {
            level.setBlockAndUpdate(groundPos, steppingOn.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE));
            return;
        }

        if (steppingOn.is(Blocks.SPONGE))
        {
            level.setBlockAndUpdate(groundPos, Blocks.WET_SPONGE.defaultBlockState());
            return;
        }

        if (steppingOn.is(Blocks.MAGMA_BLOCK))
        {
            level.setBlockAndUpdate(groundPos, Blocks.BLACKSTONE.defaultBlockState());
            return;
        }

        var steppingOnName = steppingOn.getBlock().builtInRegistryHolder().key().location();
        if (steppingOnName.getNamespace().equals("minecraft") && steppingOnName.getPath().contains("copper")) // yeah fuck that copper complex this game's got going on
        {
            WeatheringCopper.getNext(steppingOn.getBlock()).ifPresent(b -> level.setBlockAndUpdate(groundPos, b.withPropertiesOf(steppingOn)));
            return;
        }

        if (level.getBlockState(pos).is(BlockTags.FIRE))
        {
            level.removeBlock(pos, false);
            return;
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        return 1f; // guaranteed
    }

    @Override
    public HydroStepAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return HYDRO_STEP;
    }
}
