package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.MountCameraManager;
import com.github.kay9.dragonmounts.dragon.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.egg.habitats.Habitat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static com.github.kay9.dragonmounts.DragonMountsLegacy.*;

@Mod(DragonMountsLegacy.MOD_ID)
public class NeoModImpl
{
    public NeoModImpl(IEventBus bus, ModContainer container)
    {
        DMLRegistry.init(bus);

        container.registerConfig(ModConfig.Type.COMMON, DMLConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, DMLConfig.CLIENT_SPEC);

        setupEvents(bus);
    }

    private static void setupEvents(IEventBus modBus)
    {
        var bus = NeoForge.EVENT_BUS;

        bus.addListener((PlayerInteractEvent.RightClickBlock e) -> e.setCanceled(overrideVanillaDragonEgg(e.getLevel(), e.getPos(), e.getEntity())));
        bus.addListener((AddReloadListenerEvent e) -> registerReloadListeners(e::addListener));

        modBus.addListener((EntityAttributeCreationEvent e) -> registerEntityAttributes(e::put));
        modBus.addListener((DataPackRegistryEvent.NewRegistry e) -> registerDatapacks(e::dataPackRegistry));
        modBus.addListener((NewRegistryEvent e) ->
        {
            e.create(new RegistryBuilder<>(Ability.REGISTRY_KEY).sync(true));
            e.create(new RegistryBuilder<>(Habitat.REGISTRY_KEY));
        });
        modBus.addListener((RegisterEvent e) -> registerOtherObjects(e.getRegistry()));

        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
        {
            bus.addListener((ClientTickEvent.Post e) -> clientTick());
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
