package com.github.kay9.dragonmounts.habitats;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface Habitat
{
    Map<ResourceLocation, Codec<? extends Habitat>> REGISTRY = new HashMap<>();

    Codec<Habitat> CODEC = ResourceLocation.CODEC.dispatch(Habitat::type, REGISTRY::get);

    ResourceLocation PICKY = reg("picky", PickyHabitat.CODEC);
    ResourceLocation BIOMES = reg("biome", BiomeHabitat.CODEC);
    ResourceLocation IN_FLUID = reg("in_fluid", FluidHabitat.CODEC);
    ResourceLocation WORLD_HEIGHT = reg("world_height", HeightHabitat.CODEC);
    ResourceLocation LIGHT = reg("light", LightHabitat.CODEC);
    ResourceLocation NEARBY_BLOCKS = reg("nearby_blocks", NearbyBlocksHabitat.CODEC);
    ResourceLocation DRAGON_BREATH = reg("dragon_breath", DragonBreathHabitat.CODEC);

    static ResourceLocation register(ResourceLocation name, Codec<? extends Habitat> codec)
    {
        REGISTRY.put(name, codec);
        return name;
    }

    private static ResourceLocation reg(String name, Codec<? extends Habitat> codec)
    {
        return register(DragonMountsLegacy.id(name), codec);
    }

    static <T extends Habitat> RecordCodecBuilder<T, Integer> withPoints(int defaultTo, Function<T, Integer> getter)
    {
        return Codec.INT.optionalFieldOf("points", defaultTo).forGetter(getter);
    }

    static <T extends Habitat> RecordCodecBuilder<T, Float> withMultiplier(float defaultTo, Function<T, Float> getter)
    {
        return Codec.FLOAT.optionalFieldOf("point_multiplier", defaultTo).forGetter(getter);
    }

    int getHabitatPoints(Level level, BlockPos pos);

    ResourceLocation type();
}
