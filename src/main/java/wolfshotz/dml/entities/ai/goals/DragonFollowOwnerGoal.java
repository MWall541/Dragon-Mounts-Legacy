package wolfshotz.dml.entities.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import wolfshotz.dml.entities.TameableDragonEntity;

import java.util.EnumSet;
import java.util.Random;

public class DragonFollowOwnerGoal extends Goal
{

    private final TameableDragonEntity dragon;
    private final float minDist, maxDist, tpDist;
    private LivingEntity owner;
    private int pathRetryTimer;

    public DragonFollowOwnerGoal(TameableDragonEntity dragon, float minDist, float maxDist, float tpDist)
    {
        this.dragon = dragon;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.tpDist = tpDist;
        setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean shouldExecute()
    {
        if (dragon.isSitting()) return false;
        if (dragon.getLeashed()) return false;
        LivingEntity owner = dragon.getOwner();
        if (owner == null) return false;
        if (owner.isSpectator()) return false;
        if (dragon.isRidingOrBeingRiddenBy(owner)) return false;
        if (dragon.getDistanceSq(owner) < minDist * minDist) return false;
        this.owner = owner;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (dragon.getNavigator().noPath()) return false;
        if (dragon.isSitting()) return false;
        return dragon.getDistanceSq(owner) <= maxDist * maxDist;
    }

    @Override
    public void startExecuting() { this.pathRetryTimer = 0; }

    @Override
    public void tick()
    {
        dragon.getLookController().setLookPositionWithEntity(owner, 10f, dragon.getVerticalFaceSpeed());
        if (--pathRetryTimer > 0) return;
        this.pathRetryTimer = 10;
        if (dragon.getDistanceSq(owner) > tpDist)
        {
            tryTeleportToOwner();
            return;
        }
        dragon.getNavigator().tryMoveToEntityLiving(owner, 1);
    }

    public void tryTeleportToOwner()
    {
        Random rng = dragon.getRNG();
        BlockPos.Mutable potentialPos = new BlockPos.Mutable();
        for (int i = 0; i < 10; ++i)
        {
            double x = MathHelper.nextInt(rng, -3, 3);
            double y = owner.onGround? 0 : MathHelper.nextInt(rng, -1, 1);
            double z = MathHelper.nextInt(rng, -3, 3);
            potentialPos.setPos(owner.getPosX() + x, owner.getPosY() + y, owner.getPosZ() + z);
            if (WalkNodeProcessor.func_227480_b_(dragon.world, potentialPos.getX(), potentialPos.getY(), potentialPos.getZ()) != PathNodeType.WALKABLE)
                continue;
            if (dragon.world.hasNoCollisions(dragon, dragon.getBoundingBox().offset(potentialPos.subtract(dragon.getPosition()))))
            {
                dragon.setPosition(potentialPos.getX(), potentialPos.getY(), potentialPos.getZ());
                dragon.getNavigator().clearPath();
                return;
            }
        }
    }
}
