package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public interface Ability
{
    Map<String, Codec<? extends Ability>> REGISTRY = new HashMap<>();

    Codec<Ability> CODEC = Codec.STRING.dispatch(Ability::type, REGISTRY::get);

    String FROST_WALKER = register("frost_walker", FrostWalkerAbility.CODEC);
    String GREEN_TOES = register("green_toes", GreenToesAbility.CODEC);
    String SNOW_STEPPER = register("snow_stepper", SnowStepperAbility.CODEC);
    String HOT_FEET = register("hot_feet", HotFeetAbility.CODEC);

    static String register(String name, Codec<? extends Ability> codec)
    {
        REGISTRY.put(name, codec);
        return name;
    }

    default void initialize(TameableDragon dragon) {}

    default void close(TameableDragon dragon) {}

    default void tick(TameableDragon dragon) {}

    default void onMove(TameableDragon dragon) {}

    String type();
}
