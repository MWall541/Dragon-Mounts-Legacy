package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LiftOffIfTargetIsHighEnough extends Behavior<TameableDragon>
{
    private static final int HEIGHT_NEEDED = 3;
    private static final int VERTICAL_SAFE_ZONE = 3;
    private final int heightNeeded;

    public LiftOffIfTargetIsHighEnough()
    {
        this(HEIGHT_NEEDED);
    }

    public LiftOffIfTargetIsHighEnough(int heightNeeded)
    {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.heightNeeded = heightNeeded;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TameableDragon dragon)
    {
        Optional<WalkTarget> walkTarget = dragon.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
        double verticalDistance = walkTarget.get().getTarget().currentPosition().y - dragon.getY();
        boolean noCollision = level.noCollision(dragon, dragon.getBoundingBox().move(0, VERTICAL_SAFE_ZONE, 0));
        return !dragon.isFlying() && dragon.canFly() && noCollision && verticalDistance >= this.heightNeeded && super.checkExtraStartConditions(level, dragon);
    }

    @Override
    protected void start(ServerLevel level, TameableDragon dragon, long gameTime)
    {
        super.start(level, dragon, gameTime);
        dragon.liftOff();
    }
}