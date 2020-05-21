package wolfshotz.dml.client;

import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.EggRenderer;
import wolfshotz.dml.dragons.TameableDragonEntity;
import wolfshotz.dml.egg.DragonSpawnEggItem;

@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    public static void clientSetup(FMLClientSetupEvent evt) { ClientEvents.registerRenders(); }

    public static void registerRenders()
    {
        registerRenderer(DMLRegistry.AETHER_DAGON.get());
        registerRenderer(DMLRegistry.ENDER_DRAGON.get());
        registerRenderer(DMLRegistry.FIRE_DRAGON.get());
        registerRenderer(DMLRegistry.GHOST_DRAGON.get());
        registerRenderer(DMLRegistry.FOREST_DRAGON.get());
        registerRenderer(DMLRegistry.ICE_DRAGON.get());
        registerRenderer(DMLRegistry.NETHER_DRAGON.get());
        registerRenderer(DMLRegistry.WATER_DRAGON.get());

        RenderingRegistry.registerEntityRenderingHandler(DMLRegistry.EGG.get(), EggRenderer::new);
    }

    public static void registerRenderer(EntityType<? extends TameableDragonEntity> type)
    {
        RenderingRegistry.registerEntityRenderingHandler(type, rm -> new DragonRenderer(rm, type));
    }

//    TODO: Fix when forge provides a matrix stack
//    @SubscribeEvent
//    public static void cameraPerspective(EntityViewRenderEvent.CameraSetup evt)
//    {
//        PlayerEntity player = Minecraft.getInstance().player;
//        int perspective = Minecraft.getInstance().gameSettings.thirdPersonView;
//        if (player.getRidingEntity() instanceof TameableDragonEntity)
//        {
//            if (perspective == 1) RenderSystem.translated(0, -1, -3);
//            if (perspective == 2) RenderSystem.translated(0, 1, -5);
//        }
//    }

    public static void itemColors(ColorHandlerEvent.Item evt)
    {
        DMLRegistry.ITEMS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(DragonSpawnEggItem.class::isInstance)
                .map(DragonSpawnEggItem.class::cast)
                .forEach(i -> evt.getItemColors().register((s, c) -> i.getColor(c), i));
    }
}
