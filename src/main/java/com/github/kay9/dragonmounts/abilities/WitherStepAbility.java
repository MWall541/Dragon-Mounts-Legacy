package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WitherStepAbility extends FootprintAbility implements Ability.Factory<WitherStepAbility>
{
    public static final WitherStepAbility INSTANCE = new WitherStepAbility();
    public static final Codec<WitherStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);
        var steppingOver = level.getBlockState(pos);

        // Corrupt the ground (Grass/Dirt -> Soul Soil)
        if (steppingOn.is(Blocks.GRASS_BLOCK) || steppingOn.is(Blocks.DIRT))
        {
            level.setBlockAndUpdate(groundPos, Blocks.SOUL_SOIL.defaultBlockState());
            serverLevel.sendParticles(ParticleTypes.ASH, groundPos.getX() + 0.5, groundPos.getY() + 1.1, groundPos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0);
        }

        // Spawn Wither Roses in empty space
        if (steppingOver.isAir())
        {
            BlockState rose = Blocks.WITHER_ROSE.defaultBlockState();
            if (rose.canSurvive(level, pos))
            {
                level.setBlockAndUpdate(pos, rose);
                level.playSound(null, pos, SoundEvents.WITHER_SHOOT, dragon.getSoundSource(), 0.1f, 0.5f);
                serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.02);
                return;
            }
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        return 0.04f;
    }

    @Override
    public WitherStepAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return WITHER_STEP;
    }
}