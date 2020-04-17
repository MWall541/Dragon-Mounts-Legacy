package wolfshotz.dml.util.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.functions.SetCount;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableGen extends LootTableProvider
{
    public LootTableGen(DataGenerator dataGeneratorIn)
    {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
    {
        return ImmutableList.of(
                Pair.of(BlockLoot::new, LootParameterSets.BLOCK)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {}

    public static class BlockLoot extends BlockLootTables
    {
        @Override
        protected void addTables()
        {
            Block egg = DragonEggBlock.INSTANCE;

            registerLootTable(egg, block ->
            {
                return LootTable.builder().addLootPool(LootPool.builder()
                        .rolls(ConstantRange.of(1))
                        .addEntry(withExplosionDecay(egg, ItemLootEntry.builder(block).acceptFunction(getBlockStateBuilder(block, SetCount.builder(ConstantRange.of(1)))
                                )
                                )
                        )
                );
            });
        }

        public SetCount.Builder<?> getBlockStateBuilder(Block block, SetCount.Builder<?> builder)
        {
            for (EnumEggTypes type : EnumEggTypes.VALUES)
            {
                builder.acceptCondition(BlockStateProperty.builder(block)
                        .fromProperties(
                                StatePropertiesPredicate.Builder
                                        .newBuilder()
                                        .withProp(DragonEggBlock.BREED, type)
                        ));
            }

            return builder;
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ImmutableList.of(DragonEggBlock.INSTANCE);
        }
    }
}