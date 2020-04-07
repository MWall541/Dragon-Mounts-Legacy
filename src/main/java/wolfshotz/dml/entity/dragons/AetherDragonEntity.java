package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

public class AetherDragonEntity extends TameableDragonEntity
{
    public AetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg) { return egg.getPosY() > egg.world.getHeight() * 0.66f; }

}
