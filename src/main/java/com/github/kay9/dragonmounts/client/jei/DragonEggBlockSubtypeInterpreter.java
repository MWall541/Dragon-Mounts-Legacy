package com.github.kay9.dragonmounts.client.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class DragonEggBlockSubtypeInterpreter
        implements IIngredientSubtypeInterpreter<ItemStack> {

    @Override
    public String apply(ItemStack stack, UidContext context) {

        CompoundTag tag = stack.getTagElement("BlockEntityTag");

        if (tag != null && tag.contains("Breed")) {
            return tag.getString("Breed");
        }

        return IIngredientSubtypeInterpreter.NONE;
    }
}