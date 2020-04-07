package wolfshotz.dml.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragons.*;

public class DMLEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, DragonMountsLegacy.MOD_ID);

    public static final RegistryObject<EntityType<TameableDragonEntity>> AETHER_DAGON = dragon("aether_dragon", AetherDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> END_DRAGON = dragon("ender_dragon", EndDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FIRE_DRAGON = dragon("fire_dragon", FireDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> FOREST_DRAGON = dragon("forest_dragon", ForestDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> GHOST_DRAGON = dragon("ghost_dragon", GhostDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> ICE_DRAGON = dragon("ice_dragon", IceDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> NETHER_DRAGON = dragon("nether_dragon", NetherDragonEntity::new);
    public static final RegistryObject<EntityType<TameableDragonEntity>> WATER_DRAGON = dragon("water_dragon", WaterDragonEntity::new);

    public static final RegistryObject<EntityType<DragonEggEntity>> EGG = register("egg", EntityType.Builder.<DragonEggEntity>create(EntityClassification.MISC).size(DragonEggEntity.WIDTH, DragonEggEntity.HEIGHT).disableSummoning().setCustomClientFactory((e, w) -> new DragonEggEntity(w)));

    public static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
    {
        return ENTITIES.register(name, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
    }

    public static <T extends TameableDragonEntity> RegistryObject<EntityType<T>> dragon(String name, EntityType.IFactory<T> factory)
    {
        return register(name, EntityType.Builder.create(factory, EntityClassification.CREATURE)
                .size(TameableDragonEntity.BASE_WIDTH, TameableDragonEntity.BASE_HEIGHT)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(80)
                .setUpdateInterval(3));
    }
}
