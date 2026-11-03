package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.habitats.Habitat;
import com.github.kay9.dragonmounts.util.DMLUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record DragonBreed(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles,
                          Map<Holder<Attribute>, Double> attributes, List<Ability.Factory<? extends Ability>> abilityTypes,
                          List<Habitat> habitats, HolderSet<DamageType> immunities, Optional<Holder<SoundEvent>> ambientSound,
                          int growthTime, float hatchChance, float sizeModifier, HolderSet<Item> tamingItems,
                          HolderSet<Item> breedingItems, Either<Integer, String> reproLimit)
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));

    public static final Codec<DragonBreed> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DMLUtil.HEX_CODEC.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            DMLUtil.HEX_CODEC.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), Codec.DOUBLE).optionalFieldOf("attributes", ImmutableMap.of()).forGetter(DragonBreed::attributes),
            Ability.CODEC.listOf().optionalFieldOf("abilities", ImmutableList.of()).forGetter(DragonBreed::abilityTypes),
            Habitat.CODEC.listOf().optionalFieldOf("habitats", ImmutableList.of()).forGetter(DragonBreed::habitats),
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("immunities", HolderSet.direct()).forGetter(DragonBreed::immunities),
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.BASE_GROWTH_TIME).forGetter(DragonBreed::growthTime),
            Codec.FLOAT.optionalFieldOf("hatch_chance", HatchableEggBlock.DEFAULT_HATCH_CHANCE).forGetter(DragonBreed::hatchChance),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier),
            // empty = fall back to the #minecraft:fishes tag; see isTamingItem()/isBreedingItem().
            // Can't default to that tag directly here: it isn't bound yet while this registry loads.
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("taming_items", HolderSet.direct()).forGetter(DragonBreed::tamingItems),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("breeding_items", HolderSet.direct()).forGetter(DragonBreed::breedingItems),
            Codec.either(Codec.INT, Codec.STRING).optionalFieldOf("reproduction_limit", Either.left(-1)).forGetter(DragonBreed::reproLimit)
    ).apply(instance, DragonBreed::new));
    // Must stay format-compatible with DIRECT_CODEC: in singleplayer/local play the client re-parses
    // the same on-disk datapack JSON with THIS codec instead of receiving bytes over the network
    // (see NetworkRegistryLoadTask#load, the "known data" fallback branch), it does not just decode
    // a slimmed-down wire payload.
    public static final Codec<DragonBreed> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DMLUtil.HEX_CODEC.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            DMLUtil.HEX_CODEC.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.BASE_GROWTH_TIME).forGetter(DragonBreed::growthTime),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier)
    ).apply(instance, DragonBreed::fromNetwork));

    public static final Codec<Holder<DragonBreed>> CODEC = RegistryFixedCodec.create(REGISTRY_KEY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonBreed>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY_KEY);

    public static DragonBreed fromNetwork(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, Optional<Holder<SoundEvent>> ambientSound, int growthTime, float sizeModifier)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, Map.of(), List.of(), List.of(), HolderSet.direct(), ambientSound, growthTime, 0, sizeModifier, HolderSet.direct(), HolderSet.direct(), Either.left(0));
    }

    @Nullable
    public static Holder.Reference<DragonBreed> parse(String byString, HolderLookup.Provider reg)
    {
        Identifier id = Identifier.tryParse(byString);
        if (id == null) return null;
        return reg.lookupOrThrow(REGISTRY_KEY).get(ResourceKey.create(REGISTRY_KEY, id)).orElse(null);
    }

    @Nullable
    public static Holder.Reference<DragonBreed> get(ResourceKey<DragonBreed> key, HolderLookup.Provider reg)
    {
        return reg.lookupOrThrow(REGISTRY_KEY).get(key).orElse(null);
    }

    public static Holder.Reference<DragonBreed> getRandom(RegistryAccess reg, RandomSource random)
    {
        return reg.lookupOrThrow(REGISTRY_KEY).getRandom(random).orElseThrow();
    }

    public static Registry<DragonBreed> registry(RegistryAccess reg)
    {
        return reg.lookupOrThrow(REGISTRY_KEY);
    }

    public void initialize(TameableDragon dragon)
    {
        applyAttributes(dragon);
        for (var factory : abilityTypes())
        {
            var instance = factory.create();
            dragon.getAbilities().add(instance);
            instance.initialize(dragon);
        }
    }

    public int getReproductionLimit()
    {
        return reproLimit().map(Function.identity(), DMLConfig::getReproLimitFor);
    }

    /** Whether the given item can tame a dragon of this breed. Falls back to {@code #minecraft:fishes} if unset. */
    public boolean isTamingItem(ItemStack stack)
    {
        return tamingItems.size() == 0 ? stack.is(ItemTags.FISHES) : tamingItems.contains(stack.getItem().builtInRegistryHolder());
    }

    /** Whether the given item can put a dragon of this breed in love mode. Falls back to {@code #minecraft:fishes} if unset. */
    public boolean isBreedingItem(ItemStack stack)
    {
        return breedingItems.size() == 0 ? stack.is(ItemTags.FISHES) : stack.is(breedingItems);
    }

    public static Component getTranslation(Holder<DragonBreed> breed)
    {
        if (!breed.isBound()) return DMLRegistry.DRAGON.getDescription();
        return Component.translatable("dragon_breed." + breed.getRegisteredName().replace(':', '.'));
    }

    /**
     * Obtain the loot table for this specific breed.
     * The loot table's namespace must match the breed's namespace.
     * {@code my_mod:my_breed} -> {@code my_mod:entities/dragon_breeds/my_breed}
     * This is {@code Nullable} on purpose! If the table doesn't exist, fall back to the base dragon entity loot table.
     * Unmodified Dragon Mounts doesn't have a loot table for any breed or the base dragon, HOWEVER; if the base dragon
     * entity had a loot table via datapack, it can be overridden with a breed table, INCLUDING an empty one!
     */
    // DO NOT CACHE! LootTables are a reloadable resource!
    @Nullable
    public static ResourceKey<LootTable> getLootTableKey(Holder<DragonBreed> b, ReloadableServerRegistries.Holder reg)
    {
        if (b instanceof Holder.Reference<DragonBreed> breed)
        {
            Identifier path = breed.key().identifier().withPrefix("entities/dragon_breeds/");
            if (reg.lookup().lookupOrThrow(Registries.LOOT_TABLE).get(ResourceKey.create(Registries.LOOT_TABLE, path)).isPresent())
                return ResourceKey.create(Registries.LOOT_TABLE, path);
        }

        return null;
    }

    private void applyAttributes(TameableDragon dragon)
    {
        float healthFrac = dragon.getHealthFraction(); // in case max health is changed

        attributes().forEach((att, value) ->
        {
            AttributeInstance inst = dragon.getAttribute(att);
            if (inst != null) inst.setBaseValue(value);
        });

        dragon.setHealth(dragon.getMaxHealth() * healthFrac);
    }

    public static final class BuiltIn
    {
        public static final ResourceKey<DragonBreed> AETHER = key("aether");
        public static final ResourceKey<DragonBreed> END = key("end");
        public static final ResourceKey<DragonBreed> FIRE = key("fire");
        public static final ResourceKey<DragonBreed> FOREST = key("forest");
        public static final ResourceKey<DragonBreed> GHOST = key("ghost");
        public static final ResourceKey<DragonBreed> ICE = key("ice");
        public static final ResourceKey<DragonBreed> NETHER = key("nether");
        public static final ResourceKey<DragonBreed> WATER = key("water");

        private static ResourceKey<DragonBreed> key(String id)
        {
            return ResourceKey.create(REGISTRY_KEY, DragonMountsLegacy.id(id));
        }

        private BuiltIn() {}
    }
}