package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class GreenToesAbility extends FootprintAbility implements Ability.Factory<GreenToesAbility>
{
    public static final GreenToesAbility INSTANCE = new GreenToesAbility();
    public static final Codec<GreenToesAbility> CODEC = Codec.unit(INSTANCE);

    protected GreenToesAbility() {}

    // grow mushrooms and plants
    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.getLevel();
        var groundPos = pos.below();
        var steppingOn = level.getBlockState(groundPos);
        var steppingOver = level.getBlockState(pos);

        if (steppingOn.is(Blocks.DIRT)) // regrow grass on dirt
        {
            level.setBlockAndUpdate(groundPos, Blocks.GRASS_BLOCK.defaultBlockState());
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, groundPos, 2);
            return;
        }

        if (steppingOver.isAir()) // manually place flowers, mushrooms, etc.
        {
            BlockState placing = null;

            if (steppingOn.is(BlockTags.MUSHROOM_GROW_BLOCK))
                placing = (level.getRandom().nextBoolean()? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState();
            else if (steppingOn.is(BlockTags.DIRT) && !steppingOn.is(Blocks.MOSS_BLOCK)) // different from the actual dirt block, could be grass or podzol.
            {
                // while grass blocks etc. do have defined bone meal behavior, I think our own is more viable.

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
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                return;
            }
        }

        if (steppingOn.is(BlockTags.SAPLINGS) ||
                steppingOver.is(BlockTags.SAPLINGS) ||
                steppingOver.is(Blocks.BROWN_MUSHROOM) ||
                steppingOver.is(Blocks.RED_MUSHROOM) ||
                steppingOver.is(Blocks.WARPED_FUNGUS) ||
                steppingOver.is(Blocks.CRIMSON_FUNGUS))
        {
            return; // if these structures grow on the dragon they could hurt it...
        }

        // perform standard bone meal behavior on steppingOn or steppingOver block.
        var caret = pos;
        for (int i = 0; i < 2; caret = groundPos)
        {
            i++;
            var state = level.getBlockState(caret);
            if (!(state.getBlock() instanceof BonemealableBlock b) || !b.isValidBonemealTarget(level, caret, state, level.isClientSide()))
                continue;

            if (b.isBonemealSuccess(level, dragon.getRandom(), caret, state))
            {
                b.performBonemeal((ServerLevel) level, level.getRandom(), caret, state);
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, caret, 0);
                return;
            }
        }
    }

    @Override
    public GreenToesAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return GREEN_TOES;
    }
}
