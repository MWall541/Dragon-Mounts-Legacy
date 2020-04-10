package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

import static net.minecraft.entity.SharedMonsterAttributes.FLYING_SPEED;

public class AetherDragonEntity extends TameableDragonEntity
{
    public static final double AETHER_BASE_SPEED_FLYING = BASE_SPEED_FLYING * 1.5d;

    public AetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg) { return egg.getPosY() > egg.world.getHeight() * 0.66f; }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(FLYING_SPEED).setBaseValue(AETHER_BASE_SPEED_FLYING);
    }
}
