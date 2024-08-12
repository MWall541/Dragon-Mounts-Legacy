package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.abilities.HotFeetAbility;
import com.github.kay9.dragonmounts.dragon.abilities.ReaperStepAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

class BlockTagProvider extends BlockTagsProvider
{
    static final TagKey<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"));
    static final TagKey<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    static final TagKey<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    static final TagKey<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("nether_dragon_habitat_blocks"));
    static final TagKey<Block> WATER_DRAGON_HABITAT_BLOCKS = BlockTags.create(DragonMountsLegacy.id("water_dragon_habitat_blocks"));

    BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, String modid, ExistingFileHelper existingFileHelper)
    {
        super(output, lookup, modid, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider pProvider)
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
                .addTags(BlockTags.FLOWERS, BlockTags.SAPLINGS, BlockTags.CROPS)
                .add(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.SWEET_BERRY_BUSH, Blocks.DEAD_BUSH, Blocks.PITCHER_PLANT,
                        Blocks.BIG_DRIPLEAF, Blocks.SMALL_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SUGAR_CANE,
                        Blocks.FERN, Blocks.LARGE_FERN, Blocks.PITCHER_PLANT);

        tag(ReaperStepAbility.PLANT_DEATH_TAG)
                .addTags(BlockTags.TALL_FLOWERS, BlockTags.CROPS, BlockTags.SAPLINGS)
                .add(Blocks.TALL_GRASS, Blocks.SHORT_GRASS, Blocks.SWEET_BERRY_BUSH, Blocks.SUGAR_CANE, Blocks.BIG_DRIPLEAF_STEM,
                Blocks.BIG_DRIPLEAF, Blocks.FERN, Blocks.LARGE_FERN, Blocks.PITCHER_PLANT);

        tag(ReaperStepAbility.PLANT_DESTRUCTION_TAG)
                .addTags(BlockTags.SMALL_FLOWERS)
                .add(Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM, Blocks.SMALL_DRIPLEAF)
                .remove(Blocks.WITHER_ROSE);

        tag(ReaperStepAbility.REAPER_TRANSFORM)
                .addTags(BlockTags.SAND, BlockTags.DIRT)
                .add(Blocks.GRASS_BLOCK);
    }
}
