package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

public class DragonSaddleLayer extends LayerRenderer<TameableDragonEntity, DragonModel>
{
    public final DragonRenderer renderer;

    public DragonSaddleLayer(DragonRenderer entityRendererIn)
    {
        super(entityRendererIn);
        this.renderer = entityRendererIn;
    }

    @Override
    public void render(MatrixStack ms, IRenderTypeBuffer buffer, int packedLight, TameableDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (dragon.isSaddled())
            renderCutoutModel(getEntityModel(), renderer.getSaddleTexture(dragon), ms, buffer, packedLight, dragon, 1, 1, 1);
    }
}
