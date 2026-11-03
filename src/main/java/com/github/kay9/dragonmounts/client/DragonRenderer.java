package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.data.model.DragonModelPropertiesListener;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DragonRenderer extends MobRenderer<TameableDragon, DragonRenderState, DragonModel>
{
    public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(DragonMountsLegacy.id("dragon"), "main");
    private static final Identifier[] DEFAULT_TEXTURES = computeTextureCacheFor(DragonBreed.BuiltIn.END.identifier());
    private static final Identifier DISSOLVE_TEXTURE = DragonMountsLegacy.id("textures/entity/dragon/dissolve.png");
    private static final int LAYER_BODY = 0;
    private static final int LAYER_GLOW = 1;
    private static final int LAYER_SADDLE = 2;

    private final DragonModel defaultModel;
    private final Map<Identifier, DragonModel> modelCache;
    private final Map<Identifier, Identifier[]> textureCache = new HashMap<>(8);

    public DragonRenderer(EntityRendererProvider.Context modelBakery)
    {
        super(modelBakery, new DragonModel(modelBakery.bakeLayer(MODEL_LOCATION)), 2);

        this.defaultModel = model;
        this.modelCache = bakeModels(modelBakery);

        addLayer(new GlowLayer());
        addLayer(new SaddleLayer());
        addLayer(new DeathLayer());
    }

    @Override
    public DragonRenderState createRenderState()
    {
        return new DragonRenderState();
    }

    @Override
    public void extractRenderState(TameableDragon dragon, DragonRenderState state, float partialTicks)
    {
        super.extractRenderState(dragon, state, partialTicks);

        state.breed = dragon.getBreedHolder();
        state.isSaddled = dragon.isSaddled();
        state.maxDeathTime = dragon.getMaxDeathTime();
        state.ageScale = dragon.getAgeScale();

        var animator = dragon.getAnimator();
        state.animator = animator;
        if (animator != null)
        {
            animator.setPartialTicks(partialTicks);
            state.modelPitch = animator.getModelPitch(partialTicks);
            state.modelOffsetX = animator.getModelOffsetX();
            state.modelOffsetY = animator.getModelOffsetY();
            state.modelOffsetZ = animator.getModelOffsetZ();
        }
    }

    @Override
    public boolean shouldRender(TameableDragon dragon, Frustum pCamera, double pCamX, double pCamY, double pCamZ)
    {
        return dragon.getBreed() != null && super.shouldRender(dragon, pCamera, pCamX, pCamY, pCamZ);
    }

    // dragons are big and their necks/tails extend far beyond the bounding box; never cull.
    @Override
    protected boolean affectedByCulling(TameableDragon dragon)
    {
        return false;
    }

    @Override
    public void submit(DragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        this.model = getModel(state);
        super.submit(state, poseStack, collector, camera);
    }

    private DragonModel getModel(DragonRenderState state)
    {
        var breed = state.breed;
        if (breed == null) return defaultModel;

        var selected = modelCache.get(breed.unwrapKey().orElseThrow().identifier());
        if (selected == null) return defaultModel;

        return selected;
    }

    // During death, do not use the standard rendering and let the death layer handle it. Hacky, but better than mixins.
    @Nullable
    @Override
    protected RenderType getRenderType(DragonRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearsGlowing)
    {
        return state.deathTime > 0? null : super.getRenderType(state, isBodyVisible, forceTransparent, appearsGlowing);
    }

    @Override
    public Identifier getTextureLocation(DragonRenderState state)
    {
        return getTextureForLayer(state.breed, LAYER_BODY);
    }

    public Identifier getTextureForLayer(@Nullable Holder<DragonBreed> breed, int layer)
    {
        if (breed == null) return DEFAULT_TEXTURES[layer];

        // we need to compute texture locations now rather than earlier due to the fact that breeds don't exist then.
        return textureCache.computeIfAbsent(breed.unwrapKey().orElseThrow().identifier(), DragonRenderer::computeTextureCacheFor)[layer];
    }

    @Override
    protected void setupRotations(DragonRenderState state, PoseStack ps, float bodyRot, float entityScale)
    {
        super.setupRotations(state, ps, bodyRot, entityScale);
        float scale = state.ageScale;
        ps.scale(scale, scale, scale);
        ps.translate(state.modelOffsetX, state.modelOffsetY, state.modelOffsetZ);
        ps.translate(0, 1.5, 0.5); // change rotation point
        ps.mulPose(Axis.XP.rotationDegrees(state.modelPitch)); // rotate near the saddle so we can support the player
        ps.translate(0, -1.5, -0.5); // restore rotation point
    }

    // dragons dissolve during death, not flip.
    @Override
    protected float getFlipDegrees()
    {
        return 0;
    }

    private Map<Identifier, DragonModel> bakeModels(EntityRendererProvider.Context bakery)
    {
        var builder = ImmutableMap.<Identifier, DragonModel>builder();
        for (var entry : DragonModelPropertiesListener.INSTANCE.pollDefinitions().entrySet())
            builder.put(entry.getKey(), new DragonModel(bakery.bakeLayer(entry.getValue())));
        return builder.build();
    }

    private static Identifier[] computeTextureCacheFor(Identifier breedId)
    {
        final String[] TEXTURES = {"body", "glow", "saddle"}; // 0, 1, 2

        Identifier[] cache = new Identifier[TEXTURES.length];
        for (int i = 0; i < TEXTURES.length; i++)
            cache[i] = Identifier.tryBuild(breedId.getNamespace(), "textures/entity/dragon/" + breedId.getPath() + "/" + TEXTURES[i] + ".png");
        return cache;
    }

    private class GlowLayer extends RenderLayer<DragonRenderState, DragonModel>
    {
        GlowLayer()
        {
            super(DragonRenderer.this);
        }

        @Override
        public void submit(PoseStack ps, SubmitNodeCollector collector, int light, DragonRenderState state, float yRot, float xRot)
        {
            if (state.deathTime > 0) return;
            collector.submitModel(getParentModel(), state, ps,
                    RenderTypes.eyes(getTextureForLayer(state.breed, LAYER_GLOW)),
                    light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }

    private class SaddleLayer extends RenderLayer<DragonRenderState, DragonModel>
    {
        SaddleLayer()
        {
            super(DragonRenderer.this);
        }

        @Override
        public void submit(PoseStack ps, SubmitNodeCollector collector, int light, DragonRenderState state, float yRot, float xRot)
        {
            if (!state.isSaddled) return;

            // submit directly on the collector rather than via renderColoredCutoutModel's
            // order(0) wrapper: that indirection put this pass in a different submission bucket
            // than the opaque body pass beneath it (same coincident geometry, same depth), which
            // made the saddle randomly fail to render depending on GPU/driver depth-sort timing.
            // GlowLayer below already submits this way and has never shown the issue.
            //
            // Note: RenderTypes.createArmorDecalCutoutNoCull (vanilla's armor-trim decal type)
            // was tried here to fix a residual depth-fight on the Ice breed specifically, but it
            // made the saddle disappear entirely on every breed instead — that render type likely
            // assumes vertex data that EquipmentLayerRenderer sets up and our plain entity model
            // submission doesn't provide. Do not reuse it without also replicating whatever setup
            // EquipmentLayerRenderer does before calling it.
            //
            // Nudge the pass away from the body it's coincident with. A translate alone isn't
            // enough either: it separates depth well for faces viewed near-parallel to the
            // translation axis (e.g. the torso top, seen from above), but does almost nothing for
            // faces viewed near-perpendicular to it (e.g. the torso sides, seen from eye level,
            // where translating along Y barely changes their distance from the camera) — so a
            // Y-only translate fixed the top but left the sides riding the same knife-edge as
            // before. Combine a translate (helps faces near the pose origin, which a scale alone
            // barely moves) with a scale (helps faces far from the origin, proportionally) so
            // every face gets *some* separation regardless of view angle or position on the model.
            ps.pushPose();
            ps.translate(0, -0.015f, 0); // model space is Y-down here (see submit()'s scale(-1,-1,1))
            ps.scale(1.015f, 1.015f, 1.015f);
            collector.submitModel(getParentModel(), state, ps,
                    RenderTypes.entityCutout(getTextureForLayer(state.breed, LAYER_SADDLE)),
                    light, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            ps.popPose();
        }
    }

    private class DeathLayer extends RenderLayer<DragonRenderState, DragonModel>
    {
        DeathLayer()
        {
            super(DragonRenderer.this);
        }

        @Override
        public void submit(PoseStack ps, SubmitNodeCollector collector, int light, DragonRenderState state, float yRot, float xRot)
        {
            if (state.deathTime <= 0) return;

            // dissolve away, alpha describes how much of the body remains
            int color = ARGB.white(1f - (state.deathTime / state.maxDeathTime));
            collector.submitModel(getParentModel(), state, ps,
                    RenderTypes.entityCutoutDissolve(getTextureLocation(state), DISSOLVE_TEXTURE),
                    light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
        }
    }
}
