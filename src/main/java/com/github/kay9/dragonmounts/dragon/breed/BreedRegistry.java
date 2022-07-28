package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class BreedRegistry
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));
    public static final DeferredRegister<DragonBreed> DEFERRED_REGISTRY = DeferredRegister.create(REGISTRY_KEY, DragonMountsLegacy.MOD_ID);
    public static final Supplier<IForgeRegistry<DragonBreed>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(() -> new RegistryBuilder<DragonBreed>()
            .disableSaving()
            .dataPackRegistry(DragonBreed.CODEC, DragonBreed.NETWORK_CODEC)
            .setDefaultKey(DragonBreed.FIRE.getId()));
    public static final Codec<DragonBreed> CODEC = ResourceLocation.CODEC.xmap(BreedRegistry::get, DragonBreed::id); //todo log an error on unknown breeds instead...

    public static DragonBreed get(String byString)
    {
        return get(new ResourceLocation(byString));
    }

    public static DragonBreed get(ResourceLocation byId)
    {
        var breed = getNullable(byId);
        return breed == null? DragonBreed.FIRE.get() : breed;
    }

    public static DragonBreed getNullable(ResourceLocation byId)
    {
        return retrieveRegistry().get(byId);
    }

    public static Iterable<DragonBreed> values()
    {
        return retrieveRegistry();
    }

    public static Registry<DragonBreed> retrieveRegistry()
    {
        var server = ServerLifecycleHooks.getCurrentServer();
        return (server == null? BuiltinRegistries.ACCESS : server.registryAccess()).registryOrThrow(REGISTRY_KEY);
    }
}
