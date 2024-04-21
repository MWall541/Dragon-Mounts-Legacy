package com.github.kay9.dragonmounts.dragon.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class FightWithOwner extends Behavior<TamableAnimal>
{
    private static final Map<MemoryModuleType<?>, MemoryStatus> REQUIRED_MEMORIES = ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED);

    // How long until having to check for a target again.
    private static final long ATTACK_DURATION = 200L;
    // How long to allow aggro on owner's attacker. Really only relevant if the dragon can't attack the moment the owner is attacked (like sitting).
    // Smaller means less likely for a dragon standing up to run off to attack.
    private static final int TICKS_TO_REMEMBER_ATTACKER = 100;
    private final long attackDuration;
    private final long ticksToRememberAttacker;
    private LivingEntity attackTarget;

    public FightWithOwner()
    {
        this(ATTACK_DURATION, TICKS_TO_REMEMBER_ATTACKER);
    }

    public FightWithOwner(long attackDuration, int ticksToRememberAttacker)
    {
        super(REQUIRED_MEMORIES);
        this.attackDuration = attackDuration;
        this.ticksToRememberAttacker = ticksToRememberAttacker;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal animal) {
        if (!animal.isTame() || animal.isOrderedToSit()) return false;

        LivingEntity owner = animal.getOwner();
        if (owner == null) return false;

        this.attackTarget = owner.getLastHurtMob();
        if (this.attackTarget == null || owner.tickCount - owner.getLastHurtMobTimestamp() >= this.ticksToRememberAttacker)
        {
            this.attackTarget = owner.getLastHurtByMob();
        }
        return this.attackTarget != null
                && animal.canAttack(this.attackTarget)
                && animal.wantsToAttack(this.attackTarget, owner);
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal animal, long gameTime)
    {
        animal.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, attackTarget, this.attackDuration);
        animal.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}
