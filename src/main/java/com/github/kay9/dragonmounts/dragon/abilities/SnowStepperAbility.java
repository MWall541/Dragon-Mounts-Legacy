package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class SnowStepperAbility extends FootprintAbility implements Ability.Factory<SnowStepperAbility>
{
    public static final SnowStepperAbility INSTANCE = new SnowStepperAbility();
    public static final MapCodec<SnowStepperAbility> CODEC = MapCodec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var state = Blocks.SNOW.defaultBlockState();
        if (level.getBlockState(pos).isAir() && state.canSurvive(level, pos))
        {
            level.setBlockAndUpdate(pos, state);
            ((ServerLevel) level).sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    dragon.getRandom().nextInt(6) + 2,
                    0.5, 0.5, 0.5, 0);
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        var pos = dragon.blockPosition();
        return dragon.level().getBiome(pos).value().coldEnoughToSnow(pos)? 0.5f : 0;
    }

    @Override
    public SnowStepperAbility create()
    {
        return this;
    }

    @Override
    public MapCodec<? extends Factory<? extends Ability>> codec()
    {
        return CODEC;
    }
}
