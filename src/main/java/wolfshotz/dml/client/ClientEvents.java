package wolfshotz.dml.client;

import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.DragonSpawnEggItem;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.EggRenderer;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    public static void clientSetup(FMLClientSetupEvent evt)
    {
        ClientEvents.registerRenders();
    }

    public static void registerRenders()
    {
        registerRenderer(DMLEntities.AETHER_DAGON.get());
        registerRenderer(DMLEntities.ENDER_DRAGON.get());
        registerRenderer(DMLEntities.FIRE_DRAGON.get());
        registerRenderer(DMLEntities.GHOST_DRAGON.get());
        registerRenderer(DMLEntities.FOREST_DRAGON.get());
        registerRenderer(DMLEntities.ICE_DRAGON.get());
        registerRenderer(DMLEntities.NETHER_DRAGON.get());
        registerRenderer(DMLEntities.WATER_DRAGON.get());

        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.EGG.get(), EggRenderer::new);
    }

    public static void registerRenderer(EntityType<TameableDragonEntity> type)
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
        DragonSpawnEggItem.getEggs().forEach(i -> evt.getItemColors().register((s, c) -> i.getColor(c), i));
    }
}
