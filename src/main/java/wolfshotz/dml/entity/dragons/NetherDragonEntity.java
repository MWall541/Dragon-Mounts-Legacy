package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class NetherDragonEntity extends TameableDragonEntity
{
    public NetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }
}
