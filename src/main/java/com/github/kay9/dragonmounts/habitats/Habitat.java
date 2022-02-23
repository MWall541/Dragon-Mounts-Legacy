package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

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

    static String register(String name, Codec<? extends Habitat> codec)
    {
        REGISTRY.put(name, codec);
        return name;
    }

    int getHabitatPoints(Level level, BlockPos pos);

    String type();
}
