package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.FrostWalkerAbility;
import com.github.kay9.dragonmounts.abilities.GreenToesAbility;
import com.github.kay9.dragonmounts.abilities.SnowStepperAbility;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.habitats.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;

public class DragonBreedProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ImmutableMap<String, DragonBreed> BREEDS;

    static
    {
        var builder = ImmutableMap.<String, DragonBreed>builder();
        Pair.of("aether", new DragonBreed(
                0x718AA9,
                0xE6E6E6,
                Optional.empty(),
                DragonBreed.ModelProperties.STANDARD,
                of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING * 1.45),
                list(),
                list(new HeightHabitat(3, false, 200)),
                set(),
                Optional.empty()));

        Pair.of("end", new DragonBreed(
                0x161616,
                0xff63e8,
                Optional.of(ParticleTypes.PORTAL),
                DragonBreed.ModelProperties.STANDARD,
                of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
                list(), // teleport ability?
                list(DragonBreathHabitat.INSTANCE),
                set("dragonBreath"),
                Optional.empty()));

        Pair.of("forest", new DragonBreed(
                0x054a00,
                0x0a9600,
                Optional.of(ParticleTypes.HAPPY_VILLAGER),
                DragonBreed.ModelProperties.STANDARD,
                of(),
                list(GreenToesAbility.INSTANCE),
                list(new NearbyBlocksHabitat(0.5f, BlockTagProvider.FOREST_DRAGON_HABITAT_BLOCKS), new BiomeHabitat(2, BiomeTags.IS_JUNGLE)),
                set(),
                Optional.empty()));

        Pair.of("ghost", new DragonBreed(
                0xc4c4c4,
                0xc2f8ff,
                Optional.empty(),
                new DragonBreed.ModelProperties(true, false, true),
                of(),
                list(),
                list(new PickyHabitat(ImmutableList.of(new HeightHabitat(1, true, 0), new LightHabitat(2, true, 3)))),
                set("drown"),
                Optional.of(SoundEvents.SKELETON_AMBIENT)));

        Pair.of("ice", new DragonBreed(
                0xffffff,
                0x00E1FF,
                Optional.of(ParticleTypes.SNOWFLAKE),
                DragonBreed.ModelProperties.STANDARD,
                of(),
                list(FrostWalkerAbility.INSTANCE, SnowStepperAbility.INSTANCE),
                list(new NearbyBlocksHabitat(0.5f, BlockTagProvider.ICE_DRAGON_HABITAT_BLOCKS)),
                set("drown", "freeze"),
                Optional.empty()));

        Pair.of("nether", new DragonBreed(
                0x912400,
                0x2e0b00,
                Optional.of(ParticleTypes.SOUL_FIRE_FLAME),
                DragonBreed.ModelProperties.STANDARD,
                of(Attributes.ARMOR, 8d),
                list(),
                list(new NearbyBlocksHabitat(1f, BlockTagProvider.NETHER_DRAGON_HABITAT_BLOCKS), new BiomeHabitat(3, BiomeTags.IS_NETHER)),
                set("inFire", "onFire", "lava", "hotFloor"),
                Optional.empty()));

        Pair.of("water", new DragonBreed(
                0x0062ff,
                0x5999ff,
                Optional.of(ParticleTypes.DRIPPING_WATER),
                new DragonBreed.ModelProperties(true, true, false),
                of(),
                list(),
                list(new FluidHabitat(1f, FluidTags.WATER)),
                set("drown"),
                Optional.empty()));

        BREEDS = builder.build();
    }

    private final DataGenerator generator;

    public DragonBreedProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) throws IOException
    {
        for (Map.Entry<String, DragonBreed> entry : BREEDS.entrySet())
        {
            DataProvider.save(GSON,
                    cache,
                    DragonBreed.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, DragonMountsLegacy.LOG::error),
                    generator.getOutputFolder().resolve("data/dragonmounts/dragonmounts/dragon_breeds/" + entry.getKey() + ".json"));
        }
    }

    @Override
    public String getName()
    {
        return "Dragon Breeds";
    }

    private static <T> ImmutableList<T> list(T... objs)
    {
        return ImmutableList.copyOf(objs);
    }

    private static <T> ImmutableSet<T> set(T... objs)
    {
        return ImmutableSet.copyOf(objs);
    }
}
