package wolfshotz.dml.entity.dragons;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

public class WaterDragonEntity extends TameableDragonEntity
{
    public WaterDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        return egg.world.getBlockState(egg.getPosition()).getBlock() == Blocks.WATER;
    }
}
