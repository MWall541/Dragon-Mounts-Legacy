package wolfshotz.dml.client.model;

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

    public ModelPart(DragonModel model, int texX, int texY)
    {
        super(model, texX, texY);
        this.base = model;
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