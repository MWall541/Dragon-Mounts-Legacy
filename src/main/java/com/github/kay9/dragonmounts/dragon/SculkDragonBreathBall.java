package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SculkDragonBreathBall extends LargeFireball {

    private double startX;
    private double startY;
    private double startZ;

    // Constructor for the Registry and Loading from NBT
    public SculkDragonBreathBall(EntityType<? extends SculkDragonBreathBall> type, Level level) {
        super(type, level);
        // These might be 0 until the entity is actually spawned
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
    }

    // Constructor for Dragon to use
    public SculkDragonBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz, int power) {
        // Call our OWN first constructor using the Registry Type
        this(DMLRegistry.SCULK_BREATH.get(), level);

        // Manually set the owner (shooter)
        this.setOwner(shooter);

        // Manually set starting position to the shooter
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        this.reapplyPosition();

        // Manually set the movement direction (acceleration)
        this.xPower = dx * 0.12D;
        this.yPower = dy * 0.12D;
        this.zPower = dz * 0.12D;

        // Record starting position for distance check
        this.startX = shooter.getX();
        this.startY = shooter.getY();
        this.startZ = shooter.getZ();
    }

    @Override
    public void tick() {
        super.tick();

        // Max distance check
        double distSq = this.distanceToSqr(startX, startY, startZ);
        if (distSq > 400.0) { // 20.0 * 20.0
            this.onHit(new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @NotNull ParticleOptions getTrailParticle() {
        return ParticleTypes.SONIC_BOOM;
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
        if (target instanceof SculkDragonBreathBall) {
            return false;
        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) { super.onHitEntity(result); }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner != null) {
                // 1. Play the iconic Warden Sonic Boom sound
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0F, 1.0F);

                // 2. Trigger a Game Event (Sculk Sensors will hear this!)
                this.level().gameEvent(owner, GameEvent.EXPLODE, this.position());

                // 3. Apply the custom Sonic/Sculk damage
                this.applySculkAreaEffect(owner);

                // 4. Visual "Burst" of sonic particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                            this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                }
            }
            this.discard();
        }
    }

    private void applySculkAreaEffect(Entity owner) {
        double blastRadius = 2.5; // Slightly larger blast because it's "sound"
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(blastRadius), e -> e != this);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                if (owner instanceof LivingEntity livingOwner) {
                    // Standard damage
                    entity.hurt(level().damageSources().sonicBoom(owner), DMLConfig.getBreathDamage());

                    // Apply Darkness effect (Iconic to the Deep Dark)
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));

                    // Sonic Knockback (Horizontal and Vertical)
                    Vec3 knockbackVec = entity.position().subtract(this.position()).normalize().scale(0.5);
                    entity.push(knockbackVec.x, 0.3D, knockbackVec.z);
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
}