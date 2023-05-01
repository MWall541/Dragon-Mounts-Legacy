package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.HotFeetAbility;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;

class BlockTagProvider extends BlockTagsProvider
{
    static final TagKey<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"));
    static final TagKey<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    static final TagKey<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    static final TagKey<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("nether_dragon_habitat_blocks"));
    static final TagKey<Block> WATER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("water_dragon_habitat_blocks"));

    BlockTagProvider(DataGenerator pGenerator, String modid, ExistingFileHelper existingFileHelper)
    {
        super(pGenerator, modid, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags()
    {
        tag(FIRE_DRAGON_HABITAT_BLOCKS)
                .add(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE);

        tag(FOREST_DRAGON_HABITAT_BLOCKS)
                .addTags(BlockTags.LEAVES, BlockTags.SAPLINGS, BlockTags.FLOWERS)
                .add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, Blocks.VINE);

        tag(ICE_DRAGON_HABITAT_BLOCKS)
                .addTags(BlockTags.ICE, BlockTags.SNOW);

        tag(NETHER_DRAGON_HABITAT_BLOCKS)
                .addTags(BlockTags.SOUL_FIRE_BASE_BLOCKS, BlockTags.BASE_STONE_NETHER, BlockTags.WARPED_STEMS, BlockTags.CRIMSON_STEMS)
                .add(Blocks.GLOWSTONE, Blocks.SOUL_FIRE, Blocks.SOUL_CAMPFIRE, Blocks.SOUL_TORCH, Blocks.NETHER_GOLD_ORE,
                        Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS, Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES_PLANT,
                        Blocks.SHROOMLIGHT, Blocks.NETHER_WART, Blocks.NETHER_WART_BLOCK);

        tag(WATER_DRAGON_HABITAT_BLOCKS)
                .addTags(BlockTags.CORALS, BlockTags.WALL_CORALS, BlockTags.CORAL_BLOCKS)
                .add(Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.KELP, Blocks.KELP_PLANT, Blocks.PRISMARINE,
                        Blocks.SEA_LANTERN, Blocks.SEA_PICKLE);

        tag(HotFeetAbility.BURNABLES_TAG)
                .addTags(BlockTags.FLOWERS, BlockTags.SAPLINGS, BlockTags.REPLACEABLE_PLANTS);
    }
}
