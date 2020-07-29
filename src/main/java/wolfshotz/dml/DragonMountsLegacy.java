package wolfshotz.dml;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wolfshotz.dml.client.ClientEvents;
import wolfshotz.dml.cmd.DragonSetAgeCommand;
import wolfshotz.dml.data.DataHandler;

@Mod(DragonMountsLegacy.MOD_ID)
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(DataHandler::gather);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEvents::init);

        MinecraftForge.EVENT_BUS.addListener(this::startingServer);
        MinecraftForge.EVENT_BUS.addListener(this::injectLootTables);
        MinecraftForge.EVENT_BUS.addListener(DragonEggBlock::onVanillaEggActivate);

        DMLRegistry.BLOCKS.register(bus);
        DMLRegistry.ITEMS.register(bus);
        DMLRegistry.ENTITIES.register(bus);
        DMLRegistry.SOUNDS.register(bus);
    }

    public void startingServer(FMLServerStartingEvent evt)
    {
        evt.getCommandDispatcher().register(
                LiteralArgumentBuilder.<CommandSource>literal("dragonmounts")
                        .then(DragonSetAgeCommand.register()));
    }

    public void injectLootTables(LootTableLoadEvent evt)
    {
        ResourceLocation name = evt.getName();
        ResourceLocation path = rl(String.format("injects/%s/%s", name.getNamespace(), name.getPath()));
        evt.getTable().addPool(LootPool.builder().name("dragonmounts_injects").addEntry(TableLootEntry.builder(path).weight(1)).build());
    }

    public static ResourceLocation rl(String path) { return new ResourceLocation(MOD_ID, path); }
}
