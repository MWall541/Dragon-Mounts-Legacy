package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.config.EggLootConfig;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfigOrPreset;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
        for (var target : EggLootConfig.BUILT_IN_CHANCES)
            add(target.forBreed(), target.target(), (float) target.chance());
    }

    protected void add(ResourceLocation breed, ResourceLocation table, float chance)
    {
        // todo: change path to something like: "aether_in_simple_dungeon"
        var path = breed.getNamespace() + "/" + breed.getPath() + "/" + table.getPath();

        var conditions = new LootItemCondition[]{
                LootTableIdCondition.builder(table).build(),
                RandomChanceByConfigOrPreset.create(breed, chance)
        };

        super.add(path, DMLRegistry.EGG_LOOT_MODIFIER.get(), new DragonEggLootMod(conditions, breed));
    }
}
