package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties().tab(CreativeModeTab.TAB_MISC));
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems)
    {
        if (allowdedIn(pCategory))
        {
            for (DragonBreed breed : BreedRegistry.registry()) pItems.add(create(breed));
        }
    }

    public static ItemStack create(DragonBreed breed)
    {
        CompoundTag root = new CompoundTag();

        // entity tag
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.getRegistryName().toString());
        root.put(EntityType.ENTITY_TAG, entityTag);

        // name & colors
        // storing these in the stack nbt is more performant than getting the breed everytime
        CompoundTag itemDataTag = new CompoundTag();
        itemDataTag.putString("ItemName", breed.getTranslationKey());
        itemDataTag.putInt("PrimaryColor", breed.primaryColor());
        itemDataTag.putInt("SecondaryColor", breed.secondaryColor());
        root.put("ItemData", itemDataTag);

        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG.get());
        stack.setTag(root);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack)
    {
        String name = BreedRegistry.FIRE.get().getTranslationKey();
        CompoundTag tag = stack.getTagElement("ItemData");
        if (tag != null) name = tag.getString("ItemName");
        return new TranslatableComponent(getDescriptionId(), new TranslatableComponent(name));
    }

    public static int getColor(ItemStack stack, int tintIndex)
    {
        CompoundTag tag = stack.getTagElement("ItemData");
        if (tag != null) return tintIndex == 0? tag.getInt("PrimaryColor") : tag.getInt("SecondaryColor");
        return tintIndex == 0? BreedRegistry.FIRE.get().primaryColor() : BreedRegistry.FIRE.get().secondaryColor();
    }
}
