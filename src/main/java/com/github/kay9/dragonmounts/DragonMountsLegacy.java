package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.DragonEggRenderer;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.client.DragonRenderer;
import com.github.kay9.dragonmounts.client.EggEntityRenderer;
import com.github.kay9.dragonmounts.entity.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.entity.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.github.kay9.dragonmounts.entity.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.network.WeaponAbilityPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DMLRegistry.init(bus);
        BreedRegistry.DEFERRED_REGISTRY.register(bus);

        MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::attemptVanillaEggReplacement);

        bus.addListener((EntityAttributeCreationEvent e) -> e.put(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes().build()));

        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
        {
            MinecraftForge.EVENT_BUS.addListener((EntityViewRenderEvent.CameraSetup e) -> cameraAngles(e.getCamera()));

            bus.addListener((ModelRegistryEvent e) -> defineBlockModels());
            bus.addListener((ColorHandlerEvent.Item e) -> e.getItemColors().register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get()));
            bus.addListener(DragonMountsLegacy::rendererRegistry);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DMLConfig.COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DMLConfig.SERVER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DMLConfig.CLIENT);
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    private static void attemptVanillaEggReplacement(PlayerInteractEvent.RightClickBlock evt)
    {
        if (DMLEggBlock.overrideVanillaDragonEgg(evt.getWorld(), evt.getPos(), evt.getPlayer())) evt.setCanceled(true);
    }

    private static void defineBlockModels()
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

    private static void cameraAngles(Camera camera)
    {
        if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon)
        {
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

    static
    {
        var PROTOCOL_VERSION = "1.O";
        NETWORK = NetworkRegistry.ChannelBuilder.named(id("network"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        var index = -1;
        NETWORK.registerMessage(++index, WeaponAbilityPacket.class, WeaponAbilityPacket::encode, WeaponAbilityPacket::new, WeaponAbilityPacket::prepare);
    }
}