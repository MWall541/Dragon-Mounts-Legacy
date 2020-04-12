package wolfshotz.dml.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.client.render.RenderStates;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

public class DragonDeathLayer extends LayerRenderer<TameableDragonEntity, DragonModel>
{
    public DragonDeathLayer(DragonRenderer renderer)
    {
        super(renderer);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, TameableDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        float death = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();
        IVertexBuilder buffer = bufferIn.getBuffer(RenderStates.getDissolve(death));

        if (death > 0)
            getEntityModel().render(matrixStackIn, buffer, packedLightIn, LivingRenderer.getPackedOverlay(dragon, 0), 1, 1, 1, 1);
    }
}
