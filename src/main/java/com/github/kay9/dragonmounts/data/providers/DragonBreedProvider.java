package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.*;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.habitats.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;

class DragonBreedProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    protected Pair<ResourceLocation, DragonBreed>[] breeds()
    {
        //noinspection unchecked
        return new Pair[]{
        builtIn(DragonBreed.BuiltIn.AETHER,
                0x718AA9,
                0xE6E6E6,
                Optional.empty(),
                of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING * 1.45),
                list(),
                list(
                        new HeightHabitat(3, false, 200)
                ),
                set(),
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.END,
                0x161616,
                0xff63e8,
                Optional.of(ParticleTypes.PORTAL),
                of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
                list(), // teleport ability?
                list(
                        DragonBreathHabitat.INSTANCE
                ),
                set("dragonBreath"),
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.FIRE,
                0x912400,
                0xff9819,
                Optional.of(ParticleTypes.FLAME),
                of(),
                list(
                        HotFeetAbility.INSTANCE
                ),
                ImmutableList.of(
                        new NearbyBlocksHabitat(1, BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))),
                        new FluidHabitat(3, FluidTags.LAVA)),
                ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.FOREST,
                0x054a00,
                0x0a9600,
                Optional.of(ParticleTypes.HAPPY_VILLAGER),
                of(),
                list(
                        GreenToesAbility.INSTANCE
                ),
                list(
                        new NearbyBlocksHabitat(0.5f, BlockTagProvider.FOREST_DRAGON_HABITAT_BLOCKS),
                        new BiomeHabitat(2, BiomeTags.IS_JUNGLE)
                ),
                set(),
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.GHOST,
                0xc4c4c4,
                0xc2f8ff,
                Optional.empty(),
                of(),
                list(
                        ReaperStepAbility.INSTANCE
                ),
                list(
                        new PickyHabitat(list(
                                new HeightHabitat(1, true, 0),
                                new LightHabitat(2, true, 3)
                        ))
                ),
                set("drown"),
                Optional.of(DMLRegistry.GHOST_DRAGON_AMBIENT.get())),

        builtIn(DragonBreed.BuiltIn.ICE,
                0xffffff,
                0x00E1FF,
                Optional.of(ParticleTypes.SNOWFLAKE),
                of(),
                list(
                        FrostWalkerAbility.create(3),
                        SnowStepperAbility.INSTANCE
                ),
                list(
                        new NearbyBlocksHabitat(0.5f, BlockTagProvider.ICE_DRAGON_HABITAT_BLOCKS)
                ),
                set("drown", "freeze"),
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.NETHER,
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
                Optional.empty()),

        builtIn(DragonBreed.BuiltIn.WATER,
                0x0062ff,
                0x5999ff,
                Optional.of(ParticleTypes.DRIPPING_WATER),
                of(),
                list(
                        HydroStepAbility.INSTANCE
                ),
                list(
                        new FluidHabitat(1f, FluidTags.WATER),
                        new NearbyBlocksHabitat(0.5f, BlockTagProvider.WATER_DRAGON_HABITAT_BLOCKS)
                ),
                set("drown"),
                Optional.empty())
        };
    }

    public static DragonBreed builtIn(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, Map<Attribute, Double> attributes, List<Ability.Factory<Ability>> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> ambientSound, Either<Integer, String> reproduction)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, attributes, abilities, habitats, immunities, ambientSound, BuiltInLootTables.EMPTY, TameableDragon.BASE_GROWTH_TIME, DragonEgg.DEFAULT_HATCH_TIME, TameableDragon.BASE_SIZE_MODIFIER, Registry.ITEM.getOrCreateTag(ItemTags.FISHES), Registry.ITEM.getOrCreateTag(ItemTags.FISHES), reproduction);
    }

    private static Pair<ResourceLocation, DragonBreed> builtIn(ResourceKey<DragonBreed> id, int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, Map<Attribute, Double> attributes, List<Ability.Factory<Ability>> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> ambientSound)
    {
        Either<Integer, String> reproConfigTarget = Either.right("config:" + id.location().getPath());
        return Pair.of(id.location(), builtIn(primaryColor, secondaryColor, hatchParticles, attributes, abilities, habitats, immunities, ambientSound, reproConfigTarget));
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
}
