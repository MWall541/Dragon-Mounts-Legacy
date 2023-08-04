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
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    private static final String DATA_TAG = "ItemData";
    private static final String DATA_ITEM_NAME = "ItemName";
    private static final String DATA_PRIM_COLOR = "PrimaryColor";
    private static final String DATA_SEC_COLOR = "SecondaryColor";

    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties());
    }

    public static ItemStack create(DragonBreed breed, RegistryAccess reg)
    {
        var id = breed.id(reg);
        var root = new CompoundTag();

        // entity tag
        var entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, id.toString());
        root.put(EntityType.ENTITY_TAG, entityTag);

        // name & colors
        // storing these in the stack nbt is more performant than getting the breed everytime
        var itemDataTag = new CompoundTag();
        itemDataTag.putString(DATA_ITEM_NAME, String.join(".", DMLRegistry.SPAWN_EGG.get().getDescriptionId(), id.getNamespace(), id.getPath()));
        itemDataTag.putInt(DATA_PRIM_COLOR, breed.primaryColor());
        itemDataTag.putInt(DATA_SEC_COLOR, breed.secondaryColor());
        root.put(DATA_TAG, itemDataTag);

        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG.get());
        stack.setTag(root);
        return stack;
    }

    public static void populateTab(Consumer<ItemStack> registrar)
    {
        if (Minecraft.getInstance().level != null)
        {
            var reg = Minecraft.getInstance().level.registryAccess();
            for (DragonBreed breed : BreedRegistry.registry(reg))
                registrar.accept(create(breed, reg));
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
        var tag = stack.getTagElement(DATA_TAG);
        if (tag == null || tag.contains(DATA_ITEM_NAME))
            return Component.translatable(tag.getString(DATA_ITEM_NAME));
        return super.getName(stack);
    }

    public static int getColor(ItemStack stack, int tintIndex)
    {
        var tag = stack.getTagElement(DATA_TAG);
        if (tag != null)
            return tintIndex == 0? tag.getInt(DATA_PRIM_COLOR) : tag.getInt(DATA_SEC_COLOR);
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
