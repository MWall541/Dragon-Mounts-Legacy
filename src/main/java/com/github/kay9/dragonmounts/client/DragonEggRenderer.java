package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DragonEggRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<DMLEggBlock.Entity>, IItemRenderProperties
{
    public static final DragonEggRenderer INSTANCE = new DragonEggRenderer();
    public static final Map<ResourceLocation, ResourceLocation> MODEL_CACHE = new HashMap<>(8);

    public DragonEggRenderer()
    {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        var breed = DragonBreed.BuiltIn.FIRE.location();
        var tag = pStack.getTagElement("BlockEntityTag");
        if (tag != null)
        {
            var stored = ResourceLocation.tryParse(tag.getString(TameableDragon.NBT_BREED));
            if (stored != null) breed = stored;
        }

        renderEgg(pPoseStack, buffer.getBuffer(Sheets.translucentItemSheet()), pPackedLight, breed, false);
    }

    @Override
    public void render(DMLEggBlock.Entity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource buffer, int pPackedLight, int pPackedOverlay)
    {
        renderEgg(pPoseStack, buffer.getBuffer(Sheets.translucentCullBlockSheet()), pPackedLight, pBlockEntity.getBreedId(), false);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getItemStackRenderer()
    {
        return this;
    }

    public static void renderEgg(PoseStack ps, VertexConsumer consumer, int light, ResourceLocation breed, boolean offset)
    {
        ps.pushPose();
        if (offset) ps.translate(-0.5D, 0.0D, -0.5D);
        var model = Minecraft.getInstance().getModelManager().getModel(MODEL_CACHE.get(breed));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ps.last(), consumer, null, model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
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
