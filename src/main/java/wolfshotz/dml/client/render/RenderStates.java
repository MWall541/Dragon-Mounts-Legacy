package wolfshotz.dml.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderStates extends RenderType
{
    // dummy
    private RenderStates() { super(null, null, 0, 0, false, false, null, null); }

    public static RenderType getGlow(ResourceLocation locationIn)
    {
        RenderState.TextureState textureState = new RenderState.TextureState(locationIn, false, false);
        return RenderType.makeType("glow", DefaultVertexFormats.ENTITY, 7, 256, false, true, RenderType.State.getBuilder().texture(textureState).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).writeMask(RenderState.COLOR_DEPTH_WRITE).alpha(AlphaState.DEFAULT_ALPHA).cull(CULL_DISABLED).build(false));
    }
}
