package com.github.kay9.dragonmounts.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Map;

import static com.github.kay9.dragonmounts.dragon.breed.DragonBreed.BuiltIn.*;

public class EggLootConfig
{
    public static Target[] BUILT_IN_CHANCES = new Target[]{
            new Target(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.2),
            new Target(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.085),
            new Target(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3),
            new Target(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2),
            new Target(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.075),
            new Target(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2),
            new Target(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35),
            new Target(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175)
    };

    private static final Map<String, ForgeConfigSpec.DoubleValue> CHANCES;

    public static final ForgeConfigSpec SPEC;

    public static float getProbabilityFor(ResourceLocation breed, ResourceLocation target)
    {
        var chance = CHANCES.get(breed.toString() + target.toString()); // hacky hashing stuff; see below
        if (chance == null) return -1f;
        return chance.get().floatValue();
    }

    static // loot chances
    {
        var configurator = new ForgeConfigSpec.Builder();
        var chances = ImmutableMap.<String, ForgeConfigSpec.DoubleValue>builder();

        configurator.comment(
                "These entries define the chance values of which a dragon egg can appear in its respective loot table.",
                "Due to the static nature of configs in general, DML cannot modify the chances of custom breed eggs",
                "outside the built-in defaults, so those will continue to use their datapack presets instead.",
                "(It is however, possible to point custom egg chances to the built-in values via the loot condition, therefore using the config chance)",
                "THESE VALUES DO NOT TAKE EFFECT UNTIL `use_config_loot_values` IN `server.toml` IS SET TO `true` !!!")
                .push("chances");

        for (var target : BUILT_IN_CHANCES)
        {
            var entry = configurator
                    .comment(String.format("The chance that a %s egg appears in %s.", target.forBreed().getPath(), target.target().getPath()),
                    "0 = Never Appears, 1 = Guaranteed")
                    .defineInRange(target.forBreed().getPath() + "_egg_chances", target.chance(), 0, 1);

            // sort of hacky stuff going on here:
            // due to the fact that a breed can have multiple possible loot tables,
            // we have to figure out how to hash two RL's together to fake a "two key one value"
            // situation. So lets just mash the strings together and let existing hashing techniques take over for us.
            chances.put(target.forBreed() + target.target().toString(), entry);
        }
        CHANCES = chances.build();

        SPEC = configurator.pop().build();
    }

    // double-precision because floats look obnoxiously long in configs
    public record Target(ResourceLocation forBreed, ResourceLocation target, double chance) {}
}
