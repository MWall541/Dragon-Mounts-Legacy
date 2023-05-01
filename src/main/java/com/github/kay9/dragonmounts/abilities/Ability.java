package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public interface Ability
{
    Map<String, Codec<? extends Ability>> REGISTRY = new HashMap<>();

    Codec<Ability> CODEC = Codec.STRING.dispatch(Ability::type, REGISTRY::get);

    String FROST_WALKER = register("frost_walker", FrostWalkerAbility.CODEC);
    String GREEN_TOES = register("green_toes", GreenToesAbility.CODEC);
    String SNOW_STEPPER = register("snow_stepper", SnowStepperAbility.CODEC);
    String FIRE_BREATH = register("fire_breath", FireBreathAbility.CODEC);
    String ICE_BREATH = register("ice_breath", IceBreathAbility.CODEC);

    static String register(String name, Codec<? extends Ability> codec)
    {
        REGISTRY.put(name, codec);
        return name;
    }

    default void initialize(TameableDragon dragon) {}

    default void close(TameableDragon dragon) {}

    default void tick(TameableDragon dragon) {}

    default void onMove(TameableDragon dragon) {}

    @Nonnull
    String type();

    interface Data
    {
        default void serialize(CompoundTag tag) {}
        default void deserialize(CompoundTag tag) {}
    }
}
