package wolfshotz.dml.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.EggRenderer;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.DragonEntityType;

public class ClientEvents
{
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

    public static void registerRenderer(DragonEntityType type)
    {
        RenderingRegistry.registerEntityRenderingHandler(type, rm -> new DragonRenderer(rm, type));
    }
}
