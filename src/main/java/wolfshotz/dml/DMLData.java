package wolfshotz.dml;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.ValidationTracker;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import wolfshotz.dml.egg.DragonEggType;
import wolfshotz.dml.egg.DragonSpawnEggItem;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DMLData
{
    @SubscribeEvent
    public static void runDataGen(GatherDataEvent evt)
    {
        DataGenerator gen = evt.getGenerator();

        if (evt.includeClient())
        {
            gen.addProvider(new ItemModelGen(gen, evt.getExistingFileHelper()));
            gen.addProvider(new BlockModelGen(gen, evt.getExistingFileHelper()));
        }

        if (evt.includeServer())
        {
            gen.addProvider(new LootTableGen(gen));
        }
    }

    // ===

    static class ItemModelGen extends ItemModelProvider
    {
        public ItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
        {
            super(generator, DragonMountsLegacy.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels()
        {
            DMLRegistry.ITEMS.getEntries()
                    .stream()
                    .map(RegistryObject::get)
                    .filter(DragonSpawnEggItem.class::isInstance)
                    .map(DragonSpawnEggItem.class::cast)
                    .forEach(i -> getBuilder(i.getRegistryName().getPath()).parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg")));

            DMLRegistry.BLOCKS.getEntries().forEach(block ->
            {
                String path = block.get().getRegistryName().getPath();
                getBuilder(path).parent(new ModelFile.UncheckedModelFile("dragonmounts:block/" + path));
            });
        }

        @Override
        public String getName() { return "Dragon Mounts Item Models"; }
    }

    static class BlockModelGen extends BlockStateProvider
    {
        public BlockModelGen(DataGenerator gen, ExistingFileHelper exFileHelper)
        {
            super(gen, DragonMountsLegacy.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels()
        {
            for (DragonEggType type : DragonEggType.INSTANCES)
            {
                Block eggBlock = type.getEggBlock();
                String path = eggBlock.getRegistryName().getPath();
                BlockModelBuilder model = models().withExistingParent(path, "dragon_egg");
                if (type.getBreed() != DMLRegistry.ENDER_DRAGON.get())
                    model.texture("all", "block/" + path).texture("particle", "block/" + path);
                simpleBlock(eggBlock, ConfiguredModel.builder().modelFile(model).build());
            }
        }
    }

    static class LootTableGen extends LootTableProvider
    {
        public LootTableGen(DataGenerator dataGeneratorIn) { super(dataGeneratorIn); }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
        {
            return ImmutableList.of(Pair.of(() -> new BlockLootTables()
            {
                @Override
                protected void addTables() { getKnownBlocks().forEach(this::registerDropSelfLootTable); }

                @Override
                protected Iterable<Block> getKnownBlocks() { return DMLRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet()); }

            }, LootParameterSets.BLOCK));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {}
    }
}
