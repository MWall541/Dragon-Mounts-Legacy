package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;

public class DragonAi
{
    public static Brain<?> makeBrain(Brain<TameableDragon> brain)
    {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TameableDragon> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8f),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<TameableDragon> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new RunIf<>(TameableDragon::canBreed, new AnimalMakeLove(DMLRegistry.DRAGON.get(), 1.0F)),
                new RunOne<>(ImmutableList.of(
                        Pair.of(new RandomStroll(1.0f), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(1.0f, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)))));
    }

    private static void initFightActivity(Brain<TameableDragon> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                new RunIf<>(TameableDragon::canBreed, new AnimalMakeLove(DMLRegistry.DRAGON.get(), 1.0F)),
                new SetWalkTargetFromAttackTargetIfTargetOutOfReach(1.0F),
                new RunIf<>(TameableDragon::isAdult, new MeleeAttack(40)),
                new RunIf<>(TameableDragon::isBaby, new MeleeAttack(15)),
                new StopAttackingIfTargetInvalid<>(),
                new EraseMemoryIf<>(DragonAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivity(TameableDragon dragon) {
        dragon.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT,
                Activity.IDLE));
    }

    private static boolean isBreeding(TameableDragon dragon) {
        return dragon.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }
}
