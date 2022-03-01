package com.github.kay9.dragonmounts.habitats;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public enum DragonBreathHabitat implements Habitat
{
    INSTANCE;

    public static final Codec<DragonBreathHabitat> CODEC = Codec.unit(INSTANCE);

    @Override
    public int getHabitatPoints(Level level, BlockPos pos)
    {
        return !level.getEntities(EntityType.AREA_EFFECT_CLOUD,
                new AABB(pos),
                c -> c.getParticle() == ParticleTypes.DRAGON_BREATH).isEmpty()? 10 : 0;
    }

    @Override
    public String type()
    {
        return Habitat.DRAGON_BREATH;
    }
}
