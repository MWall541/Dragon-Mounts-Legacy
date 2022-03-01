package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.habitats.FluidHabitat;
import com.github.kay9.dragonmounts.habitats.Habitat;
import com.github.kay9.dragonmounts.habitats.NearbyBlocksHabitat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DragonBreed(ResourceLocation id, int primaryColor, int secondaryColor, boolean showMiddleTailScales,
                          boolean showTailSpikes,
                          Map<Attribute, Double> attributes,
                          List<Ability> abilities, List<Habitat> habitats, ImmutableSet<String> immunities,
                          Optional<SoundEvent> specialSound, ResourceLocation deathLoot, int growthTime)
{
    public static final Codec<DragonBreed> CODEC = RecordCodecBuilder.create(func -> func.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(DragonBreed::id),
            Codec.INT.fieldOf("primary_color").forGetter(DragonBreed::primaryColor),
            Codec.INT.fieldOf("secondary_color").forGetter(DragonBreed::secondaryColor),
            Codec.BOOL.optionalFieldOf("show_middle_tail_scales", true).forGetter(DragonBreed::showMiddleTailScales),
            Codec.BOOL.optionalFieldOf("show_tail_spikes", false).forGetter(DragonBreed::showTailSpikes),
            Codec.unboundedMap(Registry.ATTRIBUTE.byNameCodec(), Codec.DOUBLE).optionalFieldOf("attributes", ImmutableMap.of()).forGetter(DragonBreed::attributes),
            Ability.CODEC.listOf().optionalFieldOf("abilities", ImmutableList.of()).forGetter(DragonBreed::abilities),
            Habitat.CODEC.listOf().optionalFieldOf("habitats", ImmutableList.of()).forGetter(DragonBreed::habitats),
            Codec.STRING.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf).optionalFieldOf("immunities", ImmutableSet.of()).forGetter(DragonBreed::immunities), // convert to Set for "contains" performance
            SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(DragonBreed::specialSound),
            ResourceLocation.CODEC.optionalFieldOf("death_loot", BuiltInLootTables.EMPTY).forGetter(DragonBreed::deathLoot),
            Codec.INT.optionalFieldOf("growth_time", TameableDragon.DEFAULT_GROWTH_TIME).forGetter(DragonBreed::growthTime)
    ).apply(func, DragonBreed::new));

    /**
     * Internal use only. For built-in fallbacks and data generation.
     */
    public static final DragonBreed FIRE = new DragonBreed(DragonMountsLegacy.id("fire"),
            0x912400,
            0xff9819,
            false,
            false,
            ImmutableMap.of(),
            ImmutableList.of(),
            ImmutableList.of(new NearbyBlocksHabitat(BlockTags.createOptional(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))), new FluidHabitat(FluidTags.LAVA)),
            ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
            Optional.empty(),
            BuiltInLootTables.EMPTY,
            TameableDragon.DEFAULT_GROWTH_TIME);

    @Nullable
    public SoundEvent getAmbientSound()
    {
        return specialSound().orElse(null);
    }

    public String getTranslationKey()
    {
        return "dragon_breed." + id().getNamespace() + "." + id().getPath();
    }

    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int points = 0;
        for (Habitat habitat : habitats()) points += habitat.getHabitatPoints(level, pos);
        return points;
    }
}
