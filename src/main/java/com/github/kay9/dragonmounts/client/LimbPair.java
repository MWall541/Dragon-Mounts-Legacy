package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.accessors.ModelPartAccess;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

import java.util.stream.Collectors;

public class LimbPair
{
    private final ImmutableMap<ModelPart, ModelPart> limbPairs;
    private final boolean biped;

    public LimbPair(ModelPart mainLimb, ModelPart copyingLimb, boolean biped)
    {
        var main = mainLimb.getAllParts().collect(Collectors.toList());
        var copying = copyingLimb.getAllParts().collect(Collectors.toList());

        if (main.size() != copying.size()) throw new IllegalArgumentException("Limb Pair do not match limb part count");

        ImmutableMap.Builder<ModelPart, ModelPart> builder = ImmutableMap.builder();
        for (int i = 0; i < main.size(); i++) builder.put(main.get(i), copying.get(i));

        this.limbPairs = builder.build();
        this.biped = biped;
    }

    public void apply()
    {
        limbPairs.forEach((m, c) ->
        {
            c.copyFrom(m);

            if (biped) c.x = -m.x;

            ModelPartAccess mixin = ((ModelPartAccess)(Object) m);
            ((ModelPartAccess)(Object) c).setRenderScale(mixin.getXScale(), mixin.getYScale(), mixin.getZScale());
        });
    }
}
