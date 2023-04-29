package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Predicate;

public class DMLConfig
{
    static final ForgeConfigSpec COMMON;

    private static final ForgeConfigSpec.BooleanValue ALLOW_EGG_OVERRIDE;
    public static boolean allowEggOverride()
    {
        return ALLOW_EGG_OVERRIDE.get();
    }

    static final ForgeConfigSpec SERVER;

    private static final ForgeConfigSpec.BooleanValue REPLENISH_EGGS;
    public static boolean replenishEggs()
    {
        return REPLENISH_EGGS.get();
    }

    private static final ForgeConfigSpec.BooleanValue USE_LOOT_TABLES;
    public static boolean useLootTables()
    {
        return USE_LOOT_TABLES.get();
    }

    private static final ForgeConfigSpec.BooleanValue UPDATE_HABITATS;
    public static boolean updateHabitats()
    {
        return UPDATE_HABITATS.get();
    }

    private static final ForgeConfigSpec.IntValue REPRO_LIMIT;
    public static int reproLimit()
    {
        return REPRO_LIMIT.get();
    }

    private static final ForgeConfigSpec.EnumValue<GriefRules> DRAGON_GRIEF_RULES;
    public static boolean canGrief(TameableDragon dragon)
    {
        return DRAGON_GRIEF_RULES.get().canGrief(dragon) && ForgeEventFactory.getMobGriefingEvent(dragon.getLevel(), dragon);
    }

    private static final ForgeConfigSpec.DoubleValue FIRE_BREATH_SPREAD;
    public static double getFireSpreadMultiplier()
    {
        return FIRE_BREATH_SPREAD.get();
    }

    private static final ForgeConfigSpec.DoubleValue FIRE_BREATH_BURN;
    public static double getBreathBurnMultiplier()
    {
        return FIRE_BREATH_BURN.get();
    }

    static final ForgeConfigSpec CLIENT;

    private static final ForgeConfigSpec.BooleanValue CAMERA_FLIGHT;
    public static boolean cameraFlight()
    {
        return CAMERA_FLIGHT.get();
    }

    static // common
    {
        var configurator = new ForgeConfigSpec.Builder();

        ALLOW_EGG_OVERRIDE = configurator.comment("Allow the vanilla ender egg to be interacted with? (Hatchable)",
                        "Useful to help with mod compatability")
                .define("allow_egg_override", true);

        COMMON = configurator.build();
    }

    static // server
    {
        var configurator = new ForgeConfigSpec.Builder();

        REPLENISH_EGGS = configurator.comment("Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is deafeated?",
                        "Useful for multiplayer scenarios.")
                .define("replenish_eggs", true);
        USE_LOOT_TABLES = configurator.comment("Should dragon eggs generate in treasure chest loot tables?",
                        "Useful for multiplayer scenarios and offering alternative ways to obtain eggs.",
                        "Different types of egg breeds can be found in different chests (if configured.)")
                .define("use_loot_tables", false);
        UPDATE_HABITATS = configurator.comment("Should Dragon Eggs adapt to their environments and change breeds?")
                .define("update_habitats", true);
        REPRO_LIMIT = configurator.comment("Number of times a dragon is able to breed.")
                .defineInRange("breed_limit", TameableDragon.BASE_REPRO_LIMIT, 0, Integer.MAX_VALUE);
        DRAGON_GRIEF_RULES = configurator.comment("Determines if dragons can destroy environments.")
                .defineEnum("grief_rule", GriefRules.RESPECT_MOB_GRIEFING, GriefRules.values());
        FIRE_BREATH_SPREAD = configurator.comment("A multiplier to influence the flammability of fire breath.",
                        "Useful to reduce or increase the chances of fire spawning.",
                        "0 = disabled")
                .defineInRange("breath_flammability", 1, 0, Double.MAX_VALUE);
        FIRE_BREATH_BURN = configurator.comment("A multiplier to influence the incineration/scorching of fire breath.",
                "Useful to reduce or increase the chances of blocks incinerating or scorching.")
                .defineInRange("breath_burn_rate", 1, 0, Double.MAX_VALUE);

        SERVER = configurator.build();
    }

    static // client
    {
        var configurator = new ForgeConfigSpec.Builder();

        CAMERA_FLIGHT = configurator.comment("Should flight controls use the game camera (true) or vertical keybinds? (false)")
                .define("camera_flight_controls", true);

        CLIENT = configurator.build();
    }

    private enum GriefRules
    {
        NEVER(t -> false),
        RESPECT_MOB_GRIEFING(t -> t.getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)),
        ALWAYS(t -> true);

        private final Predicate<TameableDragon> canGrief;

        GriefRules(Predicate<TameableDragon> canGrief)
        {
            this.canGrief = canGrief;
        }

        public boolean canGrief(TameableDragon dragon)
        {
            return canGrief.test(dragon);
        }
    }
}
