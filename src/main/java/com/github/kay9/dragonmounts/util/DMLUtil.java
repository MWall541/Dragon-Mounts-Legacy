package com.github.kay9.dragonmounts.util;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

public class DMLUtil
{
    public static final Codec<Integer> HEX_CODEC = Codec.STRING.comapFlatMap(s ->
    {
        try
        {
            return DataResult.success(Integer.parseInt(s, 16));
        }
        catch (NumberFormatException e)
        {
            return DataResult.error(() -> String.format("[%s] Hexadecimal Codec error: '%s' is not a valid hex value.", DragonMountsLegacy.MOD_ID, s));
        }
    }, Integer::toHexString);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Registry<T> castRegistry(ResourceKey<? extends Registry<? extends T>> key)
    {
        return (Registry<T>) BuiltInRegistries.REGISTRY.getOrThrow((ResourceKey) key);
    }
}
