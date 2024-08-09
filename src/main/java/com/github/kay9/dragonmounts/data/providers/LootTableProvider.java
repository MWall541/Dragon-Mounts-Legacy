package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends net.minecraft.data.loot.LootTableProvider
{
    public LootTableProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(pOutput, Set.of(), ImmutableList.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    @Override
    protected void validate(Registry<LootTable> map, ValidationContext validationcontext, ProblemReporter report)
    {
    }

    private static class Blocks extends BlockLootSubProvider
    {
        public Blocks(HolderLookup.Provider registry)
        {
            super(Set.of(DMLRegistry.EGG_BLOCK.get().asItem()), FeatureFlags.REGISTRY.allFlags(), registry);
        }

        @Override
        protected void generate()
        {
            add(DMLRegistry.EGG_BLOCK.get(), b -> LootTable.lootTable()
                    .withPool(applyExplosionCondition(b, LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(b)
                                    .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                            .include(DMLRegistry.DRAGON_BREED_COMPONENT.get())
                                            .include(DataComponents.CUSTOM_NAME))
                                    .apply(CopyBlockState.copyState(b)
                                            .copy(HatchableEggBlock.HATCH_STAGE)
                                    )
                            )
                    ))
            );
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ImmutableList.of(DMLRegistry.EGG_BLOCK.get());
        }
    }
}
