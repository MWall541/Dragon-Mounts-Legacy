package com.github.kay9.dragonmounts.util;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.mojang.serialization.Codec;
import net.minecraft.Util;

public class DMLUtil
{
    public static final Codec<Integer> HEX_CODEC = Codec.STRING.xmap(s -> Integer.parseInt(s, 16), Integer::toHexString)
            .promotePartial(Util.prefix("The value given is not a hex value.", DragonMountsLegacy.LOG::error));
}
