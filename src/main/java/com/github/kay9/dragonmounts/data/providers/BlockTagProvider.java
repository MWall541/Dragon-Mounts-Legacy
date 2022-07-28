package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

class BlockTagProvider extends BlockTagsProvider
{
    static final TagKey<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"));
    static final TagKey<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    static final TagKey<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    static final TagKey<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("nether_dragon_habitat_blocks"));

    BlockTagProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(pGenerator, DragonMountsLegacy.MOD_ID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags()
    {
        tag(FIRE_DRAGON_HABITAT_BLOCKS).add(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE);

        tag(FOREST_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.LEAVES, BlockTags.SAPLINGS, BlockTags.FLOWERS)
                .add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, Blocks.VINE);

        tag(ICE_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.ICE, BlockTags.SNOW);

        tag(NETHER_DRAGON_HABITAT_BLOCKS).addTags(BlockTags.SOUL_FIRE_BASE_BLOCKS)
                .add(Blocks.BLACKSTONE, Blocks.BASALT, Blocks.SOUL_FIRE, Blocks.SOUL_CAMPFIRE);
    }
}
