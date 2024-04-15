package com.github.kay9.dragonmounts.util;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

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

    public static boolean noVerticalCollision(Level level, double atX, double atZ, double atYmin, double atYMax)
    {
        return level.noCollision(new AABB(atX, atYmin, atZ, atX, atYMax, atZ));
    }
}
