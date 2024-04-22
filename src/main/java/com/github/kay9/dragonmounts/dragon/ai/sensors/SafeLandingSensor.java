package com.github.kay9.dragonmounts.dragon.ai.sensors;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A sensor that searches for a viable solid block nearby that we can safely go to.
 */
public class SafeLandingSensor extends Sensor<PathfinderMob>
{
    private static final int SCAN_RATE = 30; // every second and a half
    private static final int MEMORY_EXPIRY = 600;
    private static final Supplier<Set<MemoryModuleType<?>>> REQUIRED_MEMORIES = Suppliers.memoize(() -> ImmutableSet.of(
            DMLRegistry.SAFE_LANDING_MEMORY.get()));

    public SafeLandingSensor()
    {
        super(SCAN_RATE);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return REQUIRED_MEMORIES.get();
    }

    @Override
    protected void doTick(ServerLevel level, PathfinderMob entity)
    {
        if (entity.onGround() || !entity.getBrain().isActive(DMLRegistry.SIT_ACTIVITY.get())) return;

        // search ourselves instead of using heap polluting helper methods
        for (BlockPos pos : BlockPos.withinManhattan(entity.blockPosition(), 30, 30, 30))
        {
            if (GoalUtils.isSolid(entity, pos))
            {
                pos = LandRandomPos.movePosUpOutOfSolid(entity, pos);
                if (pos != null)
                {
                    entity.getBrain().setMemoryWithExpiry(DMLRegistry.SAFE_LANDING_MEMORY.get(), pos.immutable(), MEMORY_EXPIRY);
                    return;
                }
            }
        }
    }
}
