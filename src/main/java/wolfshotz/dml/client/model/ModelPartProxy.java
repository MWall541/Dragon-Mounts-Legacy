package wolfshotz.dml.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Proxy for a model part that is used to project one model renderer on multiple
 * visible instances.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ModelPartProxy
{

    // projected parts and part childs
    private final ModelPart part;
    private final List<ModelPartProxy> childs;
    // scale multiplier
    private float renderScaleX = 1;
    private float renderScaleY = 1;
    private float renderScaleZ = 1;
    // rotation points
    private float rotationPointX;
    private float rotationPointY;
    private float rotationPointZ;
    // rotation angles
    private float preRotateAngleX;
    private float preRotateAngleY;
    private float preRotateAngleZ;
    private float rotateAngleX;
    private float rotateAngleY;
    private float rotateAngleZ;
    // misc meta data
    private boolean hidden;
    private boolean showModel;

    /**
     * Constructs a new proxy for the given model part.
     *
     * @param part model part to project on this proxy
     */
    public ModelPartProxy(ModelPart part)
    {
        this.part = part;

        if (part != null)
        {
            childs = new ArrayList<>();
            for (Object childModel : part.childModels) childs.add(new ModelPartProxy((ModelPart) childModel));
        }
        else childs = null;

        update();
    }

    /**
     * Saves the properties of the model part to this proxy with the default
     * rendering scale.
     */
    public final void update()
    {
        renderScaleX = part.renderScaleX;
        renderScaleY = part.renderScaleY;
        renderScaleZ = part.renderScaleZ;

        rotationPointX = part.rotationPointX;
        rotationPointY = part.rotationPointY;
        rotationPointZ = part.rotationPointZ;

        preRotateAngleX = part.preRotateAngleX;
        preRotateAngleY = part.preRotateAngleY;
        preRotateAngleZ = part.preRotateAngleZ;

        rotateAngleX = part.rotateAngleX;
        rotateAngleY = part.rotateAngleY;
        rotateAngleZ = part.rotateAngleZ;

        hidden = !part.showModel;
        showModel = part.showModel;

        if (childs != null)
        {
            for (ModelPartProxy child : childs)
            {
                child.update();
            }
        }
    }

    /**
     * Restores the properties from this proxy to the model part.
     */
    public final void apply()
    {
        part.renderScaleX = renderScaleX;
        part.renderScaleY = renderScaleY;
        part.renderScaleZ = renderScaleZ;

        part.rotationPointX = rotationPointX;
        part.rotationPointY = rotationPointY;
        part.rotationPointZ = rotationPointZ;

        part.preRotateAngleX = preRotateAngleX;
        part.preRotateAngleY = preRotateAngleY;
        part.preRotateAngleZ = preRotateAngleZ;

        part.rotateAngleX = rotateAngleX;
        part.rotateAngleY = rotateAngleY;
        part.rotateAngleZ = rotateAngleZ;

        part.showModel = hidden;
        part.showModel = showModel;

        if (childs != null)
        {
            for (ModelPartProxy child : childs)
            {
                child.apply();
            }
        }
    }

    public void render(MatrixStack matrixStack, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        apply();
        part.render(matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}