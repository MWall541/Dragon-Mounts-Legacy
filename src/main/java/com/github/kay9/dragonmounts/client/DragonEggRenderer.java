package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.HashMap;
import java.util.Map;

public class DragonEggRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<DMLEggBlock.Entity>, IItemRenderProperties
{
    public static final DragonEggRenderer INSTANCE = new DragonEggRenderer();
    private static final Map<ResourceLocation, ResourceLocation> MODEL_CACHE = new HashMap<>(8);

    public DragonEggRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        var breed = DragonBreed.FIRE;
        var tag = pStack.getTagElement("BlockEntityTag");
        if (tag != null) breed = BreedManager.read(tag.getString(TameableDragon.NBT_BREED));
        renderEgg(pPoseStack, buffer.getBuffer(RenderType.solid()), pPackedLight, breed, false);
    }

    @Override
    public void render(DMLEggBlock.Entity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        renderEgg(pPoseStack, buffer.getBuffer(RenderType.solid()), pPackedLight, pBlockEntity.getBreed(), false);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getItemStackRenderer()
    {
        return this;
    }

    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, DragonBreed breed, boolean offset)
    {
        ps.pushPose();
        if (offset) ps.translate(-0.5D, 0.0D, -0.5D);
        ForgeHooksClient.setRenderType(RenderType.solid());
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODEL_CACHE.computeIfAbsent(breed.id(), id -> new ResourceLocation(id.getNamespace(), "block/dragon_eggs/" + id.getPath() + "_dragon_egg")));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), consumer, DMLRegistry.EGG_BLOCK.get().defaultBlockState(), model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY);
        ForgeHooksClient.setRenderType(null);
        ps.popPose();
    }

    /**
     * Here to provide a way to create a method reference factory to return the instance.
     * Still confused? I'm exploiting javas shitty class loading ways.
     */
    @SuppressWarnings("unused")
    public static DragonEggRenderer instance(BlockEntityRendererProvider.Context context)
    {
        return INSTANCE;
    }
}
