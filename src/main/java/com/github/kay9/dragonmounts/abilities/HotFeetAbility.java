package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class HotFeetAbility extends FootprintAbility implements Ability.Factory<HotFeetAbility>
{
    public static final HotFeetAbility INSTANCE = new HotFeetAbility();
    public static final Codec<HotFeetAbility> CODEC = Codec.unit(INSTANCE);

    public static final TagKey<Block> BURNABLES_TAG = BlockTags.create(DragonMountsLegacy.id("hot_feet_burnables"));

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var groundPos = pos.below(); // Check the block the dragon is standing ON
        var steppingOn = level.getBlockState(groundPos);

        if (level instanceof ServerLevel serverLevel) {
            // Always spawn a few flame/smoke particles to show the heat
            serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 2, 0.1, 0.01, 0.1, 0.02);

            // Burnables Logic (Leaves, flowers, etc. defined in your tag)
            var posState = level.getBlockState(pos); // Check the block AT the feet (like fire/grass)
            if (posState.is(BURNABLES_TAG)) {
                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.1f, 1.5f);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
                return; // Exit early if we already processed a block
            }

            // Wet Sponge -> Sponge
            if (steppingOn.is(Blocks.WET_SPONGE)) {
                level.setBlockAndUpdate(groundPos, Blocks.SPONGE.defaultBlockState());
                level.playSound(null, groundPos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.2f, 1.0f);
                serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.05);
                return;
            }

            // Blackstone -> Magma Block
            if (steppingOn.is(Blocks.BLACKSTONE)) {
                level.setBlockAndUpdate(groundPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                level.playSound(null, groundPos, SoundEvents.FIRECHARGE_USE, dragon.getSoundSource(), 0.2f, 0.8f);
                return;
            }
        }
    }

    @Override
    public HotFeetAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return HOT_FEET;
    }
}
