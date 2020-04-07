package wolfshotz.dml.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.EggRenderer;
import wolfshotz.dml.entity.DMLEntities;

public class ClientEvents
{
    public static void registerRenders()
    {
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.AETHER_DAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.END_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.FIRE_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.GHOST_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.FOREST_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.ICE_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.NETHER_DRAGON.get(), DragonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.WATER_DRAGON.get(), DragonRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(DMLEntities.EGG.get(), EggRenderer::new);
    }
}
