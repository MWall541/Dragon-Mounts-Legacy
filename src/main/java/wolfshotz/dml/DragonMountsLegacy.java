package wolfshotz.dml;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.client.ClientEvents;
import wolfshotz.dml.cmd.DragonSetAgeCommand;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.util.network.NetworkUtils;

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

    // registry
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MOD_ID);

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addGenericListener(GlobalLootModifierSerializer.class, this::lootModifiers);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
        {
            bus.addListener(ClientEvents::clientSetup);
            bus.addListener(ClientEvents::itemColors);
        });

        DragonEggBlock.register(bus);
        DMLEntities.ENTITIES.register(bus);
        DMLSounds.SOUNDS.register(bus);
        ITEMS.register(bus);

        MinecraftForge.EVENT_BUS.addListener(this::startingServer);
    }

    public void commonSetup(FMLCommonSetupEvent evt) { NetworkUtils.registerPackets(); }

    public void startingServer(FMLServerStartingEvent evt)
    {
        evt.getCommandDispatcher().register(
                LiteralArgumentBuilder.<CommandSource>literal("dragon")
                        .then(DragonSetAgeCommand.register())
        );
    }

    public void lootModifiers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> evt)
    {
        evt.getRegistry().register(new DungeonLootModifier.Serializer().setRegistryName(rl("dungeon_loot")));
    }

    public static ResourceLocation rl(String path) { return new ResourceLocation(MOD_ID, path); }
}
