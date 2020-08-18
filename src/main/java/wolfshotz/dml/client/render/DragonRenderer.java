package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.model.DragonModel;
import wolfshotz.dml.client.render.layer.DragonGlowLayer;
import wolfshotz.dml.client.render.layer.DragonSaddleLayer;
import wolfshotz.dml.entities.TameableDragonEntity;

public class DragonRenderer extends MobRenderer<TameableDragonEntity, DragonModel>
{
    public static final String TEX_PATH = "textures/entity/dragon/";
    public static final ResourceLocation DISSOLVE_TEXTURE = rl("dissolve.png");

    public ResourceLocation bodyTexture;
    public ResourceLocation saddleTexture;
    public ResourceLocation glowTexture;

    public DragonRenderer(EntityRendererManager renderManagerIn, EntityType<? extends TameableDragonEntity> type)
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
            renderName(dragon, dragon.getDisplayName(), ms, bufferIn, packedLightIn);

        // render leash
        Entity entity = dragon.getLeashHolder();
        if (entity != null) renderLeash(dragon, partialTicks, ms, bufferIn, entity);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(dragon, this, partialTicks, ms, bufferIn, packedLightIn));
    }

    public void renderModel(TameableDragonEntity dragon, MatrixStack ms, IRenderTypeBuffer buffer, int packedLight, float partialTicks)
    {
        ResourceLocation texture = getEntityTexture(dragon);
        int packedOverlay = getPackedOverlay(dragon, getOverlayProgress(dragon, partialTicks));
        float deathTime = dragon.getDeathTime() / (float) dragon.getMaxDeathTime();
        if (deathTime > 0)
        {
            IVertexBuilder dissolve = buffer.getBuffer(RenderStates.func_239264_a_(DISSOLVE_TEXTURE, deathTime));
            entityModel.render(ms, dissolve, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            IVertexBuilder decal = buffer.getBuffer(RenderStates.getEntityDecal(texture));
            entityModel.render(ms, decal, packedLight, OverlayTexture.getPackedUV(0, true), 1f, 1f, 1f, 1f);

            return;
        }

        boolean visibleToPlayer = !isVisible(dragon) && !dragon.isInvisibleToPlayer(Minecraft.getInstance().player);
        IVertexBuilder ivertexbuilder = buffer.getBuffer(func_230496_a_(dragon, isVisible(dragon), visibleToPlayer, Minecraft.getInstance().func_238206_b_(dragon)));
        entityModel.render(ms, ivertexbuilder, packedLight, packedOverlay, 1f, 1f, 1f, visibleToPlayer? 0.15f : 1f);
    }

    @Override
    protected void preRenderCallback(TameableDragonEntity dragon, MatrixStack ms, float partialTickTime)
    {
        float scale = dragon.getScale();
        float matrixScale = scale * 0.8f;
        ms.scale(matrixScale, matrixScale, matrixScale);
        shadowSize = scale * 2f;
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

    private <E extends Entity> void renderLeash(TameableDragonEntity entity, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, E leashHolder)
    {
        matrixStackIn.push();
        Vector3d vector3d = leashHolder.func_241843_o(partialTicks);
        double d0 = (MathHelper.lerp(partialTicks, entity.renderYawOffset, entity.prevRenderYawOffset) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vector3d vector3d1 = entity.func_241205_ce_();
        double d1 = Math.cos(d0) * vector3d1.z + Math.sin(d0) * vector3d1.x;
        double d2 = Math.sin(d0) * vector3d1.z - Math.cos(d0) * vector3d1.x;
        double d3 = MathHelper.lerp(partialTicks, entity.prevPosX, entity.getPosX()) + d1;
        double d4 = MathHelper.lerp(partialTicks, entity.prevPosY, entity.getPosY()) + vector3d1.y;
        double d5 = MathHelper.lerp(partialTicks, entity.prevPosZ, entity.getPosZ()) + d2;
        matrixStackIn.translate(d1, vector3d1.y, d2);
        float f = (float)(vector3d.x - d3);
        float f1 = (float)(vector3d.y - d4);
        float f2 = (float)(vector3d.z - d5);
        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLeash());
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(entity.getEyePosition(partialTicks));
        BlockPos blockpos1 = new BlockPos(leashHolder.getEyePosition(partialTicks));
        int i = getBlockLight(entity, blockpos);
        int j = leashHolder.isBurning() ? 15 : leashHolder.world.getLightFor(LightType.BLOCK, blockpos1);
        int k = entity.world.getLightFor(LightType.SKY, blockpos);
        int l = entity.world.getLightFor(LightType.SKY, blockpos1);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6);
        matrixStackIn.pop();
    }

    public static ResourceLocation rl(String path)
    {
        return DragonMountsLegacy.rl(TEX_PATH + path);
    }
}
