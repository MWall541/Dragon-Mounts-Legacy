package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import static com.github.kay9.dragonmounts.data.DragonBreedProvider.*;

public class LootModifierProvider extends GlobalLootModifierProvider
{
    public LootModifierProvider(DataGenerator gen)
    {
        super(gen, DragonMountsLegacy.MOD_ID);
    }

    @Override
    protected void start()
    {
        add(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.2f);
        add(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.085f);
        add(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3f);
        add(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2f);
        add(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.075f);
        add(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2f);
        add(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35f);
        add(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175f);
    }

    public void add(DragonBreed breed, ResourceLocation table, float chance)
    {
        // todo: change path to something like: "aether_in_simple_dungeon"
        var path = breed.id().getNamespace() + "/" + breed.id().getPath() + "/" + table.getPath();

        var conditions = new LootItemCondition[] {
                LootTableIdCondition.builder(table).build(),
                LootItemRandomChanceCondition.randomChance(chance).build()
        };

        super.add(path, new DragonEggLootMod(conditions, breed));
    }
}
