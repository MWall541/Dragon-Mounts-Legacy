package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.*;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragonMountsLegacy.MOD_ID)
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger LOG = LogManager.getLogger();
    public static final SimpleChannel NETWORK;

    public DragonMountsLegacy()
    {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        DMLRegistry.init(bus);
        BreedRegistry.DEFERRED_REGISTRY.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DMLConfig.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DMLConfig.SERVER);
//        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DMLConfig.CLIENT);

        setupEvents();
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    static
    {
        var PROTOCOL_VERSION = "1.O";
        NETWORK = NetworkRegistry.ChannelBuilder.named(id("network"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
    }

    private static void setupEvents()
    {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::attemptVanillaEggReplacement);

        bus.addListener((EntityAttributeCreationEvent e) -> e.put(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes().build()));

        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
        {
            MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::cameraAngles);
            MinecraftForge.EVENT_BUS.addListener(Keybinds::handleKeyPress);
            MinecraftForge.EVENT_BUS.addListener(MountControlsMessenger::tick);
            MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::populateSearchTrees);

            bus.addListener(DragonMountsLegacy::defineBlockModels);
            bus.addListener((ColorHandlerEvent.Item e) -> e.getItemColors().register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get()));
            bus.addListener(DragonMountsLegacy::rendererRegistry);
        }
    }

    private static void attemptVanillaEggReplacement(PlayerInteractEvent.RightClickBlock evt)
    {
        if (DMLEggBlock.overrideVanillaDragonEgg(evt.getWorld(), evt.getPos(), evt.getPlayer())) evt.setCanceled(true);
    }

    private static void defineBlockModels(ModelRegistryEvent evt)
    {
        var dir = "models/block/dragon_eggs";
        var length = "models/".length();
        var suffixLength = ".json".length();
        for (var rl : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.endsWith(".json")))
        {
            var path = rl.getPath();
            path = path.substring(length, path.length() - suffixLength);
            var model = new ResourceLocation(rl.getNamespace(), path);
            var id = path.substring("block/dragon_eggs/".length(), path.length() - "_dragon_egg".length());

            ForgeModelBakery.addSpecialModel(model);
            DragonEggRenderer.MODEL_CACHE.put(new ResourceLocation(rl.getNamespace(), id), model);
        }
    }

    private static void rendererRegistry(EntityRenderersEvent.RegisterRenderers e)
    {
        e.registerEntityRenderer(DMLRegistry.DRAGON.get(), DragonRenderer::new);
        ForgeHooksClient.registerLayerDefinition(DragonRenderer.LAYER_LOCATION, DragonModel::createBodyLayer);

        e.registerEntityRenderer(DMLRegistry.DRAGON_EGG.get(), EggEntityRenderer::new);

        e.registerBlockEntityRenderer(DMLRegistry.EGG_BLOCK_ENTITY.get(), DragonEggRenderer::instance);
    }

    private static void cameraAngles(EntityViewRenderEvent.CameraSetup evt)
    {
        if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon)
        {
            var camera = evt.getCamera();
            var distance = 0;
            var vertical = 0;
            switch (Minecraft.getInstance().options.getCameraType())
            {
                case THIRD_PERSON_FRONT -> distance = 6;
                case THIRD_PERSON_BACK -> {
                    distance = 6;
                    vertical = 4;
                }
            }
            camera.move(-camera.getMaxZoom(distance), vertical, 0);
        }
    }

    private static void populateSearchTrees(TagsUpdatedEvent evt)
    {
        var tree = Minecraft.getInstance().getSearchTree(SearchRegistry.CREATIVE_NAMES);
        var oldContents = tree.search(DragonMountsLegacy.MOD_ID + ":");
        NonNullList<ItemStack> newContents = NonNullList.create();
        DMLRegistry.EGG_BLOCK_ITEM.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, newContents);
        DMLRegistry.SPAWN_EGG.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, newContents);
        newItems: // this is a sanity check if resources are reloaded
        for (var newItem : newContents)
        {
            for (var oldItem : oldContents)
                if (ItemStack.matches(newItem, oldItem)) continue newItems;
            tree.add(newItem);
        }
        tree.refresh();
    }
}