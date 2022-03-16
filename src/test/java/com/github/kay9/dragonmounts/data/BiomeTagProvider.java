package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BiomeTagProvider extends TagsProvider<Biome>
{
    public static final TagKey<Biome> FOREST_DRAGON_BIOMES = key("forest_dragon_biomes");
    public static final TagKey<Biome> NETHER_DRAGON_BIOMES = key("nether_dragon_biomes");

    public BiomeTagProvider(DataGenerator gen, ExistingFileHelper validator)
    {
        super(gen, BuiltinRegistries.BIOME, DragonMountsLegacy.MOD_ID, validator, "worldgen/biomes");
    }

    @Override
    protected void addTags()
    {
        tag(FOREST_DRAGON_BIOMES).add(Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE);
        tag(NETHER_DRAGON_BIOMES).add(Biomes.BASALT_DELTAS, Biomes.CRIMSON_FOREST, Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.WARPED_FOREST);
    }

    @Override
    public String getName()
    {
        return "DM:L Biome Tags";
    }

    private static TagKey<Biome> key(String name)
    {
        return TagKey.create(Registry.BIOME_REGISTRY, DragonMountsLegacy.id(name));
    }
}
