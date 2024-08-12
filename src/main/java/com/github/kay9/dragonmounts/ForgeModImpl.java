package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.MountCameraManager;
import com.github.kay9.dragonmounts.dragon.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.egg.habitats.Habitat;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;

import static com.github.kay9.dragonmounts.DragonMountsLegacy.*;

@Mod(DragonMountsLegacy.MOD_ID)
public class ForgeModImpl
{
    public static final SimpleChannel NETWORK;

    public ForgeModImpl()
    {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        DMLRegistry.init(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DMLConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DMLConfig.CLIENT_SPEC);

        setupEvents();
    }

    static
    {
        var PROTOCOL_VERSION = 1;
        NETWORK = ChannelBuilder.named(DragonMountsLegacy.id("network"))
                .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
                .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .simpleChannel();
    }

    private static void setupEvents()
    {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        var bus = MinecraftForge.EVENT_BUS;

        bus.addListener((PlayerInteractEvent.RightClickBlock e) -> e.setCanceled(overrideVanillaDragonEgg(e.getLevel(), e.getPos(), e.getEntity())));
        bus.addListener((AddReloadListenerEvent e) -> registerReloadListeners(e::addListener));

        modBus.addListener((EntityAttributeCreationEvent e) -> registerEntityAttributes(e::put));
        modBus.addListener((DataPackRegistryEvent.NewRegistry e) -> registerDatapacks(e::dataPackRegistry));
        modBus.addListener((NewRegistryEvent e) -> e.create(RegistryBuilder.of(Ability.REGISTRY_KEY.location())));
        modBus.addListener((NewRegistryEvent e) -> e.create(RegistryBuilder.of(Habitat.REGISTRY_KEY.location())));

        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
        {
            bus.addListener((TickEvent.ClientTickEvent e) -> clientTick(e.phase == TickEvent.Phase.START));
            bus.addListener((ViewportEvent.ComputeCameraAngles e) -> MountCameraManager.setMountCameraAngles(e.getCamera()));
            bus.addListener((InputEvent.Key e) -> onKeyPress(e.getKey(), e.getAction(), e.getModifiers()));

            modBus.addListener((ModelEvent.RegisterGeometryLoaders e) -> registerEggModelLoader(e::register));
            modBus.addListener((BuildCreativeModeTabContentsEvent e) -> registerCreativeTabItems(e.getTabKey(), e::accept));
            modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> registerRenderers());
            modBus.addListener((RegisterColorHandlersEvent.Item e) -> registerItemColors(e.getItemColors()));
            modBus.addListener((FMLConstructModEvent e) -> e.enqueueWork(DragonMountsLegacy::registerReloadListenersEarly));
            modBus.addListener((RegisterKeyMappingsEvent e) -> registerKeyBindings(e::register));
        }
    }
}
