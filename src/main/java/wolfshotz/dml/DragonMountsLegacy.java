package wolfshotz.dml;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wolfshotz.dml.client.ClientEvents;
import wolfshotz.dml.cmd.DragonSetAgeCommand;
import wolfshotz.dml.dragons.TameableDragonEntity;
import wolfshotz.dml.egg.DragonEggType;
import wolfshotz.dml.network.EggHatchPacket;

import java.util.Optional;

@Mod(DragonMountsLegacy.MOD_ID)
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger L = LogManager.getLogger(MOD_ID);
    public static final String PROTOCOL_VER = "1.0";
    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(rl("network"))
            .clientAcceptedVersions(PROTOCOL_VER::equals)
            .serverAcceptedVersions(PROTOCOL_VER::equals)
            .networkProtocolVersion(() -> PROTOCOL_VER)
            .simpleChannel();

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
        {
            bus.addListener(ClientEvents::clientSetup);
            bus.addListener(ClientEvents::itemColors);
        });

        MinecraftForge.EVENT_BUS.addListener(this::startingServer);
        MinecraftForge.EVENT_BUS.addListener(this::loadLootTables);

        DMLRegistry.BLOCKS.register(bus);
        DMLRegistry.ITEMS.register(bus);
        DMLRegistry.ENTITIES.register(bus);
        DMLRegistry.SOUNDS.register(bus);
    }

    public void commonSetup(FMLCommonSetupEvent evt)
    {
        int index = 0;
        DragonMountsLegacy.NETWORK.registerMessage(++index, EggHatchPacket.class, EggHatchPacket::encode, EggHatchPacket::new, EggHatchPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void startingServer(FMLServerStartingEvent evt)
    {
        evt.getCommandDispatcher().register(
                LiteralArgumentBuilder.<CommandSource>literal("dragon")
                        .then(DragonSetAgeCommand.register())
        );
    }

    public void loadLootTables(LootTableLoadEvent evt)
    {
        ResourceLocation table = evt.getName();
        if (table == LootTables.CHESTS_END_CITY_TREASURE)
            makePool(evt.getTable(), 0.7f, DMLRegistry.ENDER_DRAGON.get());
        else if (table == LootTables.CHESTS_WOODLAND_MANSION)
            makePool(evt.getTable(), 0.7f, DMLRegistry.GHOST_DRAGON.get());
        else if (table == LootTables.CHESTS_JUNGLE_TEMPLE)
            makePool(evt.getTable(), 0.8f, DMLRegistry.FOREST_DRAGON.get());
        else if (table == LootTables.CHESTS_DESERT_PYRAMID)
            makePool(evt.getTable(), 0.15f, DMLRegistry.FIRE_DRAGON.get());
        else if (table == LootTables.CHESTS_IGLOO_CHEST) makePool(evt.getTable(), 0.5f, DMLRegistry.ICE_DRAGON.get());
        else if (table == LootTables.CHESTS_NETHER_BRIDGE)
            makePool(evt.getTable(), 0.15f, DMLRegistry.NETHER_DRAGON.get());
        else if (table == LootTables.CHESTS_UNDERWATER_RUIN_BIG)
            makePool(evt.getTable(), 0.4f, DMLRegistry.WATER_DRAGON.get());
        else if (table == LootTables.CHESTS_BURIED_TREASURE)
            makePool(evt.getTable(), 0.9f, DMLRegistry.WATER_DRAGON.get());
        else if (table == LootTables.CHESTS_SIMPLE_DUNGEON)
            makePool(evt.getTable(), 0.25f, DMLRegistry.AETHER_DAGON.get());
    }

    private static void makePool(LootTable table, float chance, EntityType<? extends TameableDragonEntity> type)
    {
        table.addPool(LootPool.builder()
                .name("dragonmounts_added_eggs")
                .rolls(ConstantRange.of(1))
                .acceptCondition(RandomChance.builder(chance))
                .addEntry(ItemLootEntry.builder(DragonEggType.lookUp(type).getEggBlock()))
                .build());
    }

    public static ResourceLocation rl(String path) { return new ResourceLocation(MOD_ID, path); }
}
