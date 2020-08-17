package wolfshotz.dml.client;

import net.minecraft.entity.EntityType;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.EggRenderer;
import wolfshotz.dml.entities.TameableDragonEntity;
import wolfshotz.dml.misc.LazySpawnEggItem;

public class ClientEvents
{
    public static void init()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ClientEvents::setup);
        bus.addListener(ClientEvents::itemColors);
    }

    public static void setup(FMLClientSetupEvent evt)
    {
        ClientEvents.registerRenders();
//        ClientRegistry.registerKeyBinding(new BreathKeybind());
    }

    public static void registerRenders()
    {
        renderer(DMLRegistry.AETHER_DRAGON_ENTITY.get());
        renderer(DMLRegistry.ENDER_DRAGON_ENTITY.get());
        renderer(DMLRegistry.FIRE_DRAGON_ENTITY.get());
        renderer(DMLRegistry.GHOST_DRAGON_ENTITY.get());
        renderer(DMLRegistry.FOREST_DRAGON_ENTITY.get());
        renderer(DMLRegistry.ICE_DRAGON_ENTITY.get());
        renderer(DMLRegistry.NETHER_DRAGON_ENTITY.get());
        renderer(DMLRegistry.WATER_DRAGON_ENTITY.get());

        RenderingRegistry.registerEntityRenderingHandler(DMLRegistry.EGG_ENTITY.get(), EggRenderer::new);
    }

    private static void renderer(EntityType<? extends TameableDragonEntity> type) { RenderingRegistry.registerEntityRenderingHandler(type, rm -> new DragonRenderer(rm, type)); }

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
                .filter(LazySpawnEggItem.class::isInstance)
                .map(LazySpawnEggItem.class::cast)
                .forEach(item -> evt.getItemColors().register((s, index) -> item.getColor(index), item));
    }
}
