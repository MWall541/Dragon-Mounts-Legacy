package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonEggLootMod extends LootModifier
{
    private final DragonBreed breed;

    public DragonEggLootMod(LootItemCondition[] conditions, DragonBreed breed)
    {
        super(conditions);
        this.breed = breed;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        if (DMLConfig.useLootTables()) generatedLoot.add(DMLEggBlock.Item.create(breed, DragonEgg.DEFAULT_HATCH_TIME));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<DragonEggLootMod>
    {
        @Override
        public DragonEggLootMod read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions)
        {
            return new DragonEggLootMod(conditions, BreedManager.read(GsonHelper.getAsString(object, "breed")));
        }

        @Override
        public JsonObject write(DragonEggLootMod instance)
        {
            var json = makeConditions(instance.conditions);
            json.addProperty("breed", instance.breed.id().toString());
            return json;
        }
    }
}
