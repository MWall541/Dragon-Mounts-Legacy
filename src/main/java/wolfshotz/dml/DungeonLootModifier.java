package wolfshotz.dml;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class DungeonLootModifier extends LootModifier
{
    private final ResourceLocation lootTable;
    boolean reentryPrevention = false;

    public DungeonLootModifier(ILootCondition[] conditionsIn, ResourceLocation lootTable)
    {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Nonnull
    @Override
    public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        if (reentryPrevention) return generatedLoot;

        reentryPrevention = true;
        LootTable lootTable = context.func_227502_a_(this.lootTable);
        List<ItemStack> extras = lootTable.generate(context);
        generatedLoot.addAll(extras);
        reentryPrevention = false;

        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<DungeonLootModifier>
    {
        @Override
        public DungeonLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition)
        {
            return new DungeonLootModifier(ailootcondition, new ResourceLocation(JSONUtils.getString(object, "add_loot")));
        }
    }
}
