package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WitherBreathBall extends WitherSkull {

    private final double startX;
    private final double startY;
    private final double startZ;

    public WitherBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz) {
        super(level, shooter, dx, dy, dz);
        this.setOwner(shooter);
        // Record starting position
        this.startX = shooter.getX();
        this.startY = shooter.getY();
        this.startZ = shooter.getZ();
    }

    @Override
    public void tick() {
        super.tick();

        // Check max distance
        double dx = this.getX() - startX;
        double dy = this.getY() - startY;
        double dz = this.getZ() - startZ;
        double distanceSq = dx*dx + dy*dy + dz*dz;
        // max distance in blocks
        double maxDistance = 20.0;
        if (distanceSq > maxDistance * maxDistance) {
            // Explode even if it didn't hit a block
            this.onHit(new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
        }
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {

        Entity owner = this.getOwner();

        // Never collide with owner (player)
        if (target == owner) {
            return false;
        }

        // Never collide with the dragon that fired it
        if (owner != null && owner.getVehicle() == target) {
            return false;
        }

        // Never collide with passengers of owner (mounted player case)
        if (owner != null && target.isPassengerOfSameVehicle(owner)) {
            return false;
        }

        // Ignore other dragon breath balls
        if (target instanceof WitherBreathBall) {
            return false;
        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            // Check if owner is alive to prevent null pointer crashes
            if (owner != null) {
                // Give entities caught in the explosion withering and damage them
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.75F, false, Level.ExplosionInteraction.MOB);
                double blastRadius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(blastRadius), e -> e != this);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingTarget) {
                        boolean isProtected = isPartOfDragonCrew(livingTarget, owner);
                        if (!isProtected) {
                            if (owner instanceof LivingEntity livingOwner) {
                                boolean wasAlreadyDead = livingTarget.deathTime > 0 || !livingTarget.isAlive();
                                // Deal the damage
                                entity.hurt(level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                                // If the entity died, try to spawn a Wither Rose
                                if (!wasAlreadyDead && !livingTarget.isAlive()) {
                                    spawnWitherRose(livingTarget);
                                } else {
                                    // If they survived, give them the wither effect
                                    livingTarget.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                            net.minecraft.world.effect.MobEffects.WITHER, 100, 0));
                                }
                            }
                        }
                    }
                }
            }

            // Remove the wither breath ball entity
            this.discard();
        }
    }

    // Helper method to resolve the Iterable .contains issue
    private static boolean isPartOfDragonCrew(Entity target, Entity shooter) {
        if (shooter == null) return false;
        if (target.equals(shooter)) return true;

        // Check if they are sharing a vehicle or are passengers
        if (target.isPassengerOfSameVehicle(shooter)) return true;

        // Check if the target is a pet/dragon owned by the shooter
        if (target instanceof net.minecraft.world.entity.OwnableEntity ownable) {
            if (shooter.getUUID().equals(ownable.getOwnerUUID())) {
                return true;
            }
        }

        // If the shooter is the dragon, protect the player owner
        if (shooter instanceof net.minecraft.world.entity.OwnableEntity ownableShooter) {
            if (target.getUUID().equals(ownableShooter.getOwnerUUID())) {
                return true;
            }
        }

        // Manually iterate over indirect passengers since Iterable lacks .contains()
        for (Entity passenger : shooter.getIndirectPassengers()) {
            if (passenger.equals(target)) {
                return true;
            }
        }

        return false;
    }

    private void spawnWitherRose(LivingEntity target) {
        if (!this.level().isClientSide) {
            BlockPos pos = target.blockPosition();
            BlockState state = net.minecraft.world.level.block.Blocks.WITHER_ROSE.defaultBlockState();

            // Check if the block is air (or replaceable) and can sustain a Wither Rose
            if (this.level().getBlockState(pos).isAir() && state.canSurvive(this.level(), pos)) {
                this.level().setBlockAndUpdate(pos, state);
            }
        }
    }
}