package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.habitats.Habitat;
import com.github.kay9.dragonmounts.util.DMLUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public record DragonBreed(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles,
                          Map<Attribute, Double> attributes, List<Ability.Factory<Ability>> abilityTypes, List<Habitat> habitats,
                          ImmutableSet<String> immunities, Optional<SoundEvent> ambientSound,
                          ResourceLocation deathLoot, int growthTime, int hatchTime, float sizeModifier,
                          HolderSet<Item> tamingItems, HolderSet<Item> breedingItems, Either<Integer, String> reproLimit)
    implements IForgeRegistryEntry<DragonBreed>
{
    public static final Codec<DragonBreed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DMLUtil.HEX_CODEC.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            DMLUtil.HEX_CODEC.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            Codec.unboundedMap(Registry.ATTRIBUTE.byNameCodec(), Codec.DOUBLE).optionalFieldOf("attributes", ImmutableMap.of()).forGetter(DragonBreed::attributes),
            Ability.CODEC.listOf().optionalFieldOf("abilities", ImmutableList.of()).forGetter(DragonBreed::abilityTypes),
            Habitat.CODEC.listOf().optionalFieldOf("habitats", ImmutableList.of()).forGetter(DragonBreed::habitats),
            Codec.STRING.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf).optionalFieldOf("immunities", ImmutableSet.of()).forGetter(DragonBreed::immunities), // convert to Set for "contains" performance
            Registry.SOUND_EVENT.byNameCodec().optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            ResourceLocation.CODEC.optionalFieldOf("death_loot", BuiltInLootTables.EMPTY).forGetter(DragonBreed::deathLoot),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.BASE_GROWTH_TIME).forGetter(DragonBreed::growthTime),
            Codec.INT.optionalFieldOf("hatch_time", DragonEgg.DEFAULT_HATCH_TIME).forGetter(DragonBreed::hatchTime),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier),
            RegistryCodecs.homogeneousList(Registry.ITEM_REGISTRY).optionalFieldOf("taming_items", Registry.ITEM.getOrCreateTag(ItemTags.FISHES)).forGetter(DragonBreed::tamingItems),
            RegistryCodecs.homogeneousList(Registry.ITEM_REGISTRY).optionalFieldOf("breeding_items", Registry.ITEM.getOrCreateTag(ItemTags.FISHES)).forGetter(DragonBreed::breedingItems)
            Codec.either(Codec.INT, Codec.STRING).optionalFieldOf("reproduction_limit", Either.left(-1)).forGetter(DragonBreed::reproLimit)
    ).apply(instance, DragonBreed::new));
    public static final Codec<DragonBreed> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            Codec.INT.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            Registry.SOUND_EVENT.byNameCodec().optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            Codec.INT.fieldOf("growth_time").forGetter(DragonBreed::growthTime),
            Codec.INT.fieldOf("hatch_time").forGetter(DragonBreed::hatchTime),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier)
    ).apply(instance, DragonBreed::fromNetwork));

    public static DragonBreed fromNetwork(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, Optional<SoundEvent> ambientSound, int growthTime, int hatchTime, float sizeModifier)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, Map.of(), List.of(), List.of(), ImmutableSet.of(), ambientSound, BuiltInLootTables.EMPTY, growthTime, hatchTime, sizeModifier, DMLUtil.EMPTY_ITEM_HOLDER_SET, DMLUtil.EMPTY_ITEM_HOLDER_SET, Either.left(0));
    }

    public void initialize(TameableDragon dragon)
    {
        applyAttributes(dragon);
        for (Ability.Factory<Ability> factory : abilityTypes())
        {
            Ability instance = factory.create();
            dragon.getAbilities().add(instance);
            instance.initialize(dragon);
        }
    }

    public void close(TameableDragon dragon)
    {
        dragon.getAttributes().assignValues(new AttributeMap(TameableDragon.createAttributes().build())); // restore default attributes
        for (Ability ability : dragon.getAbilities()) ability.close(dragon);
        dragon.getAbilities().clear();
    }

    private void applyAttributes(TameableDragon dragon)
    {
        float healthPercentile = dragon.getHealth() / dragon.getMaxHealth();

        attributes().forEach((att, value) ->
        {
            AttributeInstance inst = dragon.getAttribute(att);
            if (inst != null) inst.setBaseValue(value);
        });

        dragon.setHealth(dragon.getMaxHealth() * healthPercentile); // in case we have less than max health
    }

    public int getReproductionLimit()
    {
        return reproLimit().map(Function.identity(), DMLConfig::getReproLimitFor);
    }

    public String getTranslationKey(RegistryAccess reg)
    {
        var name = id(reg);
        return "dragon_breed." + name.getNamespace() + "." + name.getPath();
    }

    @Override
    public DragonBreed setRegistryName(ResourceLocation name)
    {
        return this;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return BreedRegistry.REGISTRY.get().getKey(this);
    }

    public ResourceLocation id(RegistryAccess reg)
    {
        return BreedRegistry.registry(reg).getKey(this);
    }

    @Override
    public Class<DragonBreed> getRegistryType()
    {
        return DragonBreed.class;
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
//        public static final RegistryObject<DragonBreed> FIRE_BUILTIN = BreedRegistry.DEFERRED_REGISTRY.register(FIRE.getRegistryName().getPath(), () -> );

        private static ResourceKey<DragonBreed> key(String id)
        {
            return ResourceKey.create(BreedRegistry.REGISTRY_KEY, DragonMountsLegacy.id(id));
        }

        private BuiltIn() {}
    }
}