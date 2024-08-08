package com.github.kay9.dragonmounts.data.loot.conditions;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class RandomChanceByConfig implements LootItemCondition
{
    public static final MapCodec<RandomChanceByConfig> CODEC = Codec.STRING.xmap(RandomChanceByConfig::new, t -> t.configTargetID)
            .fieldOf("config_chance_target");

    private final String configTargetID;

    public RandomChanceByConfig(String forTarget)
    {
        this.configTargetID = forTarget;
    }

    @Override
    public LootItemConditionType getType()
    {
        return DMLRegistry.RANDOM_CHANCE_CONFIG_CONDITION.get();
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        if (!DMLConfig.useLootTables()) return false;

        // non-existing config targets fail silently with probability -1f
        return lootContext.getRandom().nextFloat() < DMLConfig.getEggChanceFor(configTargetID);
    }
}
