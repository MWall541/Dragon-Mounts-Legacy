package com.github.kay9.dragonmounts.dragon.egg.habitats;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public interface Habitat
{
    ResourceKey<Registry<MapCodec<? extends Habitat>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("habitat_type"));
    Codec<Habitat> CODEC = Codec.lazyInitialized(() -> DMLRegistry.HABITAT_REGISTRY.byNameCodec().dispatch(Habitat::codec, Function.identity()));

    static <T extends Habitat> RecordCodecBuilder<T, Integer> withPoints(int defaultTo, Function<T, Integer> getter)
    {
        return Codec.INT.optionalFieldOf("points", defaultTo).forGetter(getter);
    }

    static <T extends Habitat> RecordCodecBuilder<T, Float> withMultiplier(float defaultTo, Function<T, Float> getter)
    {
        return Codec.FLOAT.optionalFieldOf("point_multiplier", defaultTo).forGetter(getter);
    }

    int getHabitatPoints(Level level, BlockPos pos);

    MapCodec<? extends Habitat> codec();
}
