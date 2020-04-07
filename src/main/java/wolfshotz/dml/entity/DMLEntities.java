package wolfshotz.dml.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;
import wolfshotz.dml.entity.dragons.*;

import java.util.function.Supplier;

public class DMLEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, DragonMountsLegacy.MOD_ID);

    public static final RegistryObject<DragonEntityType> AETHER_DAGON = register("aether_dragon", () -> new DragonEntityType(AetherDragonEntity::new, EnumEggTypes.AETHER).setHabitat(AetherDragonEntity::isHabitat).setColors(0x11d6d0, 0xd3d611));
    public static final RegistryObject<DragonEntityType> ENDER_DRAGON = register("ender_dragon", () -> new DragonEntityType(EndDragonEntity::new, EnumEggTypes.ENDER).setHabitat(EndDragonEntity::isHabitat).setColors(0x0, 0x37036b));
    public static final RegistryObject<DragonEntityType> FIRE_DRAGON = register("fire_dragon", () -> new DragonEntityType(FireDragonEntity::new, EnumEggTypes.FIRE).setHabitat(FireDragonEntity::isHabitat).setColors(0x912400, 0xff9819).setTailScales());
    public static final RegistryObject<DragonEntityType> FOREST_DRAGON = register("forest_dragon", () -> new DragonEntityType(ForestDragonEntity::new, EnumEggTypes.FOREST).setHabitat(ForestDragonEntity::isHabitat).setColors(0x054a00, 0x0a9600));
    public static final RegistryObject<DragonEntityType> GHOST_DRAGON = register("ghost_dragon", () -> new DragonEntityType(GhostDragonEntity::new, EnumEggTypes.GHOST).setHabitat(GhostDragonEntity::isHabitat).setColors(0xc4c4c4, 0x292929).setThinLegs());
    public static final RegistryObject<DragonEntityType> ICE_DRAGON = register("ice_dragon", () -> new DragonEntityType(IceDragonEntity::new, EnumEggTypes.ICE).setHabitat(IceDragonEntity::isHabitat).setColors(0xcfcfcf, 0xaefcfb));
    public static final RegistryObject<DragonEntityType> NETHER_DRAGON = register("nether_dragon", () -> new DragonEntityType(NetherDragonEntity::new, EnumEggTypes.NETHER).setHabitat(NetherDragonEntity::isHabitat).setColors(0x912400, 0x2e0b00));
    public static final RegistryObject<DragonEntityType> WATER_DRAGON = register("water_dragon", () -> new DragonEntityType(WaterDragonEntity::new, EnumEggTypes.WATER).setHabitat(WaterDragonEntity::isHabitat).setColors(0x0062ff, 0x5999ff).setTailHorns());

    public static final RegistryObject<EntityType<DragonEggEntity>> EGG = register("egg", () -> EntityType.Builder.<DragonEggEntity>create(EntityClassification.MISC).size(DragonEggEntity.WIDTH, DragonEggEntity.HEIGHT).disableSummoning().setCustomClientFactory((e, w) -> new DragonEggEntity(w)).build(DragonMountsLegacy.MOD_ID + ":egg"));

    public static <T extends EntityType<?>> RegistryObject<T> register(String name, Supplier<T> type)
    {
        return ENTITIES.register(name, type);
    }
}
