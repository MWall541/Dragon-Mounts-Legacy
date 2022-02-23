package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.habitats.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.Tags;

import java.io.IOException;
import java.util.Optional;

public class DragonBreedProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final DataGenerator generator;
    private HashCache directory;

    public DragonBreedProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(HashCache pCache) throws IOException
    {
        this.directory = pCache;

        add(new DragonBreed(DragonMountsLegacy.id("aether"),
                0x718AA9,
                0xE6E6E6,
                true,
                false,
                ImmutableMap.of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING + 2),
                set(new HeightHabitat(false, 200)),
                set(),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(new DragonBreed(DragonMountsLegacy.id("end"),
                0x161616,
                0xff63e8,
                true,
                false,
                ImmutableMap.of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
                set(new NearbyBlocksHabitat(Tags.Blocks.END_STONES), new BiomeHabitat(set(Biomes.END_BARRENS, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.END_MIDLANDS, Biomes.THE_END, Biomes.SMALL_END_ISLANDS))),
                set("dragonBreath"),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(DragonBreed.FIRE);

        add(new DragonBreed(DragonMountsLegacy.id("forest"),
                0x054a00,
                0x0a9600,
                true,
                false,
                ImmutableMap.of(),
                set(new NearbyBlocksHabitat(DMLRegistry.FOREST_DRAGON_HABITAT_BLOCKS)),
                set(),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(new DragonBreed(DragonMountsLegacy.id("ghost"),
                0xc4c4c4,
                0xc2f8ff,
                true,
                false,
                ImmutableMap.of(),
                set(new PickyHabitat(ImmutableList.of(new HeightHabitat(true, 0), new LightHabitat(true, 3)))),
                set("drown"),
                Optional.of(SoundEvents.SKELETON_AMBIENT),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(new DragonBreed(DragonMountsLegacy.id("ice"),
                0xffffff,
                0x00b7ff,
                true,
                false,
                ImmutableMap.of(),
                set(new NearbyBlocksHabitat(DMLRegistry.ICE_DRAGON_HABITAT_BLOCKS)),
                set("drown", "freeze"),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(new DragonBreed(DragonMountsLegacy.id("nether"),
                0x912400,
                0x2e0b00,
                true,
                false,
                ImmutableMap.of(Attributes.ARMOR, 8d),
                set(new NearbyBlocksHabitat(DMLRegistry.NETHER_DRAGON_HABITAT_BLOCKS), new BiomeHabitat(set(Biomes.BASALT_DELTAS, Biomes.CRIMSON_FOREST, Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.WARPED_FOREST))),
                set("inFire", "onFire", "lava", "hotFloor"),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));

        add(new DragonBreed(DragonMountsLegacy.id("water"),
                0x0062ff,
                0x5999ff,
                true,
                true,
                ImmutableMap.of(),
                set(new FluidHabitat(FluidTags.WATER)),
                set("drown"),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                TameableDragon.DEFAULT_GROWTH_TIME));
    }

    protected void add(DragonBreed breed) throws IOException
    {
        String path = breed.id().getNamespace();
        String name = breed.id().getPath();
        DataProvider.save(GSON,
                directory,
                DragonBreed.CODEC.encodeStart(JsonOps.INSTANCE, breed).getOrThrow(false, DragonMountsLegacy.LOG::error),
                generator.getOutputFolder().resolve("data/" + path + "/dragon_breeds/" + name + ".json"));
    }

    @Override
    public String getName()
    {
        return "Dragon Breeds";
    }

    private <T> ImmutableSet<T> set(T... objs)
    {
        return ImmutableSet.copyOf(objs);
    }
}
