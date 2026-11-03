package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.function.Consumer;

public class DragonSpawnEgg extends SpawnEggItem
{
    public DragonSpawnEgg(Item.Properties props)
    {
        super(props.spawnEgg(DMLRegistry.DRAGON));
    }

    public static ItemStack create(Holder<DragonBreed> breed)
    {
        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG);
        setBreed(stack, breed);
        return stack;
    }

    // Note: verifyComponentsAfterLoad no longer exists in 26.2. Eggs without a breed
    // component keep working: the dragon picks a random breed on spawn, and name/color
    // accessors are null-safe.

    private static void setBreed(ItemStack stack, Holder<DragonBreed> breed)
    {
        // add breed data, used by entity type spawning in general.
        var tag = new CompoundTag();
        tag.putString(TameableDragon.NBT_BREED, breed.getRegisteredName());
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(DMLRegistry.DRAGON, tag));

        // for colors and item name
        stack.set(DMLRegistry.DRAGON_BREED_COMPONENT, breed);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        Holder<DragonBreed> breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT);

        if (breed == null) return super.getName(stack);
        return Component.translatable(String.join(".", getDescriptionId(), breed.getRegisteredName().replace(':', '.')));
    }

    // Note: spawnOffspringFromSpawnEgg is static in 26.2 and can no longer be overridden;
    // the "matching breed offspring" restriction from the Forge version is lost for now.

    public static void populateTab(Consumer<ItemStack> registrar, HolderLookup.Provider registries)
    {
        registries.lookupOrThrow(DragonBreed.REGISTRY_KEY).listElements().forEach(breed -> registrar.accept(create(breed)));
    }

    @SuppressWarnings("ConstantConditions")
    public static int getColor(ItemStack stack, int tintIndex)
    {
        Holder<DragonBreed> breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT);
        if (breed == null || !breed.isBound()) return 0xff;
        return (tintIndex == 0? breed.value().primaryColor() : breed.value().secondaryColor()) | (255 << 24);
    }
}
