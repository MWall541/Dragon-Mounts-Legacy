package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WitherBreathBall extends WitherSkull {

    private final double startX;
    private final double startY;
    private final double startZ;

    public WitherBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz) {
        super(level, shooter, dx, dy, dz);
        this.setOwner(shooter);
        this.setDeltaMovement(dx * 1.5D, dy * 1.5D, dz * 1.5D);
        // Record starting position
        this.startX = shooter.getX();
        this.startY = shooter.getY();
        this.startZ = shooter.getZ();
    }

    @Override
    public void tick() {
        super.tick();

        // Max distance check
        double maxRange = DMLConfig.getBreathRange();
        double distSq = this.distanceToSqr(startX, startY, startZ);
        if (distSq > (maxRange * maxRange)) {
            this.onHit(new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
        }
    }

    @Override
    protected @NotNull ParticleOptions getTrailParticle() {
        return ParticleTypes.SOUL;
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
                boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
                // 1. Trigger the visual/block explosion
                if (canGrief){
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.75F, false, Level.ExplosionInteraction.MOB);
                }
                // 2. Handle damage, wither effects, and Wither Rose spawning
                this.applyAreaEffectDamage(owner);
            }

            // Remove the wither breath ball entity
            this.discard();
        }
    }

    private void applyAreaEffectDamage(Entity owner) {
        double blastRadius = 1.5;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(blastRadius), e -> e != this);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                if (owner instanceof LivingEntity livingOwner) {
                    // Check if the entity was already dead to avoid redundant rose spawning
                    boolean wasAlreadyDead = !livingTarget.isAlive();

                    // Deal config-driven damage
                    entity.hurt(this.level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());

                    if (!wasAlreadyDead && !livingTarget.isAlive()) {
                        // Spawns a rose if the projectile was the killing blow
                        this.spawnWitherRose(livingTarget);
                    } else if (livingTarget.isAlive()) {
                        // Apply Wither effect to survivors
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
                    }
                }
            }
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
            BlockState state = Blocks.WITHER_ROSE.defaultBlockState();

            // Check if the block is air (or replaceable) and can sustain a Wither Rose
            if (this.level().getBlockState(pos).isAir() && state.canSurvive(this.level(), pos)) {
                this.level().setBlockAndUpdate(pos, state);
            }
        }
    }
}