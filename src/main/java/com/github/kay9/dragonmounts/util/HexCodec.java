package com.github.kay9.dragonmounts.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class HexCodec {
    public static final Codec<Integer> CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try
                {
                    return DataResult.success(Integer.parseInt(s, 16));
                }
                catch (NumberFormatException e)
                {
                    return DataResult.error(s + " is not a hex string.");
                }
            },
            Integer::toHexString
    );
}
