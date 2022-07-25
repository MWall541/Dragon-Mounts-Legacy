package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface Habitat
{
    Map<String, Codec<? extends Habitat>> REGISTRY = new HashMap<>();

    Codec<Habitat> CODEC = Codec.STRING.dispatch(Habitat::type, REGISTRY::get);

    String PICKY = register("picky", PickyHabitat.CODEC);
    String BIOMES = register("biome", BiomeHabitat.CODEC);
    String IN_FLUID = register("in_fluid", FluidHabitat.CODEC);
    String WORLD_HEIGHT = register("world_height", HeightHabitat.CODEC);
    String LIGHT = register("light", LightHabitat.CODEC);
    String NEARBY_BLOCKS = register("nearby_blocks", NearbyBlocksHabitat.CODEC);
    String DRAGON_BREATH = register("dragon_breath", DragonBreathHabitat.CODEC);

    static String register(String name, Codec<? extends Habitat> codec)
    {
        REGISTRY.put(name, codec);
        return name;
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

    String type();
}
