package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonFireballAttackGoal extends Goal {
    private final TameableDragon dragon;

    private int attackCooldown;
    private int burstShotsRemaining = 0;
    private int burstDelay = 0;
    private boolean startedFlying = false;

    private static final int BURST_SIZE = 15;
    private static final int BURST_INTERVAL = 0;
    private static final int BURST_COOLDOWN = 2;
    private static final double MAX_ATTACK_DISTANCE_SQR = 2500.0;
    private static final double MIN_FIREBALL_DISTANCE_SQR = 16.0;

    public DragonFireballAttackGoal(TameableDragon dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        this.attackCooldown = 0;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = dragon.getTarget();
        if (target == null || !target.isAlive() || isFriendly(target)) return false;
        double distance = dragon.distanceToSqr(target);
        return distance <= MAX_ATTACK_DISTANCE_SQR && distance >= MIN_FIREBALL_DISTANCE_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = dragon.getTarget();
        if (target == null || !target.isAlive() || isFriendly(target)) return false;
        double distance = dragon.distanceToSqr(target);
        return distance <= MAX_ATTACK_DISTANCE_SQR && distance >= MIN_FIREBALL_DISTANCE_SQR;
    }

    @Override
    public void tick() {
        LivingEntity target = dragon.getTarget();
        if (target != null) {
            double distance = dragon.distanceToSqr(target);
            if (distance >= MIN_FIREBALL_DISTANCE_SQR) {
                if (burstShotsRemaining > 0) {
                    if (--burstDelay <= 0) {
                        ensureFlying();
                        shootFireball(target);
                        burstShotsRemaining--;
                        burstDelay = BURST_INTERVAL;
                    }
                    return;
                }

                if (attackCooldown > 0) {
                    attackCooldown--;
                    return;
                }

                burstShotsRemaining = BURST_SIZE;
                burstDelay = 0;
                attackCooldown = BURST_COOLDOWN;
                ensureFlying();
            }
            dragon.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    @Override
    public void stop() {
        burstShotsRemaining = 0;
        burstDelay = 0;
        if (startedFlying) {
            dragon.setFlying(false);
            startedFlying = false;
        }
    }

    private void ensureFlying() {
        if (!dragon.isFlying()) {
            if (this.dragon.getRandom().nextFloat() < 0.025f) {
                dragon.liftOff();
                dragon.setFlying(true);
                startedFlying = true;
            }
        }
    }

    private void shootFireball(LivingEntity target) {
        if (dragon.isServer()) {
            Vec3 mouthPos = dragon.getApproximateMouthPos();
            Vec3 direction = target.getBoundingBox().getCenter().subtract(mouthPos).normalize();
            dragon.performBreathAttack(dragon, direction);
        }
    }

    private boolean isFriendly(LivingEntity target) {
        if (dragon.isOwnedBy(target)) return true;
        if (target instanceof OwnableEntity ownableTarget) {
            if (dragon.getOwnerUUID() != null && dragon.getOwnerUUID().equals(ownableTarget.getOwnerUUID())) {
                return true;
            }
        }
        return dragon.isAlliedTo(target);
    }
}