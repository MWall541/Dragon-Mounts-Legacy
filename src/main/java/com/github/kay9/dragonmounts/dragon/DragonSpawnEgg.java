package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties());
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
        itemDataTag.putString("ItemName", DragonBreed.getTranslationKey(breed.id(reg).toString()));
        itemDataTag.putInt("PrimaryColor", breed.primaryColor());
        itemDataTag.putInt("SecondaryColor", breed.secondaryColor());
        root.put("ItemData", itemDataTag);

        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG.get());
        stack.setTag(root);
        return stack;
    }

    public static void populateTab(BuildCreativeModeTabContentsEvent evt)
    {
        if (Minecraft.getInstance().level != null)
        {
            var reg = Minecraft.getInstance().level.m_9598_();
            for (DragonBreed breed : BreedRegistry.registry(reg))
                evt.accept(create(breed, reg));
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        preconditionSpawnEgg(stack); // extremely hacky to be doing it here... but there doesn't seem to be any other options.
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        var name = "";
        var tag = stack.getTagElement("ItemData");
        if (tag != null) name = tag.getString("ItemName");
        return Component.translatable(getDescriptionId(), Component.translatable(name));
    }

    public static int getColor(ItemStack stack, int tintIndex)
    {
        var tag = stack.getTagElement("ItemData");
        if (tag != null)
            return tintIndex == 0? tag.getInt("PrimaryColor") : tag.getInt("SecondaryColor");
        return 0xffffff;
    }

    @SuppressWarnings("ConstantConditions")
    private static void preconditionSpawnEgg(ItemStack stack)
    {
        if (ServerLifecycleHooks.getCurrentServer() == null) return;

        var root = stack.getOrCreateTag();
        var blockEntityData = stack.getOrCreateTagElement(EntityType.ENTITY_TAG);
        var breedId = blockEntityData.getString(TameableDragon.NBT_BREED);
        var regAcc = ServerLifecycleHooks.getCurrentServer().registryAccess();
        var reg = BreedRegistry.registry(regAcc);

        if (breedId.isEmpty() || !reg.containsKey(new ResourceLocation(breedId))) // this item doesn't contain a breed yet?
        {
            // assign one ourselves then.
            var breed = reg.getRandom(RandomSource.create()).orElseThrow();
            var updated = create(breed.get(), regAcc);
            root.merge(updated.getTag());
        }
    }
}
