package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record NearbyBlocksHabitat(Tag<Block> tag) implements Habitat
{
    public static final Codec<NearbyBlocksHabitat> CODEC = Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY))
            .fieldOf("block_tag")
            .xmap(NearbyBlocksHabitat::new, NearbyBlocksHabitat::tag)
            .codec();

    @Override
    public int getHabitatPoints(Level level, BlockPos basePos)
    {
        return (int) BlockPos.betweenClosedStream(basePos.offset(1, 1, 1), basePos.offset(-1, -1, -1))
                .filter(p -> level.getBlockState(p).is(tag))
                .count() / 2;
    }

    @Override
    public String type()
    {
        return Habitat.NEARBY_BLOCKS;
    }
}
