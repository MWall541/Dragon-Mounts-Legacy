package wolfshotz.dml.dragons.ai.goals;

import net.minecraft.entity.ai.goal.Goal;
import wolfshotz.dml.dragons.TameableDragonEntity;

import java.util.Comparator;
import java.util.List;

public class DragonBabuFollowParent extends Goal
{
    private final TameableDragonEntity babu;
    private final float searchDistance;
    private TameableDragonEntity adultParent;
    private int delayCounter = 0;

    public DragonBabuFollowParent(TameableDragonEntity dragon, float searchDistance)
    {
        this.babu = dragon;
        this.searchDistance = searchDistance;
    }

    @Override
    public boolean shouldExecute()
    {
        if (!babu.isHatchling() || babu.getOwner() != null) return false;

        List<TameableDragonEntity> list = babu.world.getEntitiesWithinAABB(TameableDragonEntity.class, babu.getBoundingBox().grow(searchDistance, searchDistance * 0.5f, searchDistance), TameableDragonEntity::isAdult);
        this.adultParent = list.stream().min(Comparator.comparingDouble(babu::getDistanceSq)).orElse(null);
        return adultParent != null;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (!babu.isHatchling()) return false;
        if (babu.getOwner() != null) return false;
        if (adultParent.isAlive()) return false;
        return babu.getDistanceSq(adultParent) < 256d;
    }

    @Override
    public void startExecuting() { this.delayCounter = 0; }

    @Override
    public void resetTask() { adultParent = null; }

    @Override
    public void tick()
    {
        if (--delayCounter <= 0 && babu.getDistance(adultParent) > 4)
        {
            delayCounter = 10;
            babu.getNavigator().tryMoveToEntityLiving(adultParent, 1);
        }
    }
}
