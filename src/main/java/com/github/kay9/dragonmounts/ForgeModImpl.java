package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.MountCameraManager;
import com.github.kay9.dragonmounts.data.CrossBreedingManager;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.github.kay9.dragonmounts.DragonMountsLegacy.*;

@Mod(DragonMountsLegacy.MOD_ID)
public class ForgeModImpl
{
    public static final SimpleChannel NETWORK;

    public ForgeModImpl()
    {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        DMLRegistry.init(bus);
        BreedRegistry.DEFERRED_REGISTRY.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DMLConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DMLConfig.CLIENT_SPEC);

        setupEvents();
    }

    static
    {
        var PROTOCOL_VERSION = "1.O";
        NETWORK = NetworkRegistry.ChannelBuilder.named(DragonMountsLegacy.id("network"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
    }

    private static void setupEvents()
    {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        var bus = MinecraftForge.EVENT_BUS;

        bus.addListener((PlayerInteractEvent.RightClickBlock e) -> e.setCanceled(overrideVanillaDragonEgg(e.getWorld(), e.getPos(), e.getPlayer())));
        bus.addListener((AddReloadListenerEvent e) -> e.addListener(CrossBreedingManager.INSTANCE));

        modBus.addListener((EntityAttributeCreationEvent e) -> registerEntityAttributes(e::put));
        modBus.addGenericListener(GlobalLootModifierSerializer.class, (RegistryEvent.Register<GlobalLootModifierSerializer<?>> e) -> DMLRegistry.registerLootConditions());

        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
        {
            bus.addListener((TickEvent.ClientTickEvent e) -> clientTick(e.phase == TickEvent.Phase.START));
            bus.addListener((EntityViewRenderEvent.CameraSetup e) -> MountCameraManager.setMountCameraAngles(e.getCamera()));
            bus.addListener((InputEvent.KeyInputEvent e) -> onKeyPress(e.getKey(), e.getAction(), e.getModifiers()));
            bus.addListener((TagsUpdatedEvent e) -> populateSearchTrees());

            modBus.addListener((EntityRenderersEvent.RegisterRenderers e) -> registerRenderers());
            modBus.addListener((ModelRegistryEvent e) -> defineBlockModels(ForgeModelBakery::addSpecialModel));
            modBus.addListener((ColorHandlerEvent.Item e) -> registerItemColors(e.getItemColors()));
            modBus.addListener((FMLConstructModEvent e) -> e.enqueueWork(DragonMountsLegacy::registerReloadListenersEarly));
        }
    }
}
