package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

public class DragonEggLootMod
{
    public record Target(ResourceKey<DragonBreed> forBreed, ResourceKey<LootTable> target, double chance) {}

    public static final Target[] BUILT_IN_CHANCES = new Target[]{
            new Target(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.15),
            new Target(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.075),
            new Target(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3),
            new Target(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2),
            new Target(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.095),
            new Target(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2),
            new Target(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35),
            new Target(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175)
    };

    public static void register()
    {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, wrapperLookup) ->
        {
            if (!DMLConfig.useLootTables()) return;

            for (var target : BUILT_IN_CHANCES)
            {
                if (!target.target().equals(key)) continue;

                double chance = DMLConfig.getEggChanceFor(
                        DMLConfig.formatEggTargetAsPath(target.forBreed(), target.target()));
                if (chance <= 0) continue;

                try
                {
                    var breedLookup = wrapperLookup.lookupOrThrow(DragonBreed.REGISTRY_KEY);
                    var breedHolder = breedLookup.getOrThrow(target.forBreed());

                    tableBuilder.withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .when(LootItemRandomChanceCondition.randomChance((float) chance))
                            .add(LootItem.lootTableItem(DMLRegistry.EGG_BLOCK_ITEM)
                                    .apply(SetComponentsFunction
                                            .setComponent(DMLRegistry.DRAGON_BREED_COMPONENT, breedHolder))));
                }
                catch (Exception e)
                {
                    // breed not found in registry — skip silently
                }
            }
        });
    }
}
