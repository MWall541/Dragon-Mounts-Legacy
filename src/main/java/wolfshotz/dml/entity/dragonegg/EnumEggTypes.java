package wolfshotz.dml.entity.dragonegg;

import net.minecraft.entity.EntityType;
import net.minecraft.util.IStringSerializable;
import org.apache.commons.lang3.tuple.Pair;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.DragonSpawnEggItem;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.*;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum EnumEggTypes implements IStringSerializable
{
    AETHER(DMLEntities.AETHER_DAGON, AetherDragonEntity::isHabitat, 0x11d6d0, 0xffff00),
    ENDER(DMLEntities.ENDER_DRAGON, EndDragonEntity::isHabitat, 0x161616, 0xff63e8),
    NETHER(DMLEntities.NETHER_DRAGON, NetherDragonEntity::isHabitat, 0x912400, 0x2e0b00),
    FIRE(DMLEntities.FIRE_DRAGON, FireDragonEntity::isHabitat, 0x912400, 0xff9819),
    FOREST(DMLEntities.FOREST_DRAGON, ForestDragonEntity::isHabitat, 0x054a00, 0x0a9600),
    GHOST(DMLEntities.GHOST_DRAGON, GhostDragonEntity::isHabitat, 0xc4c4c4, 0xc2f8ff),
    ICE(DMLEntities.ICE_DRAGON, IceDragonEntity::isHabitat, 0xffffff, 0x00b7ff),
    WATER(DMLEntities.WATER_DRAGON, WaterDragonEntity::isHabitat, 0x0062ff, 0x5999ff);

    public static final EnumEggTypes[] VALUES = values(); // cache for speed

    private final Supplier<EntityType<TameableDragonEntity>> type;
    private final Predicate<DragonEggEntity> habitatCheck;
    private final int primColor, secColor;

    static
    {
        for (EnumEggTypes value : VALUES)
            DragonMountsLegacy.ITEMS.register(value.getName() + "_dragon_spawn_egg", () -> new DragonSpawnEggItem(value));
    }

    EnumEggTypes(Supplier<EntityType<TameableDragonEntity>> type, Predicate<DragonEggEntity> habitatCheck, int primColor, int secColor)
    {
        this.type = type;
        this.habitatCheck = habitatCheck;
        this.primColor = primColor;
        this.secColor = secColor;
    }

    public static EnumEggTypes getByHabitat(DragonEggEntity egg)
    {
        return Arrays.stream(VALUES).filter(v -> v.habitatCheck.test(egg)).findFirst().orElse(null);
    }

    public static EnumEggTypes getByType(EntityType<?> type)
    {
        for (EnumEggTypes value : VALUES) if (value.getType().equals(type)) return value;
        return AETHER;  // FALLBACK!
    }

    public EntityType<TameableDragonEntity> getType() { return type.get(); }

    public Pair<Integer, Integer> getColors() { return Pair.of(primColor, secColor); }

    public float getRColor(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 16) & 0xFF) / 255f;
    }

    public float getGColor(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 8) & 0xFF) / 255f;
    }

    @Override
    public String getName() { return toString().toLowerCase(); }

    public float getBColor(boolean primary)
    {
        int color = primary? primColor : secColor;
        return (color & 0xFF) / 255f;
    }
}
