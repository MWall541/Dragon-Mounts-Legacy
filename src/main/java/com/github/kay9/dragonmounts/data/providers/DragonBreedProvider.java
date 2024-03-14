package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.*;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.habitats.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.of;

class DragonBreedProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final Pair<ResourceLocation, DragonBreed> AETHER = Pair.of(DragonMountsLegacy.id("aether"), DragonBreed.builtIn(
            0x718AA9,
            0xE6E6E6,
            Optional.empty(),
            of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING * 1.45),
            list(),
            list(
                    new HeightHabitat(3, false, 200)
            ),
            set(),
            Optional.empty()));

    static final Pair<ResourceLocation, DragonBreed> END = Pair.of(DragonMountsLegacy.id("end"), DragonBreed.builtIn(
            0x161616,
            0xff63e8,
            Optional.of(ParticleTypes.PORTAL),
            of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
            list(), // teleport ability?
            list(
                    DragonBreathHabitat.INSTANCE
            ),
            set("dragonBreath"),
            Optional.empty()));

    static final Pair<ResourceLocation, DragonBreed> FOREST = Pair.of(DragonMountsLegacy.id("forest"), DragonBreed.builtIn(
            0x054a00,
            0x0a9600,
            Optional.of(ParticleTypes.HAPPY_VILLAGER),
            of(),
            list(
                    ability(Ability.GREEN_TOES, () -> GreenToesAbility.INSTANCE)
            ),
            list(
                    new NearbyBlocksHabitat(0.5f, BlockTagProvider.FOREST_DRAGON_HABITAT_BLOCKS),
                    new BiomeHabitat(2, BiomeTags.IS_JUNGLE)
            ),
            set(),
            Optional.empty()));

    static final Pair<ResourceLocation, DragonBreed> GHOST = Pair.of(DragonMountsLegacy.id("ghost"), DragonBreed.builtIn(
            0xc4c4c4,
            0xc2f8ff,
            Optional.empty(),
            of(),
            list(
                    ability(Ability.REAPER_STEP, () -> ReaperStepAbility.INSTANCE)
            ),
            list(
                    new PickyHabitat(list(
                            new HeightHabitat(1, true, 0),
                            new LightHabitat(2, true, 3)
                    ))
            ),
            set("drown"),
            Optional.of(DMLRegistry.GHOST_DRAGON_AMBIENT.get())));

    static final Pair<ResourceLocation, DragonBreed> ICE = Pair.of(DragonMountsLegacy.id("ice"), DragonBreed.builtIn(
            0xffffff,
            0x00E1FF,
            Optional.of(ParticleTypes.SNOWFLAKE),
            of(),
            list(
                    new FrostWalkerAbility.Factory(3),
                    ability(Ability.SNOW_STEPPER, () -> SnowStepperAbility.INSTANCE)
            ),
            list(
                    new NearbyBlocksHabitat(0.5f, BlockTagProvider.ICE_DRAGON_HABITAT_BLOCKS)
            ),
            set("drown", "freeze"),
            Optional.empty()));

    static final Pair<ResourceLocation, DragonBreed> NETHER = Pair.of(DragonMountsLegacy.id("nether"), DragonBreed.builtIn(
            0x912400,
            0x2e0b00,
            Optional.of(ParticleTypes.SOUL_FIRE_FLAME),
            of(Attributes.ARMOR, 8d),
            list(),
            list(
                    new NearbyBlocksHabitat(0.5f, BlockTagProvider.NETHER_DRAGON_HABITAT_BLOCKS),
                    new BiomeHabitat(3, BiomeTags.IS_NETHER)
            ),
            set("inFire", "onFire", "lava", "hotFloor"),
            Optional.empty()));

    static final Pair<ResourceLocation, DragonBreed> WATER = Pair.of(DragonMountsLegacy.id("water"), DragonBreed.builtIn(
            0x0062ff,
            0x5999ff,
            Optional.of(ParticleTypes.DRIPPING_WATER),
            of(),
            list(
                    ability(Ability.HYDRO_STEP, () -> HydroStepAbility.INSTANCE)
            ),
            list(
                    new FluidHabitat(1f, FluidTags.WATER),
                    new NearbyBlocksHabitat(0.5f, BlockTagProvider.WATER_DRAGON_HABITAT_BLOCKS)
            ),
            set("drown"),
            Optional.empty()));

    private final DataGenerator generator;
    private HashCache cache;

    DragonBreedProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(HashCache cache) throws IOException
    {
        this.cache = cache;
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        for (var breed : breeds()) encode(breed.getKey(), breed.getValue(), ops);
    }

    protected Pair<ResourceLocation, DragonBreed>[] breeds()
    {
        var fire = Pair.of(DragonBreed.BuiltIn.FIRE_BUILTIN.getId(), DragonBreed.BuiltIn.FIRE_BUILTIN.get());
        //noinspection unchecked
        return new Pair[]{AETHER, END, fire, FOREST, GHOST, ICE, NETHER, WATER};
    }

    protected void encode(ResourceLocation id, DragonBreed breed, RegistryOps<JsonElement> ops) throws IOException
    {
        var json = DragonBreed.CODEC.encodeStart(ops, breed).getOrThrow(false, DragonMountsLegacy.LOG::error);
        var regKey = BreedRegistry.REGISTRY.get().getRegistryKey().location();
        var path = String.join("/", "data", id.getNamespace(), regKey.getNamespace(), regKey.getPath(), id.getPath() + ".json");

        DataProvider.save(GSON, cache, json, generator.getOutputFolder().resolve(path));
    }

    @Override
    public String getName()
    {
        return "Dragon Breeds";
    }

    @SuppressWarnings("unchecked")
    protected static <T> ImmutableList<T> list(Object... objs)
    {
        return (ImmutableList<T>) ImmutableList.copyOf(objs);
    }

    @SafeVarargs
    protected static <T> ImmutableSet<T> set(T... objs)
    {
        return ImmutableSet.copyOf(objs);
    }

    protected static Ability.Factory<Ability> ability(ResourceLocation id, Supplier<Ability> fact)
    {
        return Ability.simpleFactory(id, fact);
    }
}
