package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum GreenToesAbility implements Ability
{
    INSTANCE;

    public static final Codec<GreenToesAbility> CODEC = Codec.unit(INSTANCE);
    private static final int GRASS_LIGHT_THRESHOLD = 4;

    @Override
    public void tick(TameableDragon dragon)
    {
        if (dragon.level.isClientSide()) return;
        if (!dragon.isAdult() || !dragon.isOnGround()) return;

        for (int i = 0; i < 4; i++)
        {
            // place only if randomly selected
            if (dragon.getRandom().nextFloat() > 0.05f)
            {
                continue;
            }

            // get footprint position
            double bx = dragon.getX() + (i % 2 * 2 - 1) * 0.25;
            double by = dragon.getY() + 0.5;
            double bz = dragon.getZ() + (i / 2f % 2 * 2 - 1) * 0.25;
            BlockPos pos = new BlockPos(bx, by, bz);

            // footprints can only be placed on empty space
            if (!dragon.level.getBlockState(pos).isAir()) continue;

            placeFootprintBlock(dragon, pos);
        }
    }

    // grow mushrooms and plants
    private void placeFootprintBlock(TameableDragon dragon, BlockPos pos)
    {
        Level level = dragon.level;
        BlockPos groundPos = pos.below();
        BlockState steppingOn = level.getBlockState(groundPos);
        BlockState placing = null;

        if (steppingOn.is(Blocks.DIRT))
        {
            if (level.getLightEmission(pos) >= GRASS_LIGHT_THRESHOLD)
            {
                level.setBlockAndUpdate(groundPos, Blocks.GRASS.defaultBlockState());
                return;
            }
        }
        else if (steppingOn.is(BlockTags.MUSHROOM_GROW_BLOCK))
            placing = (level.getRandom().nextBoolean()? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM).defaultBlockState();
        else if (steppingOn.is(BlockTags.DIRT)) // different from the actual dirt block. Could be grass or moss
            placing = BlockTags.SMALL_FLOWERS.getRandomElement(dragon.getRandom()).defaultBlockState();

        if (placing != null) level.setBlockAndUpdate(pos, placing);
    }

    @Override
    public String type()
    {
        return Ability.GREEN_TOES;
    }
}
