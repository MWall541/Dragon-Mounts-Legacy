package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.function.Function;
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
 *  If the ability DOES NOT require per-entity data, it may be better to off to implement {@link Factory} directly and
 *  return itself.
 */
//todo registry based
public interface Ability
{
    ResourceKey<Registry<MapCodec<? extends Factory<? extends Ability>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("ability_type"));
    Supplier<IForgeRegistry<MapCodec<? extends Factory<? extends Ability>>>> REGISTRY = Suppliers.memoize(() -> RegistryManager.ACTIVE.getRegistry(REGISTRY_KEY));
    Codec<Factory<? extends Ability>> CODEC = Codec.lazyInitialized(() -> REGISTRY.get().getCodec().dispatch(Factory::codec, Function.identity()));

    default void initialize(TameableDragon dragon) {}

    default void close(TameableDragon dragon) {}

    default void write(TameableDragon dragon, CompoundTag nbt) {}

    default void read(TameableDragon dragon, CompoundTag nbt) {}

    default void tick(TameableDragon dragon) {}

    /**
     * Only called on the server
     */
    default void onMove(TameableDragon dragon) {}

    /**
     * The Ability Factory is responsible for creating the instances of an ability.
     * If an ability is meant to be breed specific and needs no per-entity data, this interface can be implemented
     * directly by the ability class, and return itself in {@link Factory#create}. This effectively removes the need for
     * a new factory type instance that just returns a new 'effective singleton', which is unnecessary for memory.
     * Most abilities in DML are simplistic in nature and follow this approach. {@link FrostWalkerAbility} is a good
     * example.
     * <br><br>
     * It is crucially important that if you are extending a class already implementing {@link Factory} that
     * you override {@link Factory#create} and {@link Factory#codec}. <br>
     * If the parent ability class is a breed specific type, and your type is an entity specific, {@link Factory#create} and
     * {@link Factory#codec} do not matter since your codec SHOULD NOT USE THEM! You MUST create a new factory type for per-entity
     * implementations, or use {@link Ability#simpleFactory}
     */
    interface Factory<T extends Ability>
    {
        /**
         *
         * @return an ability instance of type T
         */
        T create();

        MapCodec<? extends Factory<? extends Ability>> codec();
    }

//    /**
//     * While this is here, I don't necessarily recommend using it because the type has to be created to serialize
//     * its values, but it's not exactly a deal-breaker at data-generation. Just... impractical. <br>
//     * Example usage:
//     * <pre>
//     * {@code Codec<Factory<MyAbility>> CODEC = Codec.FLOAT
//     *  .xmap(myFloat -> Ability.simpleFactory(() -> new MyAbility(myfloat)), myFactory -> myFactory.create().myFloat)
//     *  .fieldOf("my_float")
//     *  .codec();}
//     * </pre>
//     */
//    static <T extends Ability> Factory<T> simpleFactory(ResourceLocation id, Supplier<T> factory)
//    {
//        return new Factory<>()
//        {
//            @Override
//            public T create()
//            {
//                return factory.get();
//            }
//
//            @Override
//            public ResourceLocation codec()
//            {
//                return id;
//            }
//        };
//    }
}
