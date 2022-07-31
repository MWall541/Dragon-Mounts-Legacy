package com.github.kay9.dragonmounts.dragon.breed;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.habitats.FluidHabitat;
import com.github.kay9.dragonmounts.habitats.NearbyBlocksHabitat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.function.Supplier;

public class BreedRegistry
{
    public static final ResourceKey<Registry<DragonBreed>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonMountsLegacy.id("dragon_breeds"));
    public static final DeferredRegister<DragonBreed> DEFERRED_REGISTRY = DeferredRegister.create(REGISTRY_KEY, DragonMountsLegacy.MOD_ID);
    public static final Supplier<IForgeRegistry<DragonBreed>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(() -> new RegistryBuilder<DragonBreed>()
            .disableSaving()
            .dataPackRegistry(DragonBreed.CODEC, DragonBreed.NETWORK_CODEC)
            .setDefaultKey(DragonMountsLegacy.id("fire")));
    public static final Codec<DragonBreed> CODEC = ResourceLocation.CODEC.xmap(BreedRegistry::get, DragonBreed::id)
            .promotePartial(err -> DragonMountsLegacy.LOG.error("Unknown Dragon Breed Type: {}", err));

    /**
     * Internal use only. For built-in fallbacks and data generation.
     */
    public static final RegistryObject<DragonBreed> FIRE_BUILTIN = BreedRegistry.DEFERRED_REGISTRY.register("fire", () -> new DragonBreed(
            0x912400,
            0xff9819,
            Optional.of(ParticleTypes.FLAME),
            new DragonBreed.ModelProperties(false, false, false),
            ImmutableMap.of(),
            ImmutableList.of(),
            ImmutableList.of(new NearbyBlocksHabitat(1, BlockTags.create(DragonMountsLegacy.id("fire_dragon_habitat_blocks"))), new FluidHabitat(3, FluidTags.LAVA)),
            ImmutableSet.of("onFire", "inFire", "lava", "hotFloor"),
            Optional.empty(),
            BuiltInLootTables.EMPTY,
            TameableDragon.DEFAULT_GROWTH_TIME,
            DragonEgg.DEFAULT_HATCH_TIME));
    public static final Supplier<DragonBreed> FIRE = () -> get(FIRE_BUILTIN.getId());

    public static DragonBreed get(String byString)
    {
        return get(new ResourceLocation(byString));
    }

    public static DragonBreed get(ResourceLocation byId)
    {
        var breed = registry().get(byId);
        if (breed == null) breed = FIRE.get(); // guard for if/when the registry is not defaulted...
        return breed;
    }

    public static Registry<DragonBreed> registry()
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
