package wolfshotz.dml;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import wolfshotz.dml.dragons.*;
import wolfshotz.dml.egg.DragonEggBlock;
import wolfshotz.dml.egg.DragonEggEntity;
import wolfshotz.dml.egg.DragonEggType;
import wolfshotz.dml.egg.DragonSpawnEggItem;

import java.util.function.ToIntFunction;

public class DMLRegistry
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, DragonMountsLegacy.MOD_ID);

    public static final RegistryObject<EntityType<TameableDragonEntity>> AETHER_DAGON = dragon("aether_dragon", of(AetherDragonEntity::new), AetherDragonEntity::getHabitatPoints, 0x11d6d0, 0xffff00);
    public static final RegistryObject<EntityType<TameableDragonEntity>> ENDER_DRAGON = dragon("ender_dragon", of(EndDragonEntity::new), EndDragonEntity::getHabitatPoints, 0x161616, 0xff63e8);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FIRE_DRAGON = dragon("fire_dragon", of(FireDragonEntity::new).immuneToFire(), FireDragonEntity::getHabitatPoints, 0x912400, 0xff9819);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FOREST_DRAGON = dragon("forest_dragon", of(ForestDragonEntity::new), ForestDragonEntity::getHabitatPoints, 0x054a00, 0x0a9600);
    public static final RegistryObject<EntityType<TameableDragonEntity>> GHOST_DRAGON = dragon("ghost_dragon", of(GhostDragonEntity::new), GhostDragonEntity::getHabitatPoints, 0xc4c4c4, 0xc2f8ff);
    public static final RegistryObject<EntityType<TameableDragonEntity>> ICE_DRAGON = dragon("ice_dragon", of(IceDragonEntity::new), IceDragonEntity::getHabitatPoints, 0xffffff, 0x00b7ff);
    public static final RegistryObject<EntityType<TameableDragonEntity>> NETHER_DRAGON = dragon("nether_dragon", of(NetherDragonEntity::new).immuneToFire(), NetherDragonEntity::getHabitatPoints, 0x912400, 0x2e0b00);
    public static final RegistryObject<EntityType<TameableDragonEntity>> WATER_DRAGON = dragon("water_dragon", of(WaterDragonEntity::new), WaterDragonEntity::getHabitatPoints, 0x0062ff, 0x5999ff);

    public static final RegistryObject<EntityType<DragonEggEntity>> EGG = ENTITIES.register("egg", () -> EntityType.Builder.<DragonEggEntity>create(DragonEggEntity::new, EntityClassification.MISC).size(DragonEggEntity.WIDTH, DragonEggEntity.HEIGHT).setShouldReceiveVelocityUpdates(true).setUpdateInterval(20).setTrackingRange(10).disableSummoning().immuneToFire().build(DragonMountsLegacy.MOD_ID + ":egg"));
    public static final RegistryObject<SoundEvent> DRAGON_BREATHE = sound("dragon.breathe");
    public static final RegistryObject<SoundEvent> DRAGON_STEP = sound("dragon.step");

    // == Sounds ==
    public static final RegistryObject<SoundEvent> DRAGON_DEATH = sound("dragon.death");

    public static RegistryObject<EntityType<TameableDragonEntity>> dragon(String name, EntityType.Builder<TameableDragonEntity> builder, ToIntFunction<DragonEggEntity> habitatFunc, int primColor, int secColor)
    {
        RegistryObject<EntityType<TameableDragonEntity>> type = ENTITIES.register(name, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
        RegistryObject<Block> eggBlock = BLOCKS.register(name + "_egg", () -> new DragonEggBlock(type));
        DragonEggType eggType = new DragonEggType(type, eggBlock, habitatFunc, primColor, secColor);
        ITEMS.register(name + "_egg", () -> new BlockItem(eggBlock.get(), new Item.Properties().group(ItemGroup.MISC)));
        ITEMS.register(name + "_spawn_egg", () -> new DragonSpawnEggItem(eggType));
        return type;
    }

    private static EntityType.Builder<TameableDragonEntity> of(EntityType.IFactory<TameableDragonEntity> factory) { return EntityType.Builder.create(factory, EntityClassification.CREATURE).setShouldReceiveVelocityUpdates(true).setUpdateInterval(3).setTrackingRange(80).size(TameableDragonEntity.BASE_WIDTH, TameableDragonEntity.BASE_HEIGHT); }

    private static RegistryObject<SoundEvent> sound(String name)
    {
        String registryName = "entity." + name;
        return SOUNDS.register(registryName, () -> new SoundEvent(DragonMountsLegacy.rl(registryName)));
    }
}
