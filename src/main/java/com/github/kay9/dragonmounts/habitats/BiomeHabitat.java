package com.github.kay9.dragonmounts.habitats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record BiomeHabitat(ImmutableSet<ResourceKey<Biome>> biomes) implements Habitat
{
    public static final Codec<BiomeHabitat> CODEC = ResourceKey.codec(Registry.BIOME_REGISTRY)
            .listOf()
            .fieldOf("biomes")
            .xmap(l -> new BiomeHabitat(ImmutableSet.copyOf(l)), l -> ImmutableList.copyOf(l.biomes())) // convert to Set for "contains" performance
            .codec();

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        return level.getBiomeName(pos).map(b -> biomes.contains(b)? 4 : 0).orElse(0);
    }

    @Override
    public String type()
    {
        return Habitat.BIOMES;
    }
}
