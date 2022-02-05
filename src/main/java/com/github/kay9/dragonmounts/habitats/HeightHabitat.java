package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record HeightHabitat(boolean inverse, int height) implements Habitat
{
    public static final Codec<HeightHabitat> CODEC = RecordCodecBuilder.create(func -> func.group(
            Codec.BOOL.fieldOf("inverse").forGetter(HeightHabitat::inverse),
            Codec.INT.fieldOf("height").forGetter(HeightHabitat::height)
    ).apply(func, HeightHabitat::new));

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int y = pos.getY();
        int max = height;
        return (inverse? y < max : y > max)? 3 : -2;
    }

    @Override
    public HabitatType type()
    {
        return Habitat.WORLD_HEIGHT;
    }
}
