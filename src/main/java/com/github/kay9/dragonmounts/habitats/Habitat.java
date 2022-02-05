package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public interface Habitat
{
    Map<String, HabitatType> REGISTRY = new HashMap<>();

    HabitatType BIOMES = register("biome", BiomeHabitat.CODEC);
    HabitatType IN_FLUID = register("in_fluid", FluidHabitat.CODEC);
    HabitatType WORLD_HEIGHT = register("world_height", HeightHabitat.CODEC);
    HabitatType LIGHT = register("light", LightHabitat.CODEC);
    HabitatType NEARBY_BLOCKS = register("nearby_blocks", NearbyBlocksHabitat.CODEC);

    Codec<Habitat> CODEC = Codec.STRING.xmap(REGISTRY::get, HabitatType::name).dispatch(Habitat::type, HabitatType::codec);

    static HabitatType register(String name, Codec<? extends Habitat> codec)
    {
        HabitatType habitatType = new HabitatType(name, codec);
        REGISTRY.put(name, habitatType);
        return habitatType;
    }

    int getHabitatPoints(Level level, BlockPos pos);

    HabitatType type();

    record HabitatType(String name, Codec<? extends Habitat> codec) {}
}
