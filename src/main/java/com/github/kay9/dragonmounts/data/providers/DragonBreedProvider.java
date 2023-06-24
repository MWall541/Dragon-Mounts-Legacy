package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.*;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.habitats.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.ImmutableMap.of;

class DragonBreedProvider extends DatapackBuiltinEntriesProvider
{
    static final ResourceKey<DragonBreed> AETHER = key("aether");
    static final ResourceKey<DragonBreed> FIRE = key("fire");
    static final ResourceKey<DragonBreed> END = key("end");
    static final ResourceKey<DragonBreed> FOREST = key("forest");
    static final ResourceKey<DragonBreed> GHOST = key("ghost");
    static final ResourceKey<DragonBreed> ICE = key("ice");
    static final ResourceKey<DragonBreed> NETHER = key("nether");
    static final ResourceKey<DragonBreed> WATER = key("water");

    DragonBreedProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup)
    {
        super(output, lookup, build(), Set.of(DragonMountsLegacy.MOD_ID));
    }

    private static RegistrySetBuilder build()
    {
        return new RegistrySetBuilder().add(BreedRegistry.REGISTRY_KEY, context ->
        {
            context.register(AETHER, builtIn(0x718AA9, 0xE6E6E6,
                    Optional.empty(),
                    DragonBreed.ModelProperties.STANDARD,
                    of(Attributes.FLYING_SPEED, TameableDragon.BASE_SPEED_FLYING * 1.45),
                    list(),
                    list(new HeightHabitat(3, false, 200)),
                    set(),
                    Optional.empty()));
            context.register(FIRE, builtIn(
                    0x912400,
                    0xff9819,
                    Optional.of(ParticleTypes.FLAME),
                    new DragonBreed.ModelProperties(false, false, false),
                    ImmutableMap.of(),
                    ImmutableList.of(HotFeetAbility.INSTANCE),
                    ImmutableList.of(
                            new NearbyBlocksHabitat(1,BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))),
                            new FluidHabitat(3, FluidTags.LAVA)),
                    ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
                    Optional.empty()));
            context.register(END, builtIn(
                    0x161616,
                    0xff63e8,
                    Optional.of(ParticleTypes.PORTAL),
                    DragonBreed.ModelProperties.STANDARD,
                    of(Attributes.MAX_HEALTH, TameableDragon.BASE_HEALTH * 1.25),
                    list(), // teleport ability?
                    list(DragonBreathHabitat.INSTANCE),
                    set("dragonBreath"),
                    Optional.empty()));
            context.register(FOREST, builtIn(
                    0x054a00,
                    0x0a9600,
                    Optional.of(ParticleTypes.HAPPY_VILLAGER),
                    DragonBreed.ModelProperties.STANDARD,
                    of(),
                    list(GreenToesAbility.INSTANCE),
                    list(new NearbyBlocksHabitat(0.5f, BlockTagProvider.FOREST_DRAGON_HABITAT_BLOCKS), new BiomeHabitat(2, BiomeTags.IS_JUNGLE)),
                    set(),
                    Optional.empty()));
            context.register(GHOST, builtIn(
                    0xc4c4c4,
                    0xc2f8ff,
                    Optional.empty(),
                    new DragonBreed.ModelProperties(true, false, true),
                    of(),
                    list(),
                    list(new PickyHabitat(list(new HeightHabitat(1, true, 0), new LightHabitat(2, true, 3)))),
                    set("drown"),
                    sound(DMLRegistry.GHOST_DRAGON_AMBIENT.get())));
            context.register(ICE, builtIn(
                    0xffffff,
                    0x00E1FF,
                    Optional.of(ParticleTypes.SNOWFLAKE),
                    DragonBreed.ModelProperties.STANDARD,
                    of(),
                    list(FrostWalkerAbility.INSTANCE, SnowStepperAbility.INSTANCE),
                    list(new NearbyBlocksHabitat(0.5f, BlockTagProvider.ICE_DRAGON_HABITAT_BLOCKS)),
                    set("drown", "freeze"),
                    Optional.empty()));
            context.register(NETHER, builtIn(
                    0x912400,
                    0x2e0b00,
                    Optional.of(ParticleTypes.SOUL_FIRE_FLAME),
                    DragonBreed.ModelProperties.STANDARD,
                    of(Attributes.ARMOR, 8d),
                    list(),
                    list(new NearbyBlocksHabitat(0.5f, BlockTagProvider.NETHER_DRAGON_HABITAT_BLOCKS), new BiomeHabitat(3, BiomeTags.IS_NETHER)),
                    set("inFire", "onFire", "lava", "hotFloor"),
                    Optional.empty()));
            context.register(WATER, builtIn(
                    0x0062ff,
                    0x5999ff,
                    Optional.of(ParticleTypes.DRIPPING_WATER),
                    new DragonBreed.ModelProperties(true, true, false),
                    of(),
                    list(),
                    list(new FluidHabitat(1f, FluidTags.WATER), new NearbyBlocksHabitat(0.5f, BlockTagProvider.WATER_DRAGON_HABITAT_BLOCKS)),
                    set("drown"),
                    Optional.empty()));
        });
    }

    private static ResourceKey<DragonBreed> key(String path)
    {
        return ResourceKey.create(BreedRegistry.REGISTRY_KEY, DragonMountsLegacy.id(path));
    }

    public static DragonBreed builtIn(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, com.github.kay9.dragonmounts.dragon.breed.DragonBreed.ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<Holder<SoundEvent>> ambientSound)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, modelProperties, attributes, abilities, habitats, immunities, ambientSound, BuiltInLootTables.EMPTY, TameableDragon.BASE_GROWTH_TIME, HatchableEggBlock.DEFAULT_HATCH_CHANCE, TameableDragon.BASE_SIZE_MODIFIER, BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FISHES), BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FISHES));
    }

    @SafeVarargs
    protected static <T> ImmutableList<T> list(T... objs)
    {
        return ImmutableList.copyOf(objs);
    }

    @SafeVarargs
    protected static <T> ImmutableSet<T> set(T... objs)
    {
        return ImmutableSet.copyOf(objs);
    }

    private static Optional<Holder<SoundEvent>> sound(SoundEvent event)
    {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event));
    }
}
