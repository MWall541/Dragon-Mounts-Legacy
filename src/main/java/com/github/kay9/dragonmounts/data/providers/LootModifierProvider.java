package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

class LootModifierProvider extends GlobalLootModifierProvider
{
    LootModifierProvider(DataGenerator gen, String modid)
    {
        super(gen, modid);
    }

    @Override
    protected void start()
    {
        for (var target : DragonEggLootMod.BUILT_IN_CHANCES)
            addWithConfigChance(target.forBreed().location(), target.target());
    }

    protected void add(ResourceLocation breed, ResourceLocation table, float chance)
    {
        var conditions = new LootItemCondition[]{
                LootTableIdCondition.builder(table).build(),
                LootItemRandomChanceCondition.randomChance(chance).build()
        };

        var path = String.join("/", breed.getNamespace(), breed.getPath(), table.getPath());
        super.add(path, DMLRegistry.EGG_LOOT_MODIFIER.get(), new DragonEggLootMod(conditions, breed, false));
    }

    private void addWithConfigChance(ResourceLocation breed, ResourceLocation table)
    {
        var conditions = new LootItemCondition[]{
                LootTableIdCondition.builder(table).build(),
                new RandomChanceByConfig(DMLConfig.formatEggTargetAsPath(breed, table)) // Automatically formats the given inputs to point the chance values to the corresponding config entry
        };

        var path = String.join("/", breed.getNamespace(), breed.getPath(), table.getPath());
        super.add(path, DMLRegistry.EGG_LOOT_MODIFIER.get(), new DragonEggLootMod(conditions, breed, false));
    }
}
