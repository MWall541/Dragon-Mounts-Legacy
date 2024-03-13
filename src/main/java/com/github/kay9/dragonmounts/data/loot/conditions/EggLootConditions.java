package com.github.kay9.dragonmounts.data.loot.conditions;

import com.github.kay9.dragonmounts.config.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.config.EggLootConfig;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class EggLootConditions implements LootItemCondition
{
    private final ResourceLocation breedID;
    private final float dataProbability;

    EggLootConditions(ResourceLocation forBreed, float dataProbability)
    {
        this.breedID = forBreed;
        this.dataProbability = dataProbability;
    }

    // typically uses a builder but... why. we'll just do this instead...
    public static EggLootConditions create(ResourceLocation forBreed, float chance)
    {
        return new EggLootConditions(forBreed, chance);
    }

    @Override
    public LootItemConditionType getType()
    {
        return DMLRegistry.EGG_LOOT_CONDITION;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        if (!DMLConfig.useLootTables()) return false;

        var probability = DMLConfig.useConfigLootValues()? EggLootConfig.getProbabilityFor(breedID, lootContext.getQueriedLootTableId()) : dataProbability;
        if (probability < 0) probability = dataProbability; // not a built-in breed type, default to data preset

        return lootContext.getRandom().nextFloat() < probability;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EggLootConditions>
    {
        @Override
        public void serialize(JsonObject json, EggLootConditions value, JsonSerializationContext context)
        {
            json.addProperty("config_chance_target", value.breedID.toString());
            json.addProperty("chance", value.dataProbability);
        }

        @Override
        public EggLootConditions deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return EggLootConditions.create(new ResourceLocation(GsonHelper.getAsString(json, "config_chance_target")), GsonHelper.getAsFloat(json, "chance"));
        }
    }
}
