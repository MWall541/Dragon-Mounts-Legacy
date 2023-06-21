package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DragonEggRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<HatchableEggBlockEntity>, IClientItemExtensions
{
    public static final DragonEggRenderer INSTANCE = new DragonEggRenderer();
    public static final Map<ResourceLocation, ResourceLocation> MODEL_CACHE = new HashMap<>(8);
    private static final ResourceLocation DEFAULT_MODEL = new ResourceLocation("block/dragon_egg");

    public DragonEggRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext context, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        ResourceLocation breed = null;
        var tag = pStack.getTagElement("BlockEntityTag");
        if (tag != null) breed = ResourceLocation.tryParse(tag.getString(TameableDragon.NBT_BREED));
        renderEgg(pPoseStack, buffer.getBuffer(Sheets.translucentItemSheet()), pPackedLight, breed, false, 0);
    }

    @Override
    public void render(HatchableEggBlockEntity data, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        renderEgg(pPoseStack, buffer.getBuffer(Sheets.translucentCullBlockSheet()), pPackedLight, data.getBreed().id(Minecraft.getInstance().level.m_9598_()), false, data.getBlockState().getValue(HatchableEggBlock.HATCH_STAGE));
    }

    @Override
    public boolean shouldRender(HatchableEggBlockEntity data, Vec3 pCameraPos)
    {
        return data.getBreed() != null && BlockEntityRenderer.super.shouldRender(data, pCameraPos);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return this;
    }

    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, ResourceLocation breed, boolean offset, int hatchStage)
    {
        ps.pushPose();
        if (offset) ps.translate(-0.5D, 0.0D, -0.5);
        var model = Minecraft.getInstance().getModelManager().getModel(MODEL_CACHE.getOrDefault(breed, DEFAULT_MODEL));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), consumer, null, model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
        if (hatchStage != 0)
        {
            var stack = ps.last();
            var buffer = Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(10));
            var breakConsumer = new SheetedDecalTextureGenerator(buffer, stack.pose(), stack.normal(), 1f);
            Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(DMLRegistry.EGG_BLOCK.get().defaultBlockState(), BlockPos.ZERO, Minecraft.getInstance().level, ps, breakConsumer, ModelData.EMPTY);
        }
        ps.popPose();
    }

    /**
     * Here to provide a way to create a method reference factory to return the instance.
     * Still confused? I'm exploiting javas shitty class loading ways.
     * <p>
     * Don't use this as a getter. Simply use the public field.
     */
    @SuppressWarnings({"unused", "SameReturnValue"})
    @NotNull
    public static DragonEggRenderer instance(Object context)
    {
        return INSTANCE;
    }
}
