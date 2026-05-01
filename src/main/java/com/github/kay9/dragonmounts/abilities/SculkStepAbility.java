package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public class SculkStepAbility extends FootprintAbility implements Ability.Factory<SculkStepAbility> {
    public static final SculkStepAbility INSTANCE = new SculkStepAbility();
    public static final Codec<SculkStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos) {
        var level = dragon.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var random = level.getRandom();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);
        var steppingOver = level.getBlockState(pos);

        // Target: Grass, Dirt, and Stones
        boolean isConvertible = steppingOn.is(BlockTags.DIRT) ||
                steppingOn.is(Blocks.GRASS_BLOCK) ||
                steppingOn.is(BlockTags.BASE_STONE_OVERWORLD);

        if (isConvertible) {
            float roll = random.nextFloat();

            // Scenario 1: Just Sculk Block (approx 33% of hits)
            if (roll < 0.33f) {
                level.setBlockAndUpdate(groundPos, Blocks.SCULK.defaultBlockState());
            }
            // Scenario 2: Just Sculk Vein (approx 33% of hits)
            else if (roll < 0.66f) {
                if (steppingOver.isAir()) {
                    level.setBlockAndUpdate(pos, Blocks.SCULK_VEIN.defaultBlockState());
                } else {
                    // Fallback to block if air is blocked
                    level.setBlockAndUpdate(groundPos, Blocks.SCULK.defaultBlockState());
                }
            }
            // Scenario 3: Both Block AND Vein (approx 34% of hits)
            else {
                level.setBlockAndUpdate(groundPos, Blocks.SCULK.defaultBlockState());
                if (steppingOver.isAir()) {
                    level.setBlockAndUpdate(pos, Blocks.SCULK_VEIN.defaultBlockState());
                }
            }

            // Effects
            level.playSound(null, pos, SoundEvents.SCULK_BLOCK_STEP, dragon.getSoundSource(), 0.5f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, groundPos.getX() + 0.5, groundPos.getY() + 1.1, groundPos.getZ() + 0.5, 5, 0.2, 0.1, 0.2, 0.02);
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon) {
        return 0.02f;
    }

    @Override
    public SculkStepAbility create() { return this; }

    @Override
    public ResourceLocation type() { return SCULK_STEP; }
}