package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EndDragonEntity extends TameableDragonEntity
{
    public EndDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        damageImmunities.add(DamageSource.MAGIC);
    }
}
