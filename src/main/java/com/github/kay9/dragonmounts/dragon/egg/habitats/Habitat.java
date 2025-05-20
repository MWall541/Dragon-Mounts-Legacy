package com.github.kay9.dragonmounts.dragon.egg.habitats;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.util.DMLUtil;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Habitat
{
    ResourceKey<Registry<MapCodec<? extends Habitat>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("habitat_type"));
    Codec<Habitat> CODEC = Codec.lazyInitialized(() -> DMLUtil.castRegistry(REGISTRY_KEY).byNameCodec().dispatch(Habitat::codec, Function.identity()));

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
