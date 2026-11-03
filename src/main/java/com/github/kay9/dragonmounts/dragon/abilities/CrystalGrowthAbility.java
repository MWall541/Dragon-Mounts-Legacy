package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;

public class CrystalGrowthAbility extends FootprintAbility implements Ability.Factory<CrystalGrowthAbility>
{
    public static final CrystalGrowthAbility INSTANCE = new CrystalGrowthAbility();
    public static final MapCodec<CrystalGrowthAbility> CODEC = MapCodec.unit(INSTANCE);

    // budding_amethyst is a (slow) renewable shard source, so it's kept much rarer than
    // the plain amethyst_block outcome to avoid turning this into an easy farm.
    private static final float BUDDING_CHANCE = 0.01f;

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        if (steppingOn.is(Blocks.STONE) || steppingOn.is(Blocks.DEEPSLATE))
        {
            boolean budding = dragon.getRandom().nextFloat() < BUDDING_CHANCE;
            level.setBlockAndUpdate(groundPos, (budding? Blocks.BUDDING_AMETHYST : Blocks.AMETHYST_BLOCK).defaultBlockState());

            if (budding)
            {
                level.playSound(null, groundPos, SoundEvents.AMETHYST_CLUSTER_PLACE, dragon.getSoundSource(), 0.6f, 0.8f);
                ((ServerLevel) level).sendParticles(ParticleTypes.REVERSE_PORTAL,
                        groundPos.getX() + 0.5, groundPos.getY() + 1, groundPos.getZ() + 0.5,
                        12, 0.3, 0.2, 0.3, 0.05);
            }
            else
            {
                level.playSound(null, groundPos, SoundEvents.AMETHYST_BLOCK_CHIME, dragon.getSoundSource(), 0.4f, 1f);
                ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD,
                        groundPos.getX() + 0.5, groundPos.getY() + 1, groundPos.getZ() + 0.5,
                        3, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        return 0.03f; // rarer than default: this permanently changes terrain, not just decoration
    }

    @Override
    public CrystalGrowthAbility create()
    {
        return this;
    }

    @Override
    public MapCodec<? extends Factory<? extends Ability>> codec()
    {
        return CODEC;
    }
}
