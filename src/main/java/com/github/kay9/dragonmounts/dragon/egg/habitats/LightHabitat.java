package com.github.kay9.dragonmounts.dragon.egg.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record LightHabitat(int points, boolean below, int light) implements Habitat
{
    public static final MapCodec<LightHabitat> CODEC = RecordCodecBuilder.mapCodec(func -> func.group(
            Habitat.withPoints(3, LightHabitat::points),
            Codec.BOOL.optionalFieldOf("below", false).forGetter(LightHabitat::below),
            Codec.INT.fieldOf("light").forGetter(LightHabitat::light)
    ).apply(func, LightHabitat::new));

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int lightEmission = level.getLightEmission(pos);
        return (below? lightEmission < light : lightEmission > light)? points : 0;
    }

    @Override
    public MapCodec<? extends Habitat> codec()
    {
        return CODEC;
    }
}
