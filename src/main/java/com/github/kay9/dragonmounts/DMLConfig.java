package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Map;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

public class DMLConfig
{
    public static final ForgeConfigSpec COMMON_SPEC;

    private static final ForgeConfigSpec.BooleanValue ALLOW_EGG_OVERRIDE;

    public static boolean allowEggOverride()
    {
        return ALLOW_EGG_OVERRIDE.get();
    }

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

    private static final Map<String, ForgeConfigSpec.DoubleValue> EGG_CHANCES;

    public static float getEggChanceFor(String configTarget)
    {
        var chance = EGG_CHANCES.get(configTarget);
        if (chance == null) return -1f;
        return chance.get().floatValue();
    }

    private static final Map<String, ForgeConfigSpec.IntValue> REPRO_LIMITS;

    public static int getReproLimitFor(String configTarget)
    {
        var limit = REPRO_LIMITS.get(configTarget.replace("config:", ""));
        if (limit == null) return -1;
        return limit.get();
    }

    static
    {
        var configurator = new ForgeConfigSpec.Builder()
                .push("config");

        ALLOW_EGG_OVERRIDE = configurator.comment(
                        "Allow the vanilla ender egg to be interacted with? (Hatchable)",
                        "Useful to help with mod compatibility")
                .define("allow_egg_override", true);
        REPLENISH_EGGS = configurator.comment(
                        "Should Ender Dragon Eggs replenish on the exit portal after a respawned dragon is defeated?",
                        "Useful for multiplayer scenarios.",
                        "NOTE: This may break with mods that add content in the end. (A big example is YUNG's better end islands)",
                        "You should see if those mods have ways to replenish dragon eggs themselves.")
                .define("replenish_eggs", true);
        USE_LOOT_TABLES = configurator.comment(
                        "Should dragon eggs generate in treasure chest loot tables?",
                        "Useful for multiplayer scenarios and offering alternative ways to obtain eggs.",
                        "Egg chance values can be modified below. If you'd like to change the loot tables the eggs appear",
                        "in, consider using a datapack, since the static nature of configs complicates things.")
                .define("use_loot_tables", false);
        UPDATE_HABITATS = configurator.comment("Should Dragon Eggs adapt to their environments and change breeds?")
                .define("update_habitats", true);

        configurator.pop();


        configurator.comment(
                        "!!! THESE VALUES DO NOT TAKE EFFECT UNTIL `use_loot_tables` ABOVE IS SET TO `true` !!!",
                        "These entries define the chance values of which a dragon egg can appear in its respective loot table.",
                        "Due to the static nature of configs in general, DML cannot modify the chances of custom breed eggs",
                        "outside the built-in defaults, so those should be configured to use minecraft's built in random chance conditions.",
                        "(It is however, possible to point custom egg chances to the built-in values via the loot condition, therefore using a config chance)")
                .push("egg_loot_chances");
        EGG_CHANCES = defineChanceEntries(configurator);
        configurator.pop();


        configurator.comment(
                "These entries define the reproduction (breed) limits of each dragon breed.",
                "Due to the static nature of configs in general, DML cannot modify the reproduction limits of custom breeds",
                "outside the mod's built in breeds. Those should be configured in datapacks to use a direct number rather",
                "than point to an entry here (unless that's the goal.)")
                .push("reproduction_limits");
        REPRO_LIMITS = defineReproLimEntries(configurator);
        configurator.pop();


        COMMON_SPEC = configurator.build();
    }

    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue CAMERA_DRIVEN_FLIGHT;

    public static boolean cameraDrivenFlight()
    {
        return CAMERA_DRIVEN_FLIGHT.get();
    }

    public static final ForgeConfigSpec.BooleanValue THIRD_PERSON_ON_MOUNT;

    public static boolean thirdPersonOnMount()
    {
        return THIRD_PERSON_ON_MOUNT.get();
    }

    // [0][0..2] = Back third person  ; distance, vertical, horizontal
    // [1][0..2] = Front third person ; distance, vertical, horizontal
    private static final ForgeConfigSpec.DoubleValue[][] CAMERA_OFFSETS = new ForgeConfigSpec.DoubleValue[2][3];

    public static ForgeConfigSpec.DoubleValue[] getCameraPerspectiveOffset(boolean back)
    {
        return CAMERA_OFFSETS[back? 0 : 1];
    }

    static
    {
        var configurator = new ForgeConfigSpec.Builder()
                .push("client");

        CAMERA_DRIVEN_FLIGHT = configurator.comment(
                "Is dragon flight vertical movement driven by the pitch of the game camera?",
                "This option can be toggled in-game via keybinding for quick switching.",
                "If you choose to disable this, vertical movement can still be achieved via dedicated keybindings.")
                .define("camera_driven_flight", true);

        THIRD_PERSON_ON_MOUNT = configurator.comment(
                "When mounting and dismounting a dragon, should the camera automatically switch third-person perspectives?")
                .define("third_person_on_mount", true);

        configurator.pop();

        configurator.comment(
                "The values that define the offset of the camera position when riding a dragon in the third person camera.")
                .push("camera_offsets");
        defineCameraOffsetEntries(configurator);
        configurator.pop();

        CLIENT_SPEC = configurator.build();
    }

    private static ImmutableMap<String, ForgeConfigSpec.DoubleValue> defineChanceEntries(ForgeConfigSpec.Builder configurator)
    {
        var chances = ImmutableMap.<String, ForgeConfigSpec.DoubleValue>builder();
        for (var target : DragonEggLootMod.BUILT_IN_CHANCES)
        {
            var path = formatEggTargetAsPath(target.forBreed(), target.target());
            var entry = configurator.comment(
                            String.format("The chance that a %s egg appears in %s.", target.forBreed().location().getPath(), target.target().location().getPath()),
                            "0 = Never Appears, 1 = Guaranteed")
                    .defineInRange(path, target.chance(), 0, 1);

            chances.put(path, entry);
        }
        return chances.build();
    }

    private static ImmutableMap<String, ForgeConfigSpec.IntValue> defineReproLimEntries(ForgeConfigSpec.Builder configurator)
    {
        var lims = ImmutableMap.<String, ForgeConfigSpec.IntValue>builder();
        for (var type : new ResourceKey[]{AETHER, END, FIRE, FOREST, GHOST, ICE, NETHER, WATER})
        {
            var path = type.location().getPath();
            lims.put(path, configurator.comment(
                    "-1 = No Limit, 0 = Cannot breed, 2 = Can breed only two times")
                    .defineInRange(path, TameableDragon.BASE_REPRO_LIMIT, -1, Integer.MAX_VALUE));
        }
        return lims.build();
    }

    public static String formatEggTargetAsPath(ResourceKey<DragonBreed> forBreed, ResourceKey<LootTable> forTarget)
    {
        return String.format("%s_in_%s_chance",
                forBreed.location().getPath(),
                forTarget.location().getPath().substring(forTarget.location().getPath().lastIndexOf('/') + 1));
    }

    private static void defineCameraOffsetEntries(ForgeConfigSpec.Builder configurator)
    {
        var perspectiveName = "back";
        for (ForgeConfigSpec.DoubleValue[] perspective : CAMERA_OFFSETS)
        {
            configurator.push("third_person_" + perspectiveName);
            perspectiveName = "front";

            perspective[0] = configurator.defineInRange("distance", 6.0, -3, 1000);
            perspective[1] = configurator.defineInRange("vertical", 4.0, -3, 1000);
            perspective[2] = configurator.defineInRange("horizontal", 0.0, -1000, 1000);

            configurator.pop();
        }
    }
}
