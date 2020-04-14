package wolfshotz.dml.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.client.render.DragonRenderer;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

public class DragonDeathLayer extends LayerRenderer<TameableDragonEntity, DragonModel>
{
    public DragonDeathLayer(DragonRenderer renderer)
    {
        super(renderer);
    }

    @Override
    public void render(MatrixStack ms, IRenderTypeBuffer bufferIn, int packedLightIn, TameableDragonEntity dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
    }
}
