package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DragonRenderer extends MobRenderer<TameableDragon, DragonModel>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(DragonMountsLegacy.id("dragon"), "main");
    private static final ResourceLocation DISSOLVE_TEXTURE = DragonMountsLegacy.id("textures/entity/dragon/dissolve.png");
    private static final Map<ResourceLocation, ResourceLocation[]> TEXTURE_CACHE = new HashMap<>(8);

    public DragonRenderer(EntityRendererProvider.Context modelBakery)
    {
        super(modelBakery, new DragonModel(modelBakery.bakeLayer(LAYER_LOCATION)), 2);
        addLayer(GLOW_LAYER);
        addLayer(SADDLE_LAYER);
        addLayer(DEATH_LAYER);
    }

    // During death, do not use the standard rendering and let the death layer handle it. Hacky, but better than mixins.
    @Nullable
    @Override
    protected RenderType getRenderType(TameableDragon entity, boolean visible, boolean invisToClient, boolean glowing)
    {
        return entity.deathTime > 0? null : super.getRenderType(entity, visible, invisToClient, glowing);
    }

    @Override
    protected void setupRotations(TameableDragon pEntityLiving, PoseStack ps, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
        super.setupRotations(pEntityLiving, ps, pAgeInTicks, pRotationYaw, pPartialTicks);
        var scale = pEntityLiving.getScale();
        ps.scale(scale, scale, scale);
    }

    // dragons dissolve during death, not flip.
    @Override
    protected float getFlipDegrees(TameableDragon pLivingEntity)
    {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation(TameableDragon dragon)
    {
        return getTextureForLayer(dragon.getBreed(), 0);
    }

    public static ResourceLocation getTextureForLayer(DragonBreed breed, int layer)
    {
        return TEXTURE_CACHE.computeIfAbsent(breed.id(), DragonRenderer::cacheTextures)[layer];
    }

    private static ResourceLocation[] cacheTextures(ResourceLocation id)
    {
        final String[] TEXTURES = {"body", "glow", "saddle"}; // 0, 1, 2

        ResourceLocation[] cache = new ResourceLocation[3];
        for (int i = 0; i < TEXTURES.length; i++)
            cache[i] = new ResourceLocation(id.getNamespace(), "textures/entity/dragon/" + id.getPath() + "/" + TEXTURES[i] + ".png");
        return cache;
    }

    private void renderDeathOverlay(PoseStack ps, MultiBufferSource buffer, int light, TameableDragon dragon, RenderType underDecal)
    {
        var delta = dragon.deathTime / (float) dragon.getMaxDeathTime();
        model.renderToBuffer(ps, buffer.getBuffer(CustomRenderTypes.DISSOLVE), light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, delta);
        model.renderToBuffer(ps, buffer.getBuffer(underDecal), light, OverlayTexture.pack(0, true), 1f, 1f, 1f, 1f);
    }

    public final RenderLayer<TameableDragon, DragonModel> GLOW_LAYER = new RenderLayer<>(this)
    {
        @Override
        public void render(PoseStack pMatrixStack, MultiBufferSource buffer, int pPackedLight, TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
        {
            if (dragon.deathTime == 0)
            {
                var type = CustomRenderTypes.glow(getTextureForLayer(dragon.getBreed(), 1));
                model.renderToBuffer(pMatrixStack, buffer.getBuffer(type), pPackedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            }
       }
    };
    public final RenderLayer<TameableDragon, DragonModel> SADDLE_LAYER = new RenderLayer<>(this)
    {
        @Override
        public void render(PoseStack ps, MultiBufferSource buffer, int light, TameableDragon dragon, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
        {
            if (dragon.isSaddled())
                renderColoredCutoutModel(model, getTextureForLayer(dragon.getBreed(), 2), ps, buffer, light, dragon, 1f, 1f, 1f);
        }
    };
    public final RenderLayer<TameableDragon, DragonModel> DEATH_LAYER = new RenderLayer<>(this)
    {
        @Override
        public void render(PoseStack ps, MultiBufferSource buffer, int light, TameableDragon dragon, float limbSwing, float limbSwingAmount, float partials, float age, float yaw, float pitch)
        {
            if (dragon.deathTime > 0)
                renderDeathOverlay(ps, buffer, light, dragon, RenderType.entityDecal(getTextureLocation(dragon)));
        }
    };

    private static class CustomRenderTypes extends RenderType
    {
        private static final RenderType DISSOLVE = RenderType.dragonExplosionAlpha(DISSOLVE_TEXTURE);
        private static final Function<ResourceLocation, RenderType> GLOW_FUNC = Util.memoize(texture -> create("eyes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)));

        private static RenderType glow(ResourceLocation texture)
        {
            return GLOW_FUNC.apply(texture);
        }

        @SuppressWarnings("ConstantConditions")
        private CustomRenderTypes()
        {
            // dummy
            super(null, null, null, 0, false, true, null, null);
        }
    }
}
