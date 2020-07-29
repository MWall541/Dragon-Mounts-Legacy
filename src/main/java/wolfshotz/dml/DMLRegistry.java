package wolfshotz.dml;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import wolfshotz.dml.entities.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class DMLRegistry
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DragonMountsLegacy.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DragonMountsLegacy.MOD_ID);

    public static final RegistryObject<EntityType<TameableDragonEntity>> AETHER_DRAGON_ENTITY = dragonEntity("aether_dragon", AetherDragonEntity::new, AetherDragonEntity::getHabitatPoints, 0x11d6d0, 0xffff00);
    public static final RegistryObject<EntityType<TameableDragonEntity>> ENDER_DRAGON_ENTITY = dragonEntity("ender_dragon", EndDragonEntity::new, EndDragonEntity::getHabitatPoints, 0x161616, 0xff63e8);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FIRE_DRAGON_ENTITY = dragonEntity("fire_dragon", FireDragonEntity::new, FireDragonEntity::getHabitatPoints, 0x912400, 0xff9819, EntityType.Builder::immuneToFire);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FOREST_DRAGON_ENTITY = dragonEntity("forest_dragon", ForestDragonEntity::new, ForestDragonEntity::getHabitatPoints, 0x054a00, 0x0a9600);
    public static final RegistryObject<EntityType<TameableDragonEntity>> GHOST_DRAGON_ENTITY = dragonEntity("ghost_dragon", GhostDragonEntity::new, GhostDragonEntity::getHabitatPoints, 0xc4c4c4, 0xc2f8ff);
    public static final RegistryObject<EntityType<TameableDragonEntity>> ICE_DRAGON_ENTITY = dragonEntity("ice_dragon", IceDragonEntity::new, IceDragonEntity::getHabitatPoints, 0xffffff, 0x00b7ff);
    public static final RegistryObject<EntityType<TameableDragonEntity>> NETHER_DRAGON_ENTITY = dragonEntity("nether_dragon", NetherDragonEntity::new, NetherDragonEntity::getHabitatPoints, 0x912400, 0x2e0b00, EntityType.Builder::immuneToFire);
    public static final RegistryObject<EntityType<TameableDragonEntity>> WATER_DRAGON_ENTITY = dragonEntity("water_dragon", WaterDragonEntity::new, WaterDragonEntity::getHabitatPoints, 0x0062ff, 0x5999ff);
    public static final RegistryObject<Block> AETHER_EGG_BLOCK = block("aether_dragon_egg");
    public static final RegistryObject<EntityType<DragonEggEntity>> EGG_ENTITY = entity("egg", DragonEggEntity::new, EntityClassification.MISC, b -> b.size(DragonEggEntity.WIDTH, DragonEggEntity.HEIGHT).setShouldReceiveVelocityUpdates(true).setUpdateInterval(20).setTrackingRange(10).disableSummoning().immuneToFire().build(DragonMountsLegacy.MOD_ID + ":egg"));
    public static final RegistryObject<Block> ENDER_EGG_BLOCK = block("ender_dragon_egg");
    public static final RegistryObject<Block> FIRE_EGG_BLOCK = block("fire_dragon_egg"); // todo, seperate builder properties for higher light values
    public static final RegistryObject<Block> FOREST_EGG_BLOCK = block("forest_dragon_egg");
    public static final RegistryObject<Block> GHOST_EGG_BLOCK = block("ghost_dragon_egg");
    public static final RegistryObject<Block> ICE_EGG_BLOCK = block("ice_dragon_egg");
    public static final RegistryObject<Block> NETHER_EGG_BLOCK = block("nether_dragon_egg"); // todo, seperate builder properties for higher light values
    public static final RegistryObject<Block> WATER_EGG_BLOCK = block("water_dragon_egg");

    public static final RegistryObject<SoundEvent> DRAGON_BREATHE_SOUND = sound("dragon.breathe");
    public static final RegistryObject<SoundEvent> DRAGON_STEP_SOUND = sound("dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH_SOUND = sound("dragon.death");

    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.IFactory<T> factory, EntityClassification classification, Consumer<EntityType.Builder<T>> builderConsumer)
    {
        EntityType.Builder<T> builder = EntityType.Builder.create(factory, classification);
        builderConsumer.accept(builder);
        return ENTITIES.register(name, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
    }

    private static <T extends TameableDragonEntity> RegistryObject<EntityType<T>> dragonEntity(String name, EntityType.IFactory<T> factory, ToIntFunction<DragonEggEntity> habitatFunc, int primColor, int secColor, Consumer<EntityType.Builder<T>> builderConsumer)
    {
        EntityType.Builder<T> builder = EntityType.Builder.create(factory, EntityClassification.CREATURE);
        builder.setShouldReceiveVelocityUpdates(true)
                .setUpdateInterval(3)
                .setTrackingRange(80)
                .size(TameableDragonEntity.BASE_WIDTH, TameableDragonEntity.BASE_HEIGHT);
        builderConsumer.accept(builder);
        RegistryObject<EntityType<T>> delegate = ENTITIES.register(name, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));

        block(name + "_egg", () -> new DragonEggBlock(delegate::get, habitatFunc, primColor, secColor));
        ITEMS.register(name + "_spawn_egg", () -> new LazySpawnEggItem(delegate::get, primColor, secColor));
        return delegate;
    }

    private static <T extends TameableDragonEntity> RegistryObject<EntityType<T>> dragonEntity(String name, EntityType.IFactory<T> factory, ToIntFunction<DragonEggEntity> habitatFunc, int primColor, int secColor)
    {
        return dragonEntity(name, factory, habitatFunc, primColor, secColor, t ->
        {});
    }

    private static RegistryObject<Block> block(String name, Supplier<Block> sup)
    {
        RegistryObject<Block> delegate = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(delegate.get(), new Item.Properties().group(ItemGroup.MISC)));
        return delegate;
    }

    private static RegistryObject<Block> block(String name)
    {
        return RegistryObject.of(DragonMountsLegacy.rl(name), ForgeRegistries.BLOCKS);
    }

    private static RegistryObject<SoundEvent> sound(String name)
    {
        String registryName = "entity." + name;
        return SOUNDS.register(registryName, () -> new SoundEvent(DragonMountsLegacy.rl(registryName)));
    }
}
