package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class GreenToesAbility extends FootprintAbility implements Ability.Factory<GreenToesAbility>
{
    public static final GreenToesAbility INSTANCE = new GreenToesAbility();
    public static final MapCodec<GreenToesAbility> CODEC = MapCodec.unit(INSTANCE);

    // BlockTags.SAPLINGS no longer exists as a constant, but the data tag is still shipped
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SAPLINGS =
            net.minecraft.tags.TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("saplings"));

    protected GreenToesAbility() {}

    // grow mushrooms and plants
    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
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

            if (steppingOn.is(Blocks.MYCELIUM) || steppingOn.is(Blocks.PODZOL)) // mushroom_grow_block tag no longer exists
                placing = (level.getRandom().nextBoolean()? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState();
            else if (steppingOn.is(BlockTags.DIRT) && !steppingOn.is(Blocks.MOSS_BLOCK)) // different from the actual dirt block, could be grass or podzol.
            {
                // while grass blocks etc. do have defined bone meal behavior, I think our own is more viable.

                placing = level.registryAccess().lookupOrThrow(Registries.BLOCK)
                        .get(BlockTags.SMALL_FLOWERS)
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

        if (steppingOn.is(SAPLINGS) ||
                steppingOver.is(SAPLINGS) ||
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
            if (!(state.getBlock() instanceof BonemealableBlock b) || !b.isValidBonemealTarget(level, caret, state))
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
    public MapCodec<? extends Factory<? extends Ability>> codec()
    {
        return CODEC;
    }
}
