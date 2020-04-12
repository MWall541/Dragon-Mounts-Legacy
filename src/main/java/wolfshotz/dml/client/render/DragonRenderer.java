package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.client.render.layer.DragonDeathLayer;
import wolfshotz.dml.client.render.layer.DragonGlowLayer;
import wolfshotz.dml.client.render.layer.DragonSaddleLayer;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

import javax.annotation.Nullable;

public class DragonRenderer extends MobRenderer<TameableDragonEntity, DragonModel>
{
    public static final String TEX_PATH = "textures/entity/dragon/";
    public static final ResourceLocation DISSOLVE_TEXTURE = rl("dissolve.png");

    public ResourceLocation bodyTexture;
    public ResourceLocation saddleTexture;
    public ResourceLocation glowTexture;

    public DragonRenderer(EntityRendererManager renderManagerIn, EntityType<TameableDragonEntity> type)
    {
        super(renderManagerIn, new DragonModel(type), 2);
        addLayer(new DragonSaddleLayer(this));
        addLayer(new DragonGlowLayer(this));
        addLayer(new DragonDeathLayer(this));
    }

    @Nullable
    @Override
    protected RenderType func_230042_a_(TameableDragonEntity entity, boolean p_230042_2_, boolean p_230042_3_)
    {
        return super.func_230042_a_(entity, p_230042_2_, p_230042_3_);
    }

    @Override
    protected void preRenderCallback(TameableDragonEntity dragon, MatrixStack ms, float partialTickTime)
    {
        float scale = dragon.getScale() * 0.8f;
        ms.scale(scale, scale, scale);
    }

    @Override
    public void render(TameableDragonEntity dragon, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        super.render(dragon, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected void applyRotations(TameableDragonEntity dragon, MatrixStack ms, float ageInTicks, float rotationYaw, float partialTicks)
    {
        ms.rotate(Vector3f.YP.rotationDegrees(180 - rotationYaw));
    }

    @Override
    public ResourceLocation getEntityTexture(TameableDragonEntity entity)
    {
        if (bodyTexture == null)
            bodyTexture = rl(entity.getType().getRegistryName().getPath() + "/body.png");
        return bodyTexture;
    }

    public ResourceLocation getSaddleTexture(TameableDragonEntity entity)
    {
        if (saddleTexture == null)
            saddleTexture = rl(entity.getType().getRegistryName().getPath() + "/saddle.png");
        return saddleTexture;
    }

    public ResourceLocation getGlowTexture(TameableDragonEntity entity)
    {
        if (glowTexture == null)
            glowTexture = rl(entity.getType().getRegistryName().getPath() + "/glow.png");
        return glowTexture;
    }

    public static ResourceLocation rl(String path)
    {
        return DragonMountsLegacy.rl(TEX_PATH + path);
    }
}
