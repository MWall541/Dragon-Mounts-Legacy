package com.github.kay9.dragonmounts;

import net.minecraftforge.common.ForgeConfigSpec;

public class DMLConfig
{
    private static final ForgeConfigSpec.BooleanValue REPLENISH_EGGS;
    public static boolean replenishEggs()
    {
        return REPLENISH_EGGS.get();
    }

    static final ForgeConfigSpec SERVER;

    static
    {
        var configurator = new ForgeConfigSpec.Builder();
        REPLENISH_EGGS = configurator.comment("Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is deafeated?")
                .define("replenish_eggs", false);

        SERVER = configurator.build();
    }
}
