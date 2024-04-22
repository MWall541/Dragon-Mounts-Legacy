package com.github.kay9.dragonmounts.dragon.ai.behaviors;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SitWhenOrderedTo extends Behavior<TamableAnimal>
{
    public SitWhenOrderedTo()
    {
        super(ImmutableMap.of(DMLRegistry.SIT_MEMORY.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal animal)
    {
        return animal.isTame()
                && animal.isOrderedToSit()
                && animal.onGround();
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal animal, long gameTime)
    {
        animal.getBrain().clearMemories();
        animal.getBrain().setMemory(DMLRegistry.SIT_MEMORY.get(), Unit.INSTANCE);
        animal.setInSittingPose(true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TamableAnimal animal, long gameTime)
    {
        // Don't call super here because it just returns false.
        return animal.isOrderedToSit()
                && animal.onGround();
    }

    @Override
    protected boolean timedOut(long pGameTime)
    {
        // Don't want the dragons to randomly stand up.
        return false;
    }

    @Override
    protected void stop(ServerLevel level, TamableAnimal animal, long gameTime)
    {
        animal.setInSittingPose(false);
    }
}
