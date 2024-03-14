package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.WeatheringCopper;

public class HydroStepAbility extends FootprintAbility
{
    public static final HydroStepAbility INSTANCE = new HydroStepAbility();
    public static final Codec<Factory<HydroStepAbility>> CODEC = Ability.singleton(HYDRO_STEP, INSTANCE);

    // moisten farmland
    // soak sponges
    // extinguish fire
    // magmablock -> blackstone
    // copper -> rust

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.getLevel();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        if (steppingOn.is(Blocks.FARMLAND))
        {
            level.setBlock(groundPos, steppingOn.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), FarmBlock.UPDATE_CLIENTS);
            return;
        }

        if (steppingOn.is(Blocks.SPONGE))
        {
            level.setBlockAndUpdate(groundPos, Blocks.WET_SPONGE.defaultBlockState()); // places new block, have to update all?
            return;
        }

        if (steppingOn.is(Blocks.MAGMA_BLOCK))
        {
            level.setBlockAndUpdate(groundPos, Blocks.BLACKSTONE.defaultBlockState());
            return;
        }

        var steppingOnName = steppingOn.getBlock().getRegistryName();
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
}
