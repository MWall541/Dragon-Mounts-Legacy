package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Goal for dragon to follow its owner.
 * <p></p>
 * Mostly copied from <code>FollowOwnerGoal</code>, but with some modifications to fix an issue.
 * Also allows dragon to tp to owner in the air, so they don't get stuck until the owner lands.
 *
 * @author AnimalsWritingCode
 *
 * @see net.minecraft.world.entity.ai.goal.FollowOwnerGoal
 */
@SuppressWarnings("DataFlowIssue")
public class DragonFollowOwnerGoal extends Goal
{
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MIN_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 0;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final TameableDragon dragon;
    private final LevelReader level;
    private final double speedModifier;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private final float teleportDistance;
    private float oldWaterCost;

    public DragonFollowOwnerGoal(TameableDragon dragon, double speedModifier, float startDistance, float stopDistance, float teleportDistance)
    {
        this.dragon = dragon;
        this.level = dragon.level();
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.teleportDistance = teleportDistance;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse()
    {
        LivingEntity livingentity = dragon.getOwner();
        if (livingentity == null) {
            return false;
        }
        if (livingentity.isSpectator())
        {
            return false;
        }
        if (dragon.isOrderedToSit())
        {
            return false;
        }
        return dragon.distanceToSqr(livingentity) >= (double)(startDistance * startDistance);
    }

    public boolean canContinueToUse()
    {
        if (dragon.getNavigation().isDone())
        {
            return false;
        }
        if (dragon.isOrderedToSit())
        {
            return false;
        }
        return dragon.distanceToSqr(dragon.getOwner()) >= (double)(stopDistance * stopDistance);
    }

    public void start()
    {
        timeToRecalcPath = 0;
        oldWaterCost = dragon.getPathfindingMalus(BlockPathTypes.WATER);
        dragon.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public void stop()
    {
        dragon.getNavigation().stop();
        dragon.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
    }

    public void tick()
    {
        LivingEntity owner = dragon.getOwner();
        dragon.getLookControl().setLookAt(owner, 10.0F, (float)dragon.getMaxHeadXRot());
        if (--timeToRecalcPath <= 0)
        {
            timeToRecalcPath = adjustedTickDelay(10);
            if (!dragon.isLeashed() && !dragon.isPassenger())
            {
                if (dragon.distanceToSqr(owner) >= (teleportDistance * teleportDistance))
                {
                    teleportToOwner();
                }
                else if (
                        !dragon.isFlying()
                                && dragon.canFly()
                                && (owner.blockPosition().getY() - dragon.blockPosition().getY()) >= startDistance)
                {
                    dragon.liftOff();
                }
                else
                {
                    dragon.getNavigation().moveTo(owner, speedModifier);
                }

            }
        }
    }

    private void teleportToOwner()
    {
        BlockPos ownerPos = dragon.getOwner().blockPosition();

        for(int i = 0; i < 10; ++i)
        {
            BlockPos target = randomBlockPosNearPos(
                    ownerPos,
                    MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING,
                    MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING,
                    MIN_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING,
                    MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING
            );
            boolean flag = maybeTeleportTo(target);
            if (flag)
            {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(BlockPos pos)
    {
        LivingEntity owner = dragon.getOwner();
        if (owner.blockPosition().closerThan(pos, 2.0D))
        {
            return false;
        }
        if (!canTeleportTo(pos))
        {
            return false;
        }
        dragon.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ(), dragon.getYRot(), dragon.getXRot());
        dragon.getNavigation().stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos pos)
    {
        if (!dragon.canFly())
        {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, pos.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE)
            {
                return false;
            }

            BlockState blockstate = level.getBlockState(pos.below());
            if (blockstate.getBlock() instanceof LeavesBlock)
            {
                return false;
            }
        }

        BlockPos blockPos = pos.subtract(dragon.blockPosition());
        AABB targetBoundingBox = dragon.getBoundingBox().move(blockPos);
        return level.noCollision(dragon, targetBoundingBox)
                && !level.containsAnyLiquid(targetBoundingBox);
    }

    private int randomIntInclusive(int min, int max)
    {
        return dragon.getRandom().nextInt(max - min + 1) + min;
    }

    private int randomIntInclusive(int farLow, int nearLow, int nearHigh, int farHigh)
    {
        if (nearLow == nearHigh)
        {
            return randomIntInclusive(farLow, farHigh);
        }

        return dragon.getRandom().nextBoolean() ?
                randomIntInclusive(farLow, nearLow) :
                randomIntInclusive(nearHigh, farHigh);
    }

    private BlockPos randomBlockPosNearPos(BlockPos origin, int minDist, int maxDist, int minYDist, int maxYDist)
    {
        int x = randomIntInclusive(-maxDist, -minDist, minDist, maxDist);
        int y = randomIntInclusive(-maxYDist, -minYDist, minYDist, maxYDist);
        int z = randomIntInclusive(-maxDist, -minDist, minDist, maxDist);
        return origin.offset(x, y, z);
    }
}
