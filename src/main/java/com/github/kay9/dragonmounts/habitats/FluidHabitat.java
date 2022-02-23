package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public record FluidHabitat(Tag<Fluid> fluidType) implements Habitat
{
    public static final Codec<FluidHabitat> CODEC = Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.FLUID_REGISTRY))
            .fieldOf("fluid_tag")
            .xmap(FluidHabitat::new, FluidHabitat::fluidType)
            .codec();

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        return (int) BlockPos.betweenClosedStream(pos.offset(1, 1, 1), pos.offset(-1, -1, -1))
                .filter(p -> level.getFluidState(p).is(fluidType))
                .count() / 2;
    }

    @Override
    public String type()
    {
        return Habitat.IN_FLUID;
    }
}
