package wolfshotz.dml;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.client.ClientEvents;
import wolfshotz.dml.entity.dragons.EndDragonEntity;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.util.network.NetworkUtils;

@Mod(DragonMountsLegacy.MOD_ID)
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";

    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, MOD_ID);
    public static final RegistryObject<EntityType<EndDragonEntity>> END_DRAGON = ENTITIES.register("ender_dragon", () -> EntityType.Builder.create(EndDragonEntity::new, EntityClassification.CREATURE).size(TameableDragonEntity.BASE_WIDTH, TameableDragonEntity.BASE_HEIGHT).build(MOD_ID + ":ender_dragon"));

    @ObjectHolder(MOD_ID + ":dragon_egg")
    public static final Block DRAGON_EGG = null;

    public static final String PROTOCOL_VER = "1.0";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(rl("network"), () -> PROTOCOL_VER, PROTOCOL_VER::equals, PROTOCOL_VER::equals);

    public DragonMountsLegacy()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(DragonMountsLegacy::clientSetup);
        bus.addListener(DragonMountsLegacy::commonSetup);

        registerObject(bus, Block.class, new DragonEggBlock(Block.Properties.from(Blocks.DRAGON_EGG)), "dragon_egg");
        registerObject(bus, Item.class, new BlockItem(DRAGON_EGG, new Item.Properties()), "dragon_egg");

        ENTITIES.register(bus);
    }

    public static void commonSetup(FMLCommonSetupEvent evt)
    {
        NetworkUtils.registerPackets();
    }

    public static void clientSetup(FMLClientSetupEvent evt)
    {
        ClientEvents.registerRenders();
    }

    public static <F extends ForgeRegistryEntry<F>> void registerObject(IEventBus bus, Class<F> type, F object, String name)
    {
        bus.addGenericListener(type, (RegistryEvent.Register<F> e) -> e.getRegistry().register(object.setRegistryName(name)));
    }

    public static ResourceLocation rl(String path) { return new ResourceLocation(MOD_ID, path); }
}
