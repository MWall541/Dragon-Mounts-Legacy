package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.kay9.dragonmounts.dragon.breed.DragonBreed.BuiltIn.*;

public class DragonEggLootMod extends LootModifier
{
    public static Target[] BUILT_IN_CHANCES = new Target[]{
            new Target(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.2),
            new Target(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.085),
            new Target(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3),
            new Target(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2),
            new Target(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.075),
            new Target(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2),
            new Target(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35),
            new Target(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175)
    };

    private final ResourceLocation id;

    public DragonEggLootMod(LootItemCondition[] conditions, ResourceLocation breed)
    {
        super(conditions);
        this.id = breed;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        var reg = context.getLevel().registryAccess();
        var breed = BreedRegistry.registry(reg).get(id);
        if (breed != null)
            generatedLoot.add(DMLEggBlock.Item.create(breed, reg, breed.hatchTime()));
        else
            DragonMountsLegacy.LOG.error("Attempted to add a dragon egg to loot with unknown breed id: \"{}\"", id);

        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<DragonEggLootMod>
    {
        @Override
        public DragonEggLootMod read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions)
        {
            var in = GsonHelper.getAsString(object, "egg_breed");
            return new DragonEggLootMod(conditions, new ResourceLocation(in));
        }

        @Override
        public JsonObject write(DragonEggLootMod instance)
        {
            var json = makeConditions(instance.conditions);
            json.addProperty("egg_breed", instance.id.toString());
            return json;
        }
    }

    public record Target(ResourceLocation forBreed, ResourceLocation target, double chance) {}
}
