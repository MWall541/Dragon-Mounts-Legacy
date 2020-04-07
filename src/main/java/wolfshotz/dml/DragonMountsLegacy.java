package wolfshotz.dml;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.client.ClientEvents;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.util.network.NetworkUtils;

@Mod(DragonMountsLegacy.MOD_ID)
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final String PROTOCOL_VER = "1.0";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(rl("network"), () -> PROTOCOL_VER, PROTOCOL_VER::equals, PROTOCOL_VER::equals);
    public static final Logger L = LogManager.getLogger(MOD_ID);

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(DragonMountsLegacy::clientSetup);
        bus.addListener(DragonMountsLegacy::commonSetup);

        DragonEggBlock.register(bus);
        DMLEntities.ENTITIES.register(bus);
    }

    public static void commonSetup(FMLCommonSetupEvent evt)
    {
        NetworkUtils.registerPackets();
    }

    public static void clientSetup(FMLClientSetupEvent evt)
    {
        ClientEvents.registerRenders();
    }

    public static ResourceLocation rl(String path) { return new ResourceLocation(MOD_ID, path); }
}
