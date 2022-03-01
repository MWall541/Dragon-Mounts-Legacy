package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class TagProvider extends BlockTagsProvider
{
    public static final Tags.IOptionalNamedTag<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("fire_dragon_habitat_blocks"));
    public static final Tags.IOptionalNamedTag<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    public static final Tags.IOptionalNamedTag<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    public static final Tags.IOptionalNamedTag<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("nether_dragon_habitat_blocks"));

    public TagProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper)
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

        tag(NETHER_DRAGON_HABITAT_BLOCKS).addTags(Tags.Blocks.NETHERRACK, BlockTags.SOUL_FIRE_BASE_BLOCKS)
                .add(Blocks.SOUL_FIRE, Blocks.SOUL_CAMPFIRE);
    }
}
