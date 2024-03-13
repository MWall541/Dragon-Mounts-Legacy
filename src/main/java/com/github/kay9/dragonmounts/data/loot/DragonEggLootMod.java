package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
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
            var in = GsonHelper.getAsString(object, "breed");
            return new DragonEggLootMod(conditions, new ResourceLocation(in));
        }

        @Override
        public JsonObject write(DragonEggLootMod instance)
        {
            var json = makeConditions(instance.conditions);
            json.addProperty("breed", instance.id.toString());
            return json;
        }
    }
}
