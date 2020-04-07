package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class ForestDragonEntity extends TameableDragonEntity
{
    public ForestDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }
}
