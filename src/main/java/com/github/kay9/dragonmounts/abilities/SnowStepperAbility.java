package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class SnowStepperAbility extends FootprintAbility
{
    public static final SnowStepperAbility INSTANCE = new SnowStepperAbility();
    public static final Codec<Factory<SnowStepperAbility>> CODEC = Ability.singleton(SNOW_STEPPER, INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var state = Blocks.SNOW.defaultBlockState();
        if (dragon.getLevel().getBlockState(pos).isAir() && state.canSurvive(dragon.getLevel(), pos))
        {
            dragon.getLevel().setBlockAndUpdate(pos, state);
            ((ServerLevel) dragon.getLevel()).sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    dragon.getRandom().nextInt(6) + 2,
                    0.5, 0.5, 0.5, 0);
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        var pos = dragon.blockPosition();
        return dragon.getLevel().getBiome(pos).value().coldEnoughToSnow(pos)? 0.5f : 0;
    }
}
