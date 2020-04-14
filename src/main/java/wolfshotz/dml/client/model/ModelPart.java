package wolfshotz.dml.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Extended model renderer with some helpful extra methods.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ModelPart extends ModelRenderer
{
    public float renderScaleX = 1;
    public float renderScaleY = 1;
    public float renderScaleZ = 1;

    public float preRotateAngleX;
    public float preRotateAngleY;
    public float preRotateAngleZ;

    private DragonModel base;

    public ModelPart(DragonModel base)
    {
        super(base);
        this.base = base;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn)
    {
        super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void translateRotate(MatrixStack ms)
    {
//        super.translateRotate(ms);
        // skip if hidden
        if (!showModel) return;

        // translate
        ms.translate(rotationPointX / 16f, rotationPointY / 16f, rotationPointZ / 16f);

        // rotate
        if (preRotateAngleZ != 0) ms.rotate(Vector3f.ZP.rotation(preRotateAngleZ));
        if (preRotateAngleY != 0) ms.rotate(Vector3f.YP.rotation(preRotateAngleY));
        if (preRotateAngleX != 0) ms.rotate(Vector3f.XP.rotation(preRotateAngleX));

        if (rotateAngleZ != 0.0F) ms.rotate(Vector3f.ZP.rotation(rotateAngleZ));
        if (rotateAngleY != 0.0F) ms.rotate(Vector3f.YP.rotation(rotateAngleY));
        if (rotateAngleX != 0.0F) ms.rotate(Vector3f.XP.rotation(rotateAngleX));

        // scale
        if (renderScaleX != 0 || renderScaleY != 0 || renderScaleZ != 0)
            ms.scale(renderScaleX, renderScaleY, renderScaleZ);
    }

    public ModelPart addBox(float x, float y, float z, int width, int height, int depth, int textureOffsetX, int textureOffsetY)
    {
        return (ModelPart) addBox("", x, y, z, width, height, depth, 0, textureOffsetX, textureOffsetY);
    }

    public ModelPart addChildBox(float x, float y, float z, int width, int length, int height, int textureOffsetX, int textureOffsetY)
    {
        ModelPart part = new ModelPart(base);
        part.mirror = mirror;
        part.addBox(x, y, z, width, length, height, textureOffsetX, textureOffsetY);
        addChild(part);

        return part;
    }

    public ModelPart setAngles(float x, float y, float z)
    {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;

        return this;
    }

    public ModelPart setRenderScale(float scaleX, float scaleY, float scaleZ)
    {
        this.renderScaleX = scaleX;
        this.renderScaleY = scaleY;
        this.renderScaleZ = scaleZ;

        return this;
    }

    public ModelPart setRenderScale(float scale) { return setRenderScale(scale, scale, scale); }
}