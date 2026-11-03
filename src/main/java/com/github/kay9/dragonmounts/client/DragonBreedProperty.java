package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item model select property yielding the registered name ({@code "namespace:breed"}) of the
 * dragon breed stored on the item stack, or {@code null} if it has none.
 * Used by {@code items/dragon_egg.json} to pick the breed-specific egg model.
 */
public record DragonBreedProperty() implements SelectItemModelProperty<String>
{
    public static final DragonBreedProperty INSTANCE = new DragonBreedProperty();
    public static final Type<DragonBreedProperty, String> TYPE = Type.create(MapCodec.unit(INSTANCE), Codec.STRING);

    @Nullable
    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext)
    {
        var breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT);
        return breed == null? null : breed.getRegisteredName();
    }

    @Override
    public Codec<String> valueCodec()
    {
        return Codec.STRING;
    }

    @Override
    public Type<? extends SelectItemModelProperty<String>, String> type()
    {
        return TYPE;
    }
}
