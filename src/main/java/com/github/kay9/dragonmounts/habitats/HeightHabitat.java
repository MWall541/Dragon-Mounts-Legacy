package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record HeightHabitat(int points, boolean below, int height) implements Habitat
{
    public static final Codec<HeightHabitat> CODEC = RecordCodecBuilder.create(func -> func.group(
            Habitat.withPoints(3, HeightHabitat::points),
            Codec.BOOL.optionalFieldOf("below", false).forGetter(HeightHabitat::below),
            Codec.INT.fieldOf("height").forGetter(HeightHabitat::height)
    ).apply(func, HeightHabitat::new));

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int y = pos.getY();
        int max = height;
        return (below? (y < max && !level.canSeeSky(pos)) : y > max)? points : 0;
    }

    @Override
    public String type()
    {
        return Habitat.WORLD_HEIGHT;
    }
}
