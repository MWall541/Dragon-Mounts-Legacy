package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public enum DragonBreathHabitat implements Habitat
{
    INSTANCE;

    public static final MapCodec<DragonBreathHabitat> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        return !level.getEntities(EntityType.AREA_EFFECT_CLOUD,
                new AABB(pos),
                c -> c.getParticle() == ParticleTypes.DRAGON_BREATH).isEmpty()? 10 : 0;
    }

    @Override
    public ResourceLocation type()
    {
        return Habitat.DRAGON_BREATH;
    }
}
