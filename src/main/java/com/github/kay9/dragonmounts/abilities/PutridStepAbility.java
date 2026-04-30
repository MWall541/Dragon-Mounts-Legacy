package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;

public class PutridStepAbility extends FootprintAbility implements Ability.Factory<PutridStepAbility> {
    public static final PutridStepAbility INSTANCE = new PutridStepAbility();
    public static final Codec<PutridStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos) {
        var level = dragon.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);
        var steppingOver = level.getBlockState(pos);
        var random = level.getRandom();

        // Ground Transformation (The Rot)
        if (steppingOn.is(Blocks.GRASS_BLOCK)) {
            // First 50/50 roll: Fungal vs Earthen
            if (random.nextBoolean()) {
                // FUNGAL SIDE (50/50 Mycelium vs Podzol)
                if (random.nextBoolean()) {
                    level.setBlockAndUpdate(groundPos, Blocks.MYCELIUM.defaultBlockState());
                    var mushroom = random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
                    if (steppingOver.isAir() && mushroom.defaultBlockState().canSurvive(level, pos)) {
                        level.setBlockAndUpdate(pos, mushroom.defaultBlockState());
                    }
                } else {
                    level.setBlockAndUpdate(groundPos, Blocks.PODZOL.defaultBlockState());
                }
            } else {
                // EARTHEN SIDE (50/50 Moss vs Coarse Dirt)
                var decayState = random.nextBoolean() ? Blocks.MOSS_BLOCK : Blocks.COARSE_DIRT;
                level.setBlockAndUpdate(groundPos, decayState.defaultBlockState());
            }

            level.playSound(null, pos, SoundEvents.FUNGUS_HIT, dragon.getSoundSource(), 0.1f, 2f);
            serverLevel.sendParticles(ParticleTypes.MYCELIUM, groundPos.getX() + 0.5, groundPos.getY() + 1.1, groundPos.getZ() + 0.5, 5, 0.2, 0.1, 0.2, 0.02);
            return;
        }

        // Fallback: Plague (Dead Bushes)
        // This only runs if the ground wasn't Grass (e.g., walking on Stone, Sand, or Dirt)
        if (steppingOver.isAir() && Blocks.DEAD_BUSH.defaultBlockState().canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.DEAD_BUSH.defaultBlockState());
            level.playSound(null, pos, SoundEvents.FUNGUS_HIT, dragon.getSoundSource(), 0.1f, 2f);
            serverLevel.sendParticles(ParticleTypes.SNEEZE, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.01);
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon) {
        return 0.02f;
    }

    @Override
    public PutridStepAbility create() { return this; }

    @Override
    public ResourceLocation type() { return PUTRID_STEP; }
}