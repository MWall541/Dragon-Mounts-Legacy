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

    static String register(String name, Codec<? extends Ability> codec)
    {
        REGISTRY.put(name, codec);
        return name;
    }

    void tick(TameableDragon dragon);

    String type();
}
