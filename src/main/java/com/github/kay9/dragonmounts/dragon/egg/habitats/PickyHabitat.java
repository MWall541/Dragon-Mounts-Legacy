package com.github.kay9.dragonmounts.dragon.egg.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

public record PickyHabitat(List<Habitat> habitats) implements Habitat
{
    public static final MapCodec<PickyHabitat> CODEC = Habitat.CODEC
            .listOf()
            .fieldOf("required_habitats")
            .xmap(PickyHabitat::new, PickyHabitat::habitats);

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        int points = 0;
        for (var habitat : habitats)
        {
            int i = habitat.getHabitatPoints(level, pos);
            if (i == 0) return 0; // ALL habitat conditions must be met. Otherwise, nope.
            points += i;
        }
        return points;
    }

    @Override
    public MapCodec<? extends Habitat> codec()
    {
        return CODEC;
    }
}
