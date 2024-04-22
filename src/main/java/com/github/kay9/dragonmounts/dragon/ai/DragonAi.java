package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.ai.behaviors.FightWithOwner;
import com.github.kay9.dragonmounts.dragon.ai.behaviors.SetWalkTargetToOwnerIfFarEnough;
import com.github.kay9.dragonmounts.dragon.ai.behaviors.SitWhenOrderedTo;
import com.github.kay9.dragonmounts.dragon.ai.behaviors.TeleportToOwnerIfFarEnough;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class DragonAi
{
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final long RETALIATE_DURATION = 200L;
    private static final float STROLL_SPEED_FACTOR = 0.85f;

    // init lists lazily to allow our custom types to register
    private static final Supplier<ImmutableList<SensorType<? extends Sensor<? super TameableDragon>>>> SENSOR_TYPES = Suppliers.memoize(() -> ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            DMLRegistry.SAFE_LANDING_SENSOR.get()));
    private static final Supplier<ImmutableList<MemoryModuleType<?>>> MEMORY_TYPES = Suppliers.memoize(() -> ImmutableList.of(
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.AVOID_TARGET,
            DMLRegistry.SIT_MEMORY.get(), // prefer this be set in TamableAnimal rather than a ticking sensor...
            DMLRegistry.SAFE_LANDING_MEMORY.get()));
    private static final Supplier<ImmutableList<Activity>> ACTIVITIES_IN_ORDER = Suppliers.memoize(() -> ImmutableList.of(
            DMLRegistry.SIT_ACTIVITY.get(), // insertion order matters here for activity updates!
            Activity.AVOID,
            Activity.FIGHT,
            Activity.IDLE
    ));

    public static Brain.Provider<TameableDragon> brainProvider()
    {
        return Brain.provider(MEMORY_TYPES.get(), SENSOR_TYPES.get());
    }

    public static Brain<?> makeBrain(Brain<TameableDragon> brain)
    {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initAvoidActivity(brain);
        initSitActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<TameableDragon> brain)
    {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8f),
                new SitWhenOrderedTo(),
                new FightWithOwner(),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<TameableDragon> brain)
    {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                new AnimalMakeLove(DMLRegistry.DRAGON.get(), 1.0f),
                new TeleportToOwnerIfFarEnough(),
                new SetWalkTargetToOwnerIfFarEnough(1.0f),
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 10f, UniformInt.of(30, 60)),
                StartAttacking.create(DragonAi::shouldHunt, DragonAi::findNearestValidAttackTarget),
                getIdleMovementBehaviors()));
    }

    private static void initFightActivity(Brain<TameableDragon> brain)
    {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                MeleeAttack.create(40),
                EraseMemoryIf.create(DragonAi::wantsToStopFighting, MemoryModuleType.ATTACK_TARGET)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initAvoidActivity(Brain<TameableDragon> brain)
    {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.5f, 16, false),
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 10.0F, UniformInt.of(30, 60)),
                getIdleMovementBehaviors(), // walk around randomly if our avoid target is far away enough. todo avoidTarget should just clear.. no?
                EraseMemoryIf.create(DragonAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
        ), MemoryModuleType.AVOID_TARGET);
    }

    private static void initSitActivity(Brain<TameableDragon> brain)
    {
        brain.addActivityAndRemoveMemoryWhenStopped(DMLRegistry.SIT_ACTIVITY.get(), 0, ImmutableList.of(
                setWalkTargetToSafety(1.05f),
                SetEntityLookTargetSometimes.create(EntityType.PLAYER, 10f, UniformInt.of(30, 60))
        ), DMLRegistry.SIT_MEMORY.get());
    }

    public static void updateActivities(TameableDragon dragon)
    {
        dragon.getBrain().setActiveActivityToFirstValid(ACTIVITIES_IN_ORDER.get());
    }

    private static RunOne<TameableDragon> getIdleMovementBehaviors()
    {
        return new RunOne<>(ImmutableList.of(
                Pair.of(RandomStroll.stroll(STROLL_SPEED_FACTOR), 2),
                Pair.of(SetWalkTargetFromLookTarget.create(STROLL_SPEED_FACTOR, 3), 2),
                Pair.of(new DoNothing(30, 60), 1)));
    }

    private static boolean wantsToStopFighting(TameableDragon dragon)
    {
        // If we trigger breeding or the target becomes an ally, stop attacking.
        return BehaviorUtils.isBreeding(dragon) || dragon.getBrain()
                .getMemory(MemoryModuleType.ATTACK_TARGET).filter(target -> dragon.wantsToAttack(target, dragon.getOwner()))
                .isEmpty();
    }

    public static void wasHurtBy(TameableDragon dragon, LivingEntity attacker)
    {
        Brain<TameableDragon> brain = dragon.getBrain();
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (dragon.isHatchling())
            retreatFromNearestTarget(dragon, attacker);
        else
            maybeRetaliate(dragon, attacker);
    }

    private static void maybeRetaliate(TameableDragon dragon, LivingEntity attacker)
    {
        if (!dragon.getBrain().isActive(Activity.AVOID)
                && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(dragon, attacker, 4.0D)
                && Sensor.isEntityAttackable(dragon, attacker)
                && dragon.wantsToAttack(attacker, dragon.getOwner()))
        {
            setAttackTarget(dragon, attacker);
        }
    }

    private static void setAttackTarget(TameableDragon dragon, LivingEntity target)
    {
        Brain<TameableDragon> brain = dragon.getBrain();
        LivingChangeTargetEvent changeTargetEvent = ForgeHooks.onLivingChangeTarget(dragon, target, LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET);
        if (!changeTargetEvent.isCanceled())
        {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            brain.eraseMemory(MemoryModuleType.BREED_TARGET);
            brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, changeTargetEvent.getNewTarget(), RETALIATE_DURATION);
        }
    }

    private static void retreatFromNearestTarget(TameableDragon dragon, LivingEntity target)
    {
        Brain<TameableDragon> brain = dragon.getBrain();
        LivingEntity avoidTarget = BehaviorUtils.getNearestTarget(dragon, brain.getMemory(MemoryModuleType.AVOID_TARGET), target);
        avoidTarget = BehaviorUtils.getNearestTarget(dragon, brain.getMemory(MemoryModuleType.ATTACK_TARGET), avoidTarget);
        // At this point avoidTarget is the closest of target, AVOID_TARGET, and ATTACK_TARGET
        setAvoidTarget(dragon, avoidTarget);
    }

    private static void setAvoidTarget(TameableDragon dragon, LivingEntity target)
    {
        Brain<TameableDragon> brain = dragon.getBrain();
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, RETREAT_DURATION.sample(dragon.level().getRandom()));
    }

    private static boolean wantsToStopFleeing(TameableDragon dragon)
    {
        return dragon.isAdult();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(TameableDragon dragon)
    {
        return dragon.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty())
                .findClosest(e -> e instanceof Animal && !(e instanceof TameableDragon) && Sensor.isEntityAttackable(dragon, e));
    }

    private static boolean shouldHunt(TameableDragon dragon)
    {
        return !dragon.isHatchling() && !dragon.isTame();
    }

    /**
     * Creates a OneShot that will attempt to find a good landing spot if we're not already on one.
     */
    private static OneShot<TameableDragon> setWalkTargetToSafety(float speedMod)
    {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(DMLRegistry.SAFE_LANDING_MEMORY.get()))
                .apply(instance, (walkTarget, safeLanding) -> (level, entity, gameTime) -> {
                    if (entity.onGround()) return false;

                    Vec3 potentialSpot = null;

                    var safeSpot = instance.tryGet(safeLanding);
                    if (safeSpot.isPresent())
                    {
                        potentialSpot = Vec3.atBottomCenterOf(safeSpot.get());
                    }
                    else if (entity.isFlying()) // sensor couldn't find a safe spot, so run around randomly.
                    {
                        // get a spot lower than us
                        var view = entity.getViewVector(0);
                        potentialSpot = AirAndWaterRandomPos.getPos(entity, 20, 0, -10, view.x, view.z, Math.PI / 2);
                    }

                    if (potentialSpot == null) // swimming, blocked, etc, try again.
                    {
                        potentialSpot = LandRandomPos.getPos(entity, 20, 7);
                    }

                    if (potentialSpot != null)
                        walkTarget.set(new WalkTarget(potentialSpot, speedMod, 0));
                    else
                        walkTarget.erase();

                    return true;
                }));
    }
}
