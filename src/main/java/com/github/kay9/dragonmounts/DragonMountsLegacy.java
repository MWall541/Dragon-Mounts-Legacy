package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.CrossBreedingManager;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.abilities.*;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.habitats.*;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dragon Mounts Legacy - Fabric 26.2
 * <br>
 * Main mod initializer. All registration and event setup is done here.
 */
public class DragonMountsLegacy implements ModInitializer
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    /** Cached server reference for server-side registry access. Null on dedicated clients. */
    public static MinecraftServer SERVER;

    public static Identifier id(String path)
    {
        return Identifier.tryBuild(MOD_ID, path);
    }

    @Override
    public void onInitialize()
    {
        // Load config
        DMLConfig.load();

        // Build custom registries for Ability and Habitat types
        buildCustomRegistries();

        // Register the data-driven dragon breed registry (synced to clients with the slimmer network codec)
        net.fabricmc.fabric.api.event.registry.DynamicRegistries.registerSynced(
                DragonBreed.REGISTRY_KEY, DragonBreed.DIRECT_CODEC, DragonBreed.NETWORK_CODEC);

        // Register ability types
        registerAbilityTypes();

        // Register habitat types
        registerHabitatTypes();

        // Force-load DMLRegistry (triggers all static Registry.register calls)
        DMLRegistry.init();

        // Initialize egg chance defaults (uses BUILT_IN_CHANCES from DragonEggLootMod)
        DMLConfig.initEggChanceDefaults();

        // Register Fabric loot table modification for dragon eggs
        DragonEggLootMod.register();

        // Register entity data serializers
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry.register(
                id("dragon_breed"), TameableDragon.DRAGON_BREED_SERIALIZER);

        // Register server-side reload listeners
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(CrossBreedingManager.INSTANCE);

        // Handle vanilla dragon egg override
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
        {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (overrideVanillaDragonEgg(world, hitResult.getBlockPos(), player))
                return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        });

        // Prevent destruction of un-hatching end dragon eggs (they teleport away instead)
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register(
                (world, player, pos, state, blockEntity) -> HatchableEggBlock.canDestroy(world, player, pos, state));

        // Cache server reference for registry access in shared code
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);

        // Register creative tab population
        // In Fabric, creative tab items are added via ItemGroupEvents
        registerCreativeTabItems();

        LOG.info("Dragon Mounts: Legacy initialized on Fabric.");
    }

    // ========================
    //   Custom Registries
    // ========================

    private void buildCustomRegistries()
    {
        DMLRegistry.ABILITY_REGISTRY = FabricRegistryBuilder.create(Ability.REGISTRY_KEY)
                .buildAndRegister();

        DMLRegistry.HABITAT_REGISTRY = FabricRegistryBuilder.create(Habitat.REGISTRY_KEY)
                .buildAndRegister();
    }

    private void registerAbilityTypes()
    {
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("frost_walker"), FrostWalkerAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("green_toes"), GreenToesAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("snow_stepper"), SnowStepperAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("hot_feet"), HotFeetAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("reaper_step"), ReaperStepAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("hydro_step"), HydroStepAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("crystal_growth"), CrystalGrowthAbility.CODEC);
        Registry.register(DMLRegistry.ABILITY_REGISTRY, id("echolocation"), EcholocationAbility.CODEC);
    }

    private void registerHabitatTypes()
    {
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("picky"), PickyHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("biome"), BiomeHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("in_fluid"), FluidHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("world_height"), HeightHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("light"), LightHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("nearby_blocks"), NearbyBlocksHabitat.CODEC);
        Registry.register(DMLRegistry.HABITAT_REGISTRY, id("dragon_breath"), DragonBreathHabitat.CODEC);
    }

    // ========================
    //   Creative Tab Items
    // ========================

    private void registerCreativeTabItems()
    {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> DragonSpawnEgg.populateTab(output::accept, output.getContext().holders()));

        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(output -> HatchableEggBlock.populateTab(output::accept, output.getContext().holders()));
    }

    // ========================
    //       Game Events
    // ========================

    static boolean overrideVanillaDragonEgg(Level level, BlockPos pos, Player player)
    {
        if (DMLConfig.allowEggOverride() && level.getBlockState(pos).is(Blocks.DRAGON_EGG))
        {
            var end = DragonBreed.registry(level.registryAccess()).get(DragonBreed.BuiltIn.END);
            if (end.isPresent())
            {
                if (level.isClientSide()) player.swing(InteractionHand.MAIN_HAND);
                else
                {
                    var state = DMLRegistry.EGG_BLOCK.defaultBlockState().setValue(HatchableEggBlock.HATCHING, true);
                    HatchableEggBlock.place((ServerLevel) level, pos, state, end.get());
                }
                return true;
            }
        }
        return false;
    }
}
