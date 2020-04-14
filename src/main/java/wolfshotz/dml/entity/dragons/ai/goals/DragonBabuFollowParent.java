package wolfshotz.dml.entity.dragons.ai.goals;

import net.minecraft.entity.ai.goal.Goal;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

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
        if (list.isEmpty()) return false;
        TameableDragonEntity potentialParent = null;
        double distance = Double.MAX_VALUE;
        for (TameableDragonEntity dagin : list)
        {
            double d1 = babu.getDistanceSq(dagin);
            if (!(d1 > distance))
            {
                distance = d1;
                potentialParent = dagin;
            }
        }

        if (potentialParent == null) return false;
        this.adultParent = potentialParent;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (!babu.isHatchling()) return false;
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
        if (--delayCounter <= 0)
        {
            delayCounter = 10;
            babu.getNavigator().tryMoveToEntityLiving(adultParent, 1);
        }
    }
}
