package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.habitats.Habitat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class DragonBreed extends ForgeRegistryEntry.UncheckedRegistryEntry<DragonBreed>
{
    public static final Codec<DragonBreed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(DragonBreed::getRegistryName),
            Codec.INT.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            Codec.INT.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            ModelProperties.CODEC.optionalFieldOf("model_properties", ModelProperties.STANDARD).forGetter(DragonBreed::modelProperties),
            Codec.unboundedMap(Registry.ATTRIBUTE.byNameCodec(), Codec.DOUBLE).optionalFieldOf("attributes", ImmutableMap.of()).forGetter(DragonBreed::attributes),
            Ability.CODEC.listOf().optionalFieldOf("abilities", ImmutableList.of()).forGetter(DragonBreed::abilities),
            Habitat.CODEC.listOf().optionalFieldOf("habitats", ImmutableList.of()).forGetter(DragonBreed::habitats),
            Codec.STRING.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf).optionalFieldOf("immunities", ImmutableSet.of()).forGetter(DragonBreed::immunities), // convert to Set for "contains" performance
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::specialSound),
            ResourceLocation.CODEC.optionalFieldOf("death_loot", BuiltInLootTables.EMPTY).forGetter(DragonBreed::deathLoot),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.DEFAULT_GROWTH_TIME).forGetter(DragonBreed::growthTime),
            Codec.INT.optionalFieldOf("hatch_time", DragonEgg.DEFAULT_HATCH_TIME).forGetter(DragonBreed::hatchTime),
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("taming_items", ItemTags.FISHES).forGetter(DragonBreed::tamingItems),
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("breeding_items", ItemTags.FISHES).forGetter(DragonBreed::breedingItems)
    ).apply(instance, DragonBreed::named));

    public static final Codec<DragonBreed> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(DragonBreed::getRegistryName),
            Codec.INT.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            Codec.INT.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            ParticleTypes.CODEC.optionalFieldOf("hatch_particles").forGetter(DragonBreed::hatchParticles),
            ModelProperties.CODEC.fieldOf("model_properties").forGetter(DragonBreed::modelProperties),
            Codec.INT.fieldOf("growth_time").forGetter(DragonBreed::growthTime),
            Codec.INT.fieldOf("hatch_time").forGetter(DragonBreed::hatchTime)
    ).apply(instance, DragonBreed::fromNetwork));

    private static final TagKey<Item> EMPTY_TAG = ItemTags.create(DragonMountsLegacy.id("empty")); // dummy for client instances

    private final int primaryColor;
    private final int secondaryColor;
    private final Optional<ParticleOptions> hatchParticles;
    private final ModelProperties modelProperties;
    private final Map<Attribute, Double> attributes;
    private final List<Ability> abilities;
    private final List<Habitat> habitats;
    private final ImmutableSet<String> immunities;
    private final Optional<SoundEvent> specialSound;
    private final ResourceLocation deathLoot;
    private final int growthTime;
    private final int hatchTime;
    private final TagKey<Item> tamingItems;
    private final TagKey<Item> breedingItems;

    public DragonBreed(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> specialSound, ResourceLocation deathLoot, int growthTime, int hatchTime, TagKey<Item> tamingItems, TagKey<Item> breedingItems)
    {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.hatchParticles = hatchParticles;
        this.modelProperties = modelProperties;
        this.attributes = attributes;
        this.abilities = abilities;
        this.habitats = habitats;
        this.immunities = immunities;
        this.specialSound = specialSound;
        this.deathLoot = deathLoot;
        this.growthTime = growthTime;
        this.hatchTime = hatchTime;
        this.tamingItems = tamingItems;
        this.breedingItems = breedingItems;
    }

    public static DragonBreed named(ResourceLocation name, int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> specialSound, ResourceLocation deathLoot, int growthTime, int hatchTime, TagKey<Item> tamingFood, TagKey<Item> breedingItems)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, modelProperties, attributes, abilities, habitats, immunities, specialSound, deathLoot, growthTime, hatchTime, tamingFood, breedingItems)
                .setRegistryName(name);
    }

    public static DragonBreed builtInUnnamed(int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> specialSound)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, modelProperties, attributes, abilities, habitats, immunities, specialSound, BuiltInLootTables.EMPTY, TameableDragon.DEFAULT_GROWTH_TIME, DragonEgg.DEFAULT_HATCH_TIME, ItemTags.FISHES, ItemTags.FISHES);
    }

    public static DragonBreed builtIn(ResourceLocation name, int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, Map<Attribute, Double> attributes, List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities, Optional<SoundEvent> specialSound)
    {
        return builtInUnnamed(primaryColor, secondaryColor, hatchParticles, modelProperties, attributes, abilities, habitats, immunities, specialSound)
                .setRegistryName(name);
    }

    public static DragonBreed fromNetwork(ResourceLocation name, int primaryColor, int secondaryColor, Optional<ParticleOptions> hatchParticles, ModelProperties modelProperties, int growthTime, int hatchTime)
    {
        return new DragonBreed(primaryColor, secondaryColor, hatchParticles, modelProperties, Map.of(), List.of(), List.of(), ImmutableSet.of(), Optional.empty(), BuiltInLootTables.EMPTY, growthTime, hatchTime, EMPTY_TAG, EMPTY_TAG)
                .setRegistryName(name);
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

    public ParticleOptions getHatchParticles(Random random)
    {
        return hatchParticles().orElseGet(() -> getDustParticles(random));
    }

    public DustParticleOptions getDustParticles(Random random)
    {
        return new DustParticleOptions(new Vector3f(Vec3.fromRGB24(random.nextDouble() < 0.75? primaryColor() : secondaryColor())), 1);
    }

    @Nullable
    public SoundEvent getAmbientSound()
    {
        return specialSound().orElse(null);
    }

    public String getTranslationKey()
    {
        return "dragon_breed." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
    }

    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int points = 0;
        for (Habitat habitat : habitats()) points += habitat.getHabitatPoints(level, pos);
        return points;
    }

    private void applyAttributes(TameableDragon dragon)
    {
        float healthPercentile = dragon.getHealth() / dragon.getMaxHealth();

        //todo: use attributes().replaceFrom instead
        attributes().forEach((att, value) ->
        {
            AttributeInstance inst = dragon.getAttribute(att);
            if (inst != null) inst.setBaseValue(value);
        });

        dragon.setHealth(dragon.getMaxHealth() * healthPercentile); // in case we have less than max health
    }

    public int primaryColor()
    {
        return primaryColor;
    }

    public int secondaryColor()
    {
        return secondaryColor;
    }

    public Optional<ParticleOptions> hatchParticles()
    {
        return hatchParticles;
    }

    public ModelProperties modelProperties()
    {
        return modelProperties;
    }

    public Map<Attribute, Double> attributes()
    {
        return attributes;
    }

    public List<Ability> abilities()
    {
        return abilities;
    }

    public List<Habitat> habitats()
    {
        return habitats;
    }

    public ImmutableSet<String> immunities()
    {
        return immunities;
    }

    public Optional<SoundEvent> specialSound()
    {
        return specialSound;
    }

    public ResourceLocation deathLoot()
    {
        return deathLoot;
    }

    public int growthTime()
    {
        return growthTime;
    }

    public int hatchTime()
    {
        return hatchTime;
    }

    public TagKey<Item> tamingItems()
    {
        return tamingItems;
    }

    public TagKey<Item> breedingItems()
    {
        return breedingItems;
    }

    @Override
    public String toString()
    {
        return "DragonBreed{name=\"" + getRegistryName() + "\"}";
    }

    public static record ModelProperties(boolean middleTailScales, boolean tailHorns, boolean thinLegs)
    {
        public static final ModelProperties STANDARD = new ModelProperties(true, false, false);

        public static Codec<ModelProperties> CODEC = RecordCodecBuilder.create(func -> func.group(
                Codec.BOOL.optionalFieldOf("middle_tail_scales", true).forGetter(ModelProperties::middleTailScales),
                Codec.BOOL.optionalFieldOf("tail_horns", false).forGetter(ModelProperties::tailHorns),
                Codec.BOOL.optionalFieldOf("thin_legs", false).forGetter(ModelProperties::thinLegs)
        ).apply(func, ModelProperties::new));
    }
}
