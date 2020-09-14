package wolfshotz.dml.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderType
{
    // dummy
    private RenderTypes() { super(null, null, 0, 0, false, false, null, null); }

    public static RenderType getGlow(ResourceLocation locationIn)
    {
        RenderState.TextureState textureState = new RenderState.TextureState(locationIn, false, false);
        return makeType("glow", DefaultVertexFormats.ENTITY, 7, 256, false, true, State.getBuilder()
                .texture(textureState)
                .transparency(RenderState.ADDITIVE_TRANSPARENCY)
                .alpha(DEFAULT_ALPHA)
                .build(false));
    }

    public static RenderType getDecal(ResourceLocation loc)
    {
        RenderType.State rendertype$state = RenderType.State.getBuilder().texture(new RenderState.TextureState(loc, false, false))
                .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                .alpha(DEFAULT_ALPHA)
                .depthTest(DEPTH_EQUAL)
                .lightmap(LIGHTMAP_ENABLED)
                .overlay(OVERLAY_ENABLED)
                .build(false);
        return makeType("entity_decal", DefaultVertexFormats.ENTITY, 7, 256, rendertype$state);
    }
}
