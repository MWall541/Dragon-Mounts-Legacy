package com.github.kay9.dragonmounts.data.loot.conditions;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class RandomChanceByConfig implements LootItemCondition
{
    private final String configTargetID;

    public RandomChanceByConfig(String forTarget)
    {
        this.configTargetID = forTarget;
    }

    @Override
    public LootItemConditionType getType()
    {
        return DMLRegistry.RANDOM_CHANCE_CONFIG_CONDITION;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        if (!DMLConfig.useLootTables()) return false;

        // non-existing config targets fail silently with probability -1f
        return lootContext.getRandom().nextFloat() < DMLConfig.getEggChanceFor(configTargetID);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<RandomChanceByConfig>
    {
        @Override
        public void serialize(JsonObject json, RandomChanceByConfig value, JsonSerializationContext context)
        {
            json.addProperty("config_chance_target", value.configTargetID);
        }

        @Override
        public RandomChanceByConfig deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return new RandomChanceByConfig(GsonHelper.getAsString(json, "config_chance_target"));
        }
    }
}
