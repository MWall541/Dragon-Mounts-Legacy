package com.github.kay9.dragonmounts.entity.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.entity.dragon.breed.DragonBreed;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.loading.FMLLoader;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties().tab(CreativeModeTab.TAB_MISC));
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems)
    {
        if (FMLLoader.getDist().isClient() && allowdedIn(pCategory) && Minecraft.getInstance().level != null)
        {
            var reg = Minecraft.getInstance().level.registryAccess();
            for (DragonBreed breed : BreedRegistry.registry(reg))
                pItems.add(create(breed, reg));
        }

    }

    public static ItemStack create(DragonBreed breed, RegistryAccess reg)
    {
        CompoundTag root = new CompoundTag();

        // entity tag
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.id(reg).toString());
        root.put(EntityType.ENTITY_TAG, entityTag);

        // name & colors
        // storing these in the stack nbt is more performant than getting the breed everytime
        CompoundTag itemDataTag = new CompoundTag();
        itemDataTag.putString("ItemName", breed.getTranslationKey(reg));
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
        String name;
        var tag = stack.getTagElement("ItemData");
        if (tag == null || (name = tag.getString("ItemName")).isEmpty())
            name = BreedRegistry.getFallback(Minecraft.getInstance().level.registryAccess())
                    .getTranslationKey(Minecraft.getInstance().level.registryAccess());
        return new TranslatableComponent(getDescriptionId(), new TranslatableComponent(name));
    }

    public static int getColor(ItemStack stack, int tintIndex)
    {
        int prim;
        int sec;
        var tag = stack.getTagElement("ItemData");
        if (tag != null)
        {
            prim = tag.getInt("PrimaryColor");
            sec = tag.getInt("SecondaryColor");
        }
        else
        {
            var fire = BreedRegistry.getFallback(Minecraft.getInstance().level.registryAccess());
            prim = fire.primaryColor();
            sec = fire.secondaryColor();
        }

        return tintIndex == 0? prim : sec;
    }
}
