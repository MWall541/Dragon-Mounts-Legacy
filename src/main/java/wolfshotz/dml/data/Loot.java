package wolfshotz.dml.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import wolfshotz.dml.DMLRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Loot extends LootTableProvider
{
    // taken from LootTableManager
    private static final Gson GSON = LootSerializers.func_237387_b_().setPrettyPrinting().create();
    private final DataGenerator dataGen;
    private DirectoryCache cache;

    public Loot(DataGenerator dataGeneratorIn)
    {
        super(dataGeneratorIn);
        this.dataGen = dataGeneratorIn;
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
    {
        return ImmutableList.of(Pair.of(() -> new BlockLootTables()
        {
            @Override
            protected void addTables() { getKnownBlocks().forEach(this::registerDropSelfLootTable); }

            @Override
            protected Iterable<Block> getKnownBlocks() { return DMLRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet()); }
        }, LootParameterSets.CHEST));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {}

    @Override
    public void act(DirectoryCache cache)
    {
        super.act(cache);
        this.cache = cache; // for convenience
        addInjections();
    }

    public void addInjections()
    {
        // weights add up to 100... I think....
        inject(LootTables.CHESTS_END_CITY_TREASURE, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.ENDER_EGG_BLOCK.get()).weight(5))
                .addEntry(EmptyLootEntry.func_216167_a().weight(95))
                .build());

        inject(LootTables.CHESTS_WOODLAND_MANSION, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.GHOST_EGG_BLOCK.get()).weight(13))
                .addEntry(EmptyLootEntry.func_216167_a().weight(87))
                .build());

        inject(LootTables.CHESTS_JUNGLE_TEMPLE, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.FOREST_EGG_BLOCK.get()).weight(25))
                .addEntry(EmptyLootEntry.func_216167_a().weight(75))
                .build());

        inject(LootTables.CHESTS_DESERT_PYRAMID, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.FIRE_EGG_BLOCK.get()).weight(13))
                .addEntry(EmptyLootEntry.func_216167_a().weight(87))
                .build());

        inject(LootTables.CHESTS_IGLOO_CHEST, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.ICE_EGG_BLOCK.get()).weight(20))
                .addEntry(EmptyLootEntry.func_216167_a().weight(80))
                .build());

        inject(LootTables.CHESTS_NETHER_BRIDGE, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.NETHER_EGG_BLOCK.get()).weight(8))
                .addEntry(EmptyLootEntry.func_216167_a().weight(92))
                .build());

        inject(LootTables.CHESTS_UNDERWATER_RUIN_BIG, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.WATER_EGG_BLOCK.get()).weight(12))
                .addEntry(EmptyLootEntry.func_216167_a().weight(88))
                .build());

        inject(LootTables.CHESTS_BURIED_TREASURE, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.WATER_EGG_BLOCK.get()).weight(50))
                .addEntry(EmptyLootEntry.func_216167_a().weight(50))
                .build());

        inject(LootTables.CHESTS_SIMPLE_DUNGEON, LootPool.builder()
                .addEntry(ItemLootEntry.builder(DMLRegistry.AETHER_EGG_BLOCK.get()).weight(40))
                .addEntry(EmptyLootEntry.func_216167_a().weight(60))
                .build());
    }

    public void inject(ResourceLocation table, LootPool pool)
    {
        try
        {
            JsonObject object = new JsonObject();
            JsonArray array = new JsonArray();
            array.add(GSON.toJsonTree(pool));
            object.add("pools", array);
            IDataProvider.save(GSON,
                    cache,
                    object,
                    dataGen.getOutputFolder().resolve(String.format("data/dragonmounts/loot_tables/injects/%s/%s.json", table.getNamespace(), table.getPath())));
        }
        catch (IOException e) { throw new RuntimeException("Could not save table injection: " + table); }
    }
}
