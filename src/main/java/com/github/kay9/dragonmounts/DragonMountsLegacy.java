package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.DragonEggRenderer;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.client.DragonRenderer;
import com.github.kay9.dragonmounts.client.EggEntityRenderer;
import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.UpdateBreedsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
    public static final SimpleChannel NETWORK = buildNetwork();

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        DMLRegistry.init(bus);

        bus.addListener((EntityAttributeCreationEvent e) -> e.put(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes().build()));

        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> e.addListener(BreedManager.INSTANCE));
        MinecraftForge.EVENT_BUS.addListener(DMLEggBlock::overrideVanillaDragonEgg);

        if (FMLLoader.getDist() == Dist.CLIENT)
        {
            MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::cameraAngles);

            bus.addListener(DragonMountsLegacy::modelRegistry);
            bus.addListener((EntityRenderersEvent.RegisterLayerDefinitions e) -> e.registerLayerDefinition(DragonRenderer.LAYER_LOCATION, DragonModel::createBodyLayer));
            bus.addListener((ColorHandlerEvent.Item e) -> e.getItemColors().register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get()));
            bus.addListener(DragonMountsLegacy::rendererRegistry);
        }
        else
        {
            MinecraftForge.EVENT_BUS.addListener(BreedManager::syncClientBreeds);
        }
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    private static SimpleChannel buildNetwork()
    {
        var PROTOCOL_VERSION = "1.O";
        var net = NetworkRegistry.ChannelBuilder.named(id("network"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        net.registerMessage(1, UpdateBreedsPacket.class, UpdateBreedsPacket::encode, UpdateBreedsPacket::new, UpdateBreedsPacket::handle);

        return net;
    }

    public static void modelRegistry(ModelRegistryEvent e)
    {
        var dir = "models/block/dragon_eggs";
        var length = "models/".length();
        var suffixLength = ".json".length();
        for (var rl : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.endsWith(".json")))
        {
            String path = rl.getPath();
            ForgeModelBakery.addSpecialModel(new ResourceLocation(rl.getNamespace(), path.substring(length, path.length() - suffixLength)));
        }
    }

    public static void rendererRegistry(EntityRenderersEvent.RegisterRenderers e)
    {
        e.registerEntityRenderer(DMLRegistry.DRAGON.get(), DragonRenderer::new);
        e.registerEntityRenderer(DMLRegistry.DRAGON_EGG.get(), EggEntityRenderer::new);
        e.registerBlockEntityRenderer(DMLRegistry.EGG_BLOCK_ENTITY.get(), DragonEggRenderer::instance);
    }

    public static void cameraAngles(EntityViewRenderEvent.CameraSetup evt)
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
                    vertical = 3;
                }
            }
            evt.getCamera().move(-distance, vertical, 0);
        }
    }
}