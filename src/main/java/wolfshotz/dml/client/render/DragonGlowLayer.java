package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

public class DragonGlowLayer extends LayerRenderer<TameableDragonEntity, DragonModel>
{
    private final DragonRenderer renderer;

    public DragonGlowLayer(DragonRenderer entityRendererIn)
    {
        super(entityRendererIn);
        this.renderer = entityRendererIn;
    }

    @Override
    public void render(MatrixStack ms, IRenderTypeBuffer bufferIn, int packedLightIn, TameableDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        IVertexBuilder buffer = bufferIn.getBuffer(RenderType.func_230168_b_(renderer.getGlowTexture(dragon), false));
        getEntityModel().render(ms, buffer, 15728640, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
