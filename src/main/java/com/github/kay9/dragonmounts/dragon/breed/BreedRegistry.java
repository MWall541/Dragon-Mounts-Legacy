package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class BreedRegistry
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));
    public static final DeferredRegister<DragonBreed> DEFERRED_REGISTRY = DeferredRegister.create(REGISTRY_KEY, DragonMountsLegacy.MOD_ID);
    public static final Supplier<IForgeRegistry<DragonBreed>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(DragonBreed.class, () -> new RegistryBuilder<DragonBreed>()
            .disableSaving()
            .dataPackRegistry(DragonBreed.CODEC, DragonBreed.NETWORK_CODEC)
            .setDefaultKey(DragonBreed.FIRE.getId()));

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

    private static Registry<DragonBreed> retrieveRegistry()
    {
        return (switch(FMLLoader.getDist())
        {
            case CLIENT:
            {
                if (Minecraft.getInstance().level != null)
                    yield Minecraft.getInstance().level.registryAccess();
            }
            case DEDICATED_SERVER:
            {
                if (ServerLifecycleHooks.getCurrentServer() != null)
                    yield ServerLifecycleHooks.getCurrentServer().registryAccess();
            }
            yield BuiltinRegistries.ACCESS;
        }
        ).registryOrThrow(REGISTRY_KEY);
    }
}
