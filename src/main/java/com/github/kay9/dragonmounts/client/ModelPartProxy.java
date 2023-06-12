package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

/**
 * Proxy for a model part that is used to project one model renderer on multiple
 * visible instances.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@SuppressWarnings("DataFlowIssue")
public class ModelPartProxy
{
    // projected parts and part childs
    public final ModelPart part;
    private final List<ModelPartProxy> children;
    // scale multiplier
    public float scaleX = 1;
    public float scaleY = 1;
    public float scaleZ = 1;
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

        children = part.getAllParts().skip(1).map(ModelPartProxy::new).toList();

        update();
    }

    public void copy(ModelPartProxy other)
    {
        other.x = x;
        other.y = y;
        other.z = z;

        other.xRot = xRot;
        other.yRot = yRot;
        other.zRot = zRot;

        other.scaleX = scaleX;
        other.scaleY = scaleY;
        other.scaleZ = scaleZ;

        other.visible = visible;

        if (children.size() != other.children.size())
            throw new IllegalArgumentException("Proxies do not share the same children.");
        for (int i = 0; i < children.size(); i++) children.get(i).copy(other.children.get(i));
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

        scaleX = mixinPart.getXScale();
        scaleY = mixinPart.getYScale();
        scaleZ = mixinPart.getZScale();

        visible = part.visible;

        for (ModelPartProxy child : children) child.update();
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

        ((ModelPartAccess)(Object) part).setRenderScale(scaleX, scaleY, scaleZ);

        part.visible = visible;

        for (ModelPartProxy child : children) child.apply();
    }

    public void render(PoseStack ps, VertexConsumer vertices, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        apply();
        part.render(ps, vertices, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}