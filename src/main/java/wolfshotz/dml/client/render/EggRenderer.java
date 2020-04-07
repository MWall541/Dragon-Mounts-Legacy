package wolfshotz.dml.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

import java.util.Random;

public class EggRenderer extends EntityRenderer<DragonEggEntity>
{
    public EggRenderer(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void render(DragonEggEntity entityIn, float entityYaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int packedLightIn)
    {
        // prepare egg rendering
        BlockPos blockpos = entityIn.getPosition();
        BlockState state = DragonEggBlock.DRAGON_EGG.getDefaultState().with(DragonEggBlock.BREED, entityIn.eggType);

        if (state.getRenderType() == BlockRenderType.MODEL)
        {
            if (state != entityIn.world.getBlockState(new BlockPos(entityIn)) && state.getRenderType() != BlockRenderType.INVISIBLE)
            {
                ms.push();
                ms.translate(-0.5D, 0.0D, -0.5D);
                BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                for (RenderType type : RenderType.getBlockRenderTypes())
                {
                    if (RenderTypeLookup.canRenderInLayer(state, type))
                    {
                        ForgeHooksClient.setRenderLayer(type);
                        dispatcher.getBlockModelRenderer().renderModel(entityIn.world, dispatcher.getModelForState(state), state, blockpos, ms, buffer.getBuffer(type), false, new Random(), state.getPositionRandom(entityIn.getPosition()), OverlayTexture.NO_OVERLAY);
                    }
                }
                ForgeHooksClient.setRenderLayer(null);
                ms.pop();
                super.render(entityIn, entityYaw, partialTicks, ms, buffer, packedLightIn);
            }
        }
    }

    @Override
    public ResourceLocation getEntityTexture(DragonEggEntity entity)
    {
        return null;
    }
}
