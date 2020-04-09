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
        return makeType("glow", DefaultVertexFormats.ENTITY, 7, 256, false, true, State.getBuilder()
                .texture(textureState)
                .transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
                .writeMask(RenderState.COLOR_DEPTH_WRITE)
                .alpha(AlphaState.DEFAULT_ALPHA)
                .cull(CULL_DISABLED)
                .build(false));
    }

    public static RenderType getDissolve(float amount)
    {
        TextureState texture = new RenderState.TextureState(DragonRenderer.DISSOLVE_TEXTURE, true, false);
        return makeType("dissolve", DefaultVertexFormats.ENTITY, 7, 262144, State.getBuilder()
                .texture(texture)
                .depthTest(DEPTH_LEQUAL)
                .alpha(new AlphaState(amount))
                .build(false));
    }
}
