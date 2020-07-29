package wolfshotz.dml.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import static net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED;

public class AetherDragonEntity extends TameableDragonEntity
{
    public static final double AETHER_BASE_SPEED_FLYING = BASE_SPEED_FLYING * 1.5d;

    public AetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(FLYING_SPEED).setBaseValue(AETHER_BASE_SPEED_FLYING);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        if (egg.getPosY() > egg.world.getHeight() * 0.66f) return 3;
        return 0;
    }
}
