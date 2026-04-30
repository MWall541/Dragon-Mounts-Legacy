package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.WeatheringCopper;

public class ElectroStepAbility extends FootprintAbility implements Ability.Factory<ElectroStepAbility>
{
    public static final ElectroStepAbility INSTANCE = new ElectroStepAbility();
    public static final Codec<ElectroStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX(), pos.getY(), pos.getZ(), 5, 0.25, 0, 0.25, 0);

            WeatheringCopper.getPrevious(steppingOn.getBlock()).ifPresent(previousBlock -> {
                level.setBlockAndUpdate(groundPos, previousBlock.withPropertiesOf(steppingOn));
                level.playSound(null, pos, SoundEvents.COPPER_HIT, dragon.getSoundSource(), 0.1f, 2f);
                serverLevel.sendParticles(ParticleTypes.WAX_ON, pos.getX(), pos.getY(), pos.getZ(), 5, 0.1, 0.1, 0.1, 0.05);
            });
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        return 1f; // guaranteed
    }

    @Override
    public ElectroStepAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return ELECTRO_STEP;
    }
}
