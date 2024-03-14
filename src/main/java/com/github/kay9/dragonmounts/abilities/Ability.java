package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Each ability type has its own number of factories. One type can have multiple factories
 * to specify behavior.
 * Factories create instances that are unique to each dragon entity
 * this is due to the fact that sometimes an ability needs entity-specific actions,
 * and sometimes we don't (so we memoize the instance instead of making dozens of copies)
 * For example:
 *  - Ability X is defined in the datapack to place blocks in Y radius for Dragon Breed A.
 *  - Ability X is defined in the datapack to place blocks in Z radius for Dragon Breed B.
 *  - Ability X also requires a cooldown timer that has to be specific to each entity.
 *  Breed A has:
 *   - Ability X:
 *      - final int radius = Y; // finals are Breed Specific variables
 *      - int cooldown = 50 // other variables are Dragon-entity specific.
 *  Breed B has:
 *   - Ability X:
 *      - final int radius = Z;
 *      - int cooldown = 1000
 *  <br>
 *  Same ability type, different constants, instance-based variables.
 *  <br>
 *  Other abilities that don't require the second two options are instead created as singletons and memoized.
 *  Other situations vary. Bottom line: Create factories based on needs!
 */
public interface Ability
{
    Map<ResourceLocation, Codec<? extends Factory<Ability>>> REGISTRY = new HashMap<>();

    Codec<Factory<Ability>> CODEC = ResourceLocation.CODEC.dispatch(Factory::type, REGISTRY::get);

    ResourceLocation FROST_WALKER = reg("frost_walker", FrostWalkerAbility.CODEC);
    ResourceLocation GREEN_TOES = reg("green_toes", GreenToesAbility.CODEC);
    ResourceLocation SNOW_STEPPER = reg("snow_stepper", SnowStepperAbility.CODEC);
    ResourceLocation HOT_FEET = reg("hot_feet", HotFeetAbility.CODEC);
    ResourceLocation REAPER_STEP = reg("reaper_step", ReaperStepAbility.CODEC);
    ResourceLocation HYDRO_STEP = reg("hydro_step", HydroStepAbility.CODEC);

    static <T extends Ability> ResourceLocation register(ResourceLocation name, Codec<? extends Factory<T>> codec)
    {
        REGISTRY.put(name, (Codec) codec); // hacky generics cast
        return name;
    }

    private static <T extends Ability> ResourceLocation reg(String name, Codec<? extends Factory<T>> codec)
    {
        return register(DragonMountsLegacy.id(name), codec);
    }

    default void initialize(TameableDragon dragon) {}

    default void close(TameableDragon dragon) {}

    default void tick(TameableDragon dragon) {}

    default void onMove(TameableDragon dragon) {}

    interface Factory<T extends Ability>
    {
        T create();

        ResourceLocation type();
    }

    static <T extends Ability> Factory<T> simpleFactory(ResourceLocation id, Supplier<T> factory)
    {
        return new Factory<T>()
        {
            @Override
            public T create()
            {
                return factory.get();
            }

            @Override
            public ResourceLocation type()
            {
                return id;
            }
        };
    }

    static <T extends Ability> Codec<Factory<T>> singleton(ResourceLocation id, T instance)
    {
        return Codec.unit(simpleFactory(id, () -> instance));
    }
}
