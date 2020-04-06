package wolfshotz.dml.client;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.render.DragonRenderer;

public class ClientEvents
{
    public static void registerRenders()
    {
        RenderingRegistry.registerEntityRenderingHandler(DragonMountsLegacy.END_DRAGON.get(), DragonRenderer::new);
    }
}
