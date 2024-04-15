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

public class HotFeetAbility extends FootprintAbility implements Ability.Factory<HotFeetAbility>
{
    public static final HotFeetAbility INSTANCE = new HotFeetAbility();
    public static final Codec<HotFeetAbility> CODEC = Codec.unit(INSTANCE);

    public static final TagKey<Block> BURNABLES_TAG = BlockTags.create(DragonMountsLegacy.id("hot_feet_burnables"));

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var steppingOn = level.getBlockState(pos);
        if (steppingOn.is(BURNABLES_TAG))
        {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.1f, 2f);
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 1, 0, 0.05);
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
