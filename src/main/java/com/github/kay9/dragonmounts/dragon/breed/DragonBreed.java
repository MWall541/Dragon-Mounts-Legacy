package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.habitats.Habitat;
import com.github.kay9.dragonmounts.util.DMLUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("deprecation")
public record DragonBreed(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles,
                          ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities,
                          List<Habitat> habitats, ImmutableSet<String> immunities, Optional<Holder<SoundEvent>> ambientSound,
                          ResourceLocation deathLoot, int growthTime, float hatchWeight, float sizeModifier,
                          HolderSet<Item> tamingItems, HolderSet<Item> breedingItems)
{
    public static final Codec<DragonBreed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DMLUtil.HEX_CODEC.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            DMLUtil.HEX_CODEC.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            ModelProperties.CODEC.optionalFieldOf("model_properties", ModelProperties.STANDARD).forGetter(DragonBreed::modelProperties),
            Codec.unboundedMap(BuiltInRegistries.ATTRIBUTE.byNameCodec(), Codec.DOUBLE).optionalFieldOf("attributes", ImmutableMap.of()).forGetter(DragonBreed::attributes),
            Ability.CODEC.listOf().optionalFieldOf("abilities", ImmutableList.of()).forGetter(DragonBreed::abilities),
            Habitat.CODEC.listOf().optionalFieldOf("habitats", ImmutableList.of()).forGetter(DragonBreed::habitats),
            Codec.STRING.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf).optionalFieldOf("immunities", ImmutableSet.of()).forGetter(DragonBreed::immunities), // convert to Set for "contains" performance
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            ResourceLocation.CODEC.optionalFieldOf("death_loot", BuiltInLootTables.EMPTY).forGetter(DragonBreed::deathLoot),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.BASE_GROWTH_TIME).forGetter(DragonBreed::growthTime),
            Codec.FLOAT.optionalFieldOf("hatch_weight", HatchableEggBlock.DEFAULT_HATCH_WEIGHT).forGetter(DragonBreed::hatchWeight),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("taming_items", BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FISHES)).forGetter(DragonBreed::tamingItems),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("breeding_items", BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FISHES)).forGetter(DragonBreed::breedingItems)
    ).apply(instance, DragonBreed::new));

    public static final Codec<DragonBreed> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            Codec.INT.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            ModelProperties.CODEC.fieldOf("model_properties").forGetter(DragonBreed::modelProperties),
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::ambientSound),
            Codec.INT.fieldOf("growth_time").forGetter(DragonBreed::growthTime),
            Codec.FLOAT.optionalFieldOf("size_modifier", TameableDragon.BASE_SIZE_MODIFIER).forGetter(DragonBreed::sizeModifier)
    ).apply(instance, DragonBreed::fromNetwork));

    public static DragonBreed fromNetwork(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, Optional<Holder<SoundEvent>> ambientSound, int growthTime, float sizeModifier)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, modelProperties, Map.of(), List.of(), List.of(), ImmutableSet.of(), ambientSound, BuiltInLootTables.EMPTY, growthTime, 0, sizeModifier, DMLUtil.EMPTY_ITEM_HOLDER_SET, DMLUtil.EMPTY_ITEM_HOLDER_SET);
    }

    public void initialize(TameableDragon dragon)
    {
        applyAttributes(dragon);
        for (Ability a : abilities()) a.initialize(dragon);
    }

    public void close(TameableDragon dragon)
    {
        dragon.getAttributes().assignValues(new AttributeMap(TameableDragon.createAttributes().build())); // restore default attributes
        for (Ability a : abilities()) a.close(dragon);
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

    public ResourceLocation id(RegistryAccess reg)
    {
        return BreedRegistry.registry(reg).getKey(this);
    }

    public static String getTranslationKey(String resourceLocation)
    {
        return "dragon_breed." + resourceLocation.replace(':', '.');
    }

    public record ModelProperties(boolean middleTailScales, boolean tailHorns, boolean thinLegs)
    {
        public static final ModelProperties STANDARD = new ModelProperties(true, false, false);

        public static final Codec<ModelProperties> CODEC = RecordCodecBuilder.create(func -> func.group(
                Codec.BOOL.optionalFieldOf("middle_tail_scales", true).forGetter(ModelProperties::middleTailScales),
                Codec.BOOL.optionalFieldOf("tail_horns", false).forGetter(ModelProperties::tailHorns),
                Codec.BOOL.optionalFieldOf("thin_legs", false).forGetter(ModelProperties::thinLegs)
        ).apply(func, ModelProperties::new));
    }
}
