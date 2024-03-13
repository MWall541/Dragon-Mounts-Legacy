package com.github.kay9.dragonmounts.config;

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
    public static final Target[] BUILT_IN_CHANCES = new Target[]{
            new Target(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.2f),
            new Target(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.085f),
            new Target(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3f),
            new Target(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2f),
            new Target(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.075f),
            new Target(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2f),
            new Target(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35f),
            new Target(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175f)
    };

    private static final Map<TargetKey, ForgeConfigSpec.DoubleValue> CHANCES;

    public static final ForgeConfigSpec SPEC;

    public static float getProbabilityFor(ResourceLocation breed, ResourceLocation target)
    {
        var chance = CHANCES.get(breed);
        if (chance.isEmpty()) return -1f;
        for (var value : chance)


        return -1f;
    }

    static // loot chances
    {
        var configurator = new ForgeConfigSpec.Builder();
        var chances = ImmutableMultimap.<ResourceLocation, ForgeConfigSpec.DoubleValue>builder();

        for (var target : BUILT_IN_CHANCES)
        {
            var entry = configurator
                    .comment(String.format("The chance that a %s egg appears in %s.", target.forBreed().getPath(), target.target()))
                    .comment("0 = Never Appears, 1 = Guaranteed")
                    .defineInRange(target.forBreed().getPath() + "_egg_chances", target.chance(), 0, 1);

            chances.put(target.forBreed(), entry);
        }
        CHANCES = chances.build();

        SPEC = configurator.build();
    }

    public record Target(ResourceLocation forBreed, ResourceLocation target, float chance) {}
    public record TargetKey(ResourceLocation forBreed, ResourceLocation target) {}
}
