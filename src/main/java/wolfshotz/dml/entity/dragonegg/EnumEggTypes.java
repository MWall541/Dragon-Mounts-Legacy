package wolfshotz.dml.entity.dragonegg;

import net.minecraft.util.IStringSerializable;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.DragonEntityType;

import java.util.function.Supplier;

public enum EnumEggTypes implements IStringSerializable
{
    AETHER(DMLEntities.AETHER_DAGON),
    ENDER(DMLEntities.ENDER_DRAGON),
    FIRE(DMLEntities.FIRE_DRAGON),
    FOREST(DMLEntities.FOREST_DRAGON),
    GHOST(DMLEntities.GHOST_DRAGON),
    ICE(DMLEntities.ICE_DRAGON),
    NETHER(DMLEntities.NETHER_DRAGON),
    WATER(DMLEntities.WATER_DRAGON);

    private final Supplier<DragonEntityType> type;

    EnumEggTypes(Supplier<DragonEntityType> type)
    {
        this.type = type;
    }

    public DragonEntityType getType() { return type.get(); }

    @Override
    public String getName() { return toString().toLowerCase(); }
}
