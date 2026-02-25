package com.github.kay9.dragonmounts.client.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class DragonEggSubtypeInterpreter
        implements IIngredientSubtypeInterpreter<ItemStack> {

    @Override
    public String apply(ItemStack stack, UidContext context) {

        CompoundTag entityTag = stack.getTagElement("EntityTag");

        if (entityTag != null && entityTag.contains("Breed")) {
            return entityTag.getString("Breed");
        }

        return IIngredientSubtypeInterpreter.NONE;
    }
}