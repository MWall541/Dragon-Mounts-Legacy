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

public class RandomChanceByConfigOrPreset implements LootItemCondition
{
    private final ResourceLocation configTargetID;
    private final float presetProbability;

    RandomChanceByConfigOrPreset(ResourceLocation forBreed, float dataProbability)
    {
        this.configTargetID = forBreed;
        this.presetProbability = dataProbability;
    }

    // typically uses a builder but... why. we'll just do this instead...
    public static RandomChanceByConfigOrPreset create(ResourceLocation forBreed, float chance)
    {
        return new RandomChanceByConfigOrPreset(forBreed, chance);
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

        var probability = DMLConfig.useConfigLootValues()? EggLootConfig.getProbabilityFor(configTargetID, lootContext.getQueriedLootTableId()) : presetProbability;
        if (probability < 0) probability = presetProbability; // not a built-in breed type, default to data preset

        return lootContext.getRandom().nextFloat() < probability;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<RandomChanceByConfigOrPreset>
    {
        @Override
        public void serialize(JsonObject json, RandomChanceByConfigOrPreset value, JsonSerializationContext context)
        {
            json.addProperty("config_chance_target", value.configTargetID.toString());
            json.addProperty("preset_chance", value.presetProbability);
        }

        @Override
        public RandomChanceByConfigOrPreset deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return RandomChanceByConfigOrPreset.create(new ResourceLocation(GsonHelper.getAsString(json, "config_chance_target")), GsonHelper.getAsFloat(json, "preset_chance"));
        }
    }
}
