package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.HotFeetAbility;
import com.github.kay9.dragonmounts.habitats.FluidHabitat;
import com.github.kay9.dragonmounts.habitats.NearbyBlocksHabitat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Supplier;

public class BreedRegistry
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));
    public static final DeferredRegister<DragonBreed> DEFERRED_REGISTRY = DeferredRegister.create(REGISTRY_KEY, DragonMountsLegacy.MOD_ID);
    public static final RegistryObject<DragonBreed> FIRE_BUILTIN = BreedRegistry.DEFERRED_REGISTRY.register("fire", () -> DragonBreed.builtIn(
            0x912400,
            0xff9819,
            Optional.of(ParticleTypes.FLAME),
            new DragonBreed.ModelProperties(false, false, false),
            ImmutableMap.of(),
            ImmutableList.of(HotFeetAbility.INSTANCE),
            ImmutableList.of(
                    new NearbyBlocksHabitat(1, BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))),
                    new FluidHabitat(3, FluidTags.LAVA)),
            ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
            Optional.empty()));
    public static final Supplier<IForgeRegistry<DragonBreed>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(DragonBreed.class, () -> new RegistryBuilder<DragonBreed>()
            .disableSaving()
            .dataPackRegistry(DragonBreed.CODEC, DragonBreed.NETWORK_CODEC)
            .setDefaultKey(FIRE_BUILTIN.getId()));

    public static DragonBreed get(String byString, RegistryAccess reg)
    {
        return get(new ResourceLocation(byString), reg);
    }

    public static DragonBreed get(ResourceLocation byId, RegistryAccess reg)
    {
        var breed = registry(reg).get(byId);
        if (breed == null) breed = getFallback(reg); // guard for if/when the registry is not defaulted...
        return breed;
    }

    public static DragonBreed getFallback(RegistryAccess reg)
    {
        return get(FIRE_BUILTIN.getId(), reg);
    }

    public static Registry<DragonBreed> registry(RegistryAccess reg)
    {
        return reg.registryOrThrow(REGISTRY_KEY);
    }
}
