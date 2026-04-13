package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class DragonWanderAreaGoal extends Goal {
    private final TameableDragon dragon;
    private final double speed;

    public DragonWanderAreaGoal(TameableDragon dragon, double speed) {
        this.dragon = dragon;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (dragon.getNavigation().isInProgress()) return false;

        return dragon.getCommandState() == TameableDragon.STATE_WANDER
                && dragon.getWanderHomePos() != null
                && dragon.getRandom().nextInt(reducedTickDelay(30)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !dragon.getNavigation().isDone() && dragon.getCommandState() == TameableDragon.STATE_WANDER;
    }

    @Override
    public void start() {
        Vec3 target = findTarget();
        if (target != null) {
            dragon.getNavigation().moveTo(target.x, target.y, target.z, speed);
        }
    }

    @Nullable
    private Vec3 findTarget() {
        BlockPos home = dragon.getWanderHomePos();
        int range = DMLConfig.getWanderRange();
        int maxDistX = 16;
        int maxDistY = 16;
        Vec3 homeVec = Vec3.atCenterOf(home);

        Vec3 target;
        if (dragon.isFlying()) {
            target = AirAndWaterRandomPos.getPos(dragon, maxDistX, maxDistY, 0,
                    dragon.getViewVector(0).x, dragon.getViewVector(0).z, Math.PI / 2
            );
        } else {
            target = DefaultRandomPos.getPos(dragon, maxDistX, maxDistY);
        }

        if (target == null) return null;

        // Horizontal distance check to keep the dragon near home base
        double dx = target.x - homeVec.x;
        double dz = target.z - homeVec.z;
        double distSq2D = dx * dx + dz * dz;

        if (distSq2D > (double) range * range) {
            return DefaultRandomPos.getPosTowards(dragon, maxDistX, maxDistY, homeVec, (float) Math.PI / 2);
        }

        return target;
    }
}