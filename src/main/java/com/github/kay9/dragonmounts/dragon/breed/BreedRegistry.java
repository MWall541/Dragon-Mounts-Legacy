package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class BreedRegistry
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));

    @Nullable
    public static DragonBreed get(String byString, RegistryAccess reg)
    {
        return get(new ResourceLocation(byString), reg);
    }

    @Nullable
    public static DragonBreed get(ResourceLocation byId, RegistryAccess reg)
    {
        return registry(reg).get(byId);
    }

    public static DragonBreed getRandom(RegistryAccess reg, RandomSource random)
    {
        return registry(reg).getRandom(random).orElseThrow().get();
    }

    public static Registry<DragonBreed> registry(RegistryAccess reg)
    {
        return reg.registry(REGISTRY_KEY).orElseGet(() -> RegistryAccess.EMPTY.registryOrThrow(REGISTRY_KEY));
    }
}
