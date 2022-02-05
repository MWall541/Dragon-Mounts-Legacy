package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Proxy for a model part that is used to project one model renderer on multiple
 * visible instances.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ModelPartProxy
{
    // projected parts and part childs
    public final ModelPart part;
    private final List<ModelPartProxy> childs;
    // scale multiplier
    public float renderScaleX = 1;
    public float renderScaleY = 1;
    public float renderScaleZ = 1;
    // rotation points
    private float x;
    private float y;
    private float z;
    // rotation angles
    private float xRot;
    private float yRot;
    private float zRot;
    // misc meta data
    private boolean visible;

    /**
     * Constructs a new proxy for the given model part.
     *
     * @param part model part to project on this proxy
     */
    public ModelPartProxy(ModelPart part)
    {
        this.part = part;

        childs = part.getAllParts().skip(1).map(ModelPartProxy::new).collect(Collectors.toList());

        update();
    }

    /**
     * Saves the properties of the model part to this proxy with the default
     * rendering scale.
     */
    public final void update()
    {
        x = part.x;
        y = part.y;
        z = part.z;

        xRot = part.xRot;
        yRot = part.yRot;
        zRot = part.zRot;

        ModelPartAccess mixinPart = (ModelPartAccess)(Object) part;

        renderScaleX = mixinPart.getXScale();
        renderScaleY = mixinPart.getYScale();
        renderScaleZ = mixinPart.getZScale();

        visible = part.visible;

        for (ModelPartProxy child : childs) child.update();
    }

    /**
     * Restores the properties from this proxy to the model part.
     */
    public final void apply()
    {
        part.x = x;
        part.y = y;
        part.z = z;

        part.xRot = xRot;
        part.yRot = yRot;
        part.zRot = zRot;

        ((ModelPartAccess)(Object) part).setRenderScale(renderScaleX, renderScaleY, renderScaleZ);

        part.visible = visible;

        for (ModelPartProxy child : childs) child.apply();
    }

    public void render(PoseStack ps, VertexConsumer vertices, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        apply();
        part.render(ps, vertices, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}