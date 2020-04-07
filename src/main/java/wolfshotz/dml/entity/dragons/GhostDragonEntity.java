package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

public class GhostDragonEntity extends TameableDragonEntity
{
    public GhostDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        // woah dude, too high!
        if (egg.getPosY() > egg.world.getHeight() * 0.25) return false;

        BlockPos pos = egg.getPosition();

        // sun is shining!
        if (egg.world.canBlockSeeSky(pos)) return false;

        // too bright!
        return egg.world.getLight(pos) <= 4;
    }
}
