package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record LightHabitat(boolean inverse, int light) implements Habitat
{
    public static final Codec<LightHabitat> CODEC = RecordCodecBuilder.create(func -> func.group(
            Codec.BOOL.fieldOf("inverse").forGetter(LightHabitat::inverse),
            Codec.INT.fieldOf("light").forGetter(LightHabitat::light)
    ).apply(func, LightHabitat::new));

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int lightEmission = level.getLightEmission(pos);
        if (inverse)
        {
            if (!level.canSeeSky(pos) && lightEmission < light) return 4;
        }
        else if (lightEmission > light) return 3;

        return -2;
    }

    @Override
    public HabitatType type()
    {
        return Habitat.LIGHT;
    }
}
