package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;

public class JadeStepAbility extends FootprintAbility implements Ability.Factory<JadeStepAbility> {
    public static final JadeStepAbility INSTANCE = new JadeStepAbility();
    public static final Codec<JadeStepAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos) {
        var level = dragon.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        // Strictly check for Stone or Deepslate
        boolean isStone = steppingOn.is(Blocks.STONE);
        boolean isDeepslate = steppingOn.is(Blocks.DEEPSLATE);

        if (isStone || isDeepslate) {
            var ore = isDeepslate ? Blocks.DEEPSLATE_EMERALD_ORE : Blocks.EMERALD_ORE;
            level.setBlockAndUpdate(groundPos, ore.defaultBlockState());
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, dragon.getSoundSource(), 0.5f, 1.5f);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, groundPos.getX() + 0.5, groundPos.getY() + 1.1, groundPos.getZ() + 0.5, 5, 0.2, 0.1, 0.2, 0.02);
        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon) {
        return 0.002f;
    }

    @Override
    public JadeStepAbility create() { return this; }

    @Override
    public ResourceLocation type() { return JADE_STEP; }
}