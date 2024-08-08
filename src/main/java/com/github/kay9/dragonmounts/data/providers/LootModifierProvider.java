package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

class LootModifierProvider extends GlobalLootModifierProvider
{
    LootModifierProvider(PackOutput output, String modid, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, modid, registries);
    }

    @Override
    protected void start(HolderLookup.Provider provider)
    {
        for (var target : DragonEggLootMod.BUILT_IN_CHANCES)
            addWithConfigChance(target.forBreed(), target.target());
    }

    private void addWithConfigChance(ResourceKey<DragonBreed> breed, ResourceKey<LootTable> table)
    {
        var conditions = new LootItemCondition[]{
                LootTableIdCondition.builder(table.location()).build(),
                new RandomChanceByConfig(DMLConfig.formatEggTargetAsPath(breed, table)) // Automatically formats the given inputs to point the chance values to the corresponding config entry
        };

        var path = String.join("/", breed.location().getNamespace(), breed.location().getPath(), table.location().getPath());
        super.add(path, new DragonEggLootMod(conditions, breed, false));
    }
}
