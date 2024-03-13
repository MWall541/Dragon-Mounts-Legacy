package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class GreenToesAbility extends FootprintAbility
{
    public static final GreenToesAbility INSTANCE = new GreenToesAbility();
    public static final Codec<Factory<GreenToesAbility>> CODEC = Ability.singleton(GREEN_TOES, INSTANCE);
    private static final int GRASS_LIGHT_THRESHOLD = 4;

    // grow mushrooms and plants
    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level;
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);

        if (steppingOn.is(Blocks.DIRT) && level.getLightEmission(pos) >= GRASS_LIGHT_THRESHOLD)
        {
            level.setBlockAndUpdate(groundPos, Blocks.GRASS.defaultBlockState());
            return;
        }
        if (!level.getBlockState(pos).isAir()) return; // place only on empty space

        BlockState placing = null;

        if (steppingOn.is(BlockTags.MUSHROOM_GROW_BLOCK))
            placing = (level.getRandom().nextBoolean()? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState();
        else if (steppingOn.is(BlockTags.DIRT)) // different from the actual dirt block. Could be grass or moss
        {
            //noinspection deprecation
            placing = Registry.BLOCK
                    .getTag(BlockTags.SMALL_FLOWERS)
                    .flatMap(tag -> tag.getRandomElement(dragon.getRandom()))
                    .map(Holder::value)
                    .filter(b -> b != Blocks.WITHER_ROSE)
                    .orElse(Blocks.DANDELION)
                    .defaultBlockState();
        }

        if (placing != null && placing.canSurvive(level, pos))
        {
            level.setBlockAndUpdate(pos, placing);
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX(), pos.getY(), pos.getZ(),
                    dragon.getRandom().nextInt(4) + 2,
                    0.5, 0.5, 0.5, 1);
        }
    }
}
