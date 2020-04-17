package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.client.render.layer.DragonGlowLayer;
import wolfshotz.dml.client.render.layer.DragonSaddleLayer;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

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
        addLayer(new DragonGlowLayer(this));
        addLayer(new DragonSaddleLayer(this));
    }

    @Override
    public void render(TameableDragonEntity dragon, float entityYaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(dragon, this, partialTicks, ms, bufferIn, packedLightIn)))
            return;

        ms.push();

        entityModel.swingProgress = getSwingProgress(dragon, partialTicks);
        entityModel.isChild = dragon.isChild();

        // calculate rotations
        float terpYawOff = MathHelper.interpolateAngle(partialTicks, dragon.prevRenderYawOffset, dragon.renderYawOffset);
        float terpYaw = MathHelper.interpolateAngle(partialTicks, dragon.prevRotationYawHead, dragon.rotationYawHead);
        float yawDiff = terpYaw - terpYawOff;
        float lerpPitch = MathHelper.lerp(partialTicks, dragon.prevRotationPitch, dragon.rotationPitch);
        float rotation = handleRotationFloat(dragon, partialTicks);
        // calculate limb movement angles
        float limbSwingAmount = 0f;
        float limbSwing = 0f;
        if (dragon.isAlive())
        {
            limbSwingAmount = Math.min(1f, MathHelper.lerp(partialTicks, dragon.prevLimbSwingAmount, dragon.limbSwingAmount));
            limbSwing = dragon.limbSwing - dragon.limbSwingAmount * (1f - partialTicks);
            if (dragon.isChild()) limbSwing *= 3f;
        }

        // correct rotations and scaling
        applyRotations(dragon, ms, rotation, terpYawOff, partialTicks);
        ms.scale(-1f, -1f, 1f);
        preRenderCallback(dragon, ms, partialTicks);
        ms.translate(0, -1.501f, 0);

        // finish up; render model and layers
        entityModel.setLivingAnimations(dragon, limbSwing, limbSwingAmount, partialTicks);
        entityModel.setRotationAngles(dragon, limbSwing, limbSwingAmount, rotation, yawDiff, lerpPitch);
        renderModel(dragon, ms, bufferIn, packedLightIn, partialTicks);
        if (!dragon.isSpectator())
            for (LayerRenderer<TameableDragonEntity, DragonModel> layer : layerRenderers)
                layer.render(ms, bufferIn, packedLightIn, dragon, limbSwing, limbSwingAmount, partialTicks, rotation, yawDiff, lerpPitch);

        ms.pop();

        // render name
        if (canRenderName(dragon))
            renderName(dragon, dragon.getDisplayName().getFormattedText(), ms, bufferIn, packedLightIn);

        // render leash
//        Entity entity = dragon.getLeashHolder();
//        if (entity != null) renderLeash(entityIn, partialTicks, matrixStackIn, bufferIn, entity);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(dragon, this, partialTicks, ms, bufferIn, packedLightIn));
    }

    public void renderModel(TameableDragonEntity dragon, MatrixStack ms, IRenderTypeBuffer buffer, int packedLight, float partialTicks)
    {
        ResourceLocation texture = getEntityTexture(dragon);
        int packedOverlay = getPackedOverlay(dragon, getOverlayProgress(dragon, partialTicks));
        float deathTime = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();
        if (deathTime > 0)
        {
            IVertexBuilder dissolve = buffer.getBuffer(RenderStates.getEntityAlpha(DISSOLVE_TEXTURE, deathTime));
            entityModel.render(ms, dissolve, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            IVertexBuilder decal = buffer.getBuffer(RenderStates.getEntityDecal(texture));
            entityModel.render(ms, decal, packedLight, OverlayTexture.getPackedUV(0, true), 1f, 1f, 1f, 1f);

            return;
        }

        boolean visibleToPlayer = !isVisible(dragon) && !dragon.isInvisibleToPlayer(Minecraft.getInstance().player);
        IVertexBuilder ivertexbuilder = buffer.getBuffer(func_230042_a_(dragon, isVisible(dragon), visibleToPlayer));
        entityModel.render(ms, ivertexbuilder, packedLight, packedOverlay, 1f, 1f, 1f, visibleToPlayer? 0.15f : 1f);
    }

    @Override
    protected void preRenderCallback(TameableDragonEntity dragon, MatrixStack ms, float partialTickTime)
    {
        float scale = dragon.getScale() * 0.8f;
        ms.scale(scale, scale, scale);
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
