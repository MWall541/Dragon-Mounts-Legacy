package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import static com.github.kay9.dragonmounts.data.providers.DragonBreedProvider.*;

class LootModifierProvider extends GlobalLootModifierProvider
{
    LootModifierProvider(PackOutput output, String modid)
    {
        super(output, modid);
    }

    @Override
    protected void start()
    {
        add(AETHER.location(), BuiltInLootTables.SIMPLE_DUNGEON, 0.2f);
        add(FIRE.location(), BuiltInLootTables.DESERT_PYRAMID, 0.085f);
        add(FOREST.location(), BuiltInLootTables.JUNGLE_TEMPLE, 0.3f);
        add(GHOST.location(), BuiltInLootTables.WOODLAND_MANSION, 0.2f);
        add(GHOST.location(), BuiltInLootTables.ABANDONED_MINESHAFT, 0.075f);
        add(ICE.location(), BuiltInLootTables.IGLOO_CHEST, 0.2f);
        add(NETHER.location(), BuiltInLootTables.BASTION_TREASURE, 0.35f);
        add(WATER.location(), BuiltInLootTables.BURIED_TREASURE, 0.175f);
    }

    protected void add(ResourceLocation breed, ResourceLocation table, float chance)
    {
        var path = breed.getNamespace() + "/" + breed.getPath() + "/" + table.getPath();

        var conditions = new LootItemCondition[] {
                LootTableIdCondition.builder(table).build(),
                LootItemRandomChanceCondition.randomChance(chance).build(),
        };

        super.add(path, new DragonEggLootMod(conditions, breed));
    }
}
