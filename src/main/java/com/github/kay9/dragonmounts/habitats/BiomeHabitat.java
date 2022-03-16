package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record BiomeHabitat(TagKey<Biome> biomeTag) implements Habitat
{
    public static final Codec<BiomeHabitat> CODEC = TagKey.codec(Registry.BIOME_REGISTRY)
            .fieldOf("biome_tag")
            .xmap(BiomeHabitat::new, BiomeHabitat::biomeTag)
            .codec();

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        return level.getBiome(pos).is(biomeTag)? 4 : 0;
    }

    @Override
    public String type()
    {
        return Habitat.BIOMES;
    }
}
