package wolfshotz.dml.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.World;

public class AetherDragonEntity extends TameableDragonEntity
{
    public static final double AETHER_BASE_SPEED_FLYING = BASE_SPEED_FLYING * 1.5d;

    public AetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return TameableDragonEntity.getAttributes().createMutableAttribute(Attributes.FLYING_SPEED, AETHER_BASE_SPEED_FLYING);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        if (egg.getPosY() > egg.world.getHeight() * 0.66f) return 3;
        return 0;
    }
}
