package com.github.kay9.dragonmounts.dragon;

import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class StormDragonBreathBall extends Snowball {

    private final double startX;
    private final double startY;
    private final double startZ;

    public StormDragonBreathBall(Level level, LivingEntity shooter) {
        super(level, shooter);
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
        if (target instanceof StormDragonBreathBall) {
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

                // 10% chance to summon a lightning and a cloud
                if (this.random.nextFloat() < 0.10f) {
                    net.minecraft.world.entity.LightningBolt lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(this.level());
                    if (lightning != null) {
                        // Position the lightning exactly where the projectile hit
                        lightning.moveTo(this.getX(), this.getY(), this.getZ());

                        // Link the lightning to the shooter (for death messages/advancements)
                        if (owner instanceof net.minecraft.server.level.ServerPlayer player) {
                            lightning.setCause(player);
                        }

                        // Add it to the world
                        this.level().addFreshEntity(lightning);
                    }

                    AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                    if (owner instanceof LivingEntity livingOwner) {
                        cloud.setOwner(livingOwner);
                    }

                    cloud.setParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK);
                    cloud.setRadius(2.0F);
                    cloud.setDuration(60); // 3 seconds
                    cloud.setRadiusPerTick((2.0F - cloud.getRadius()) / (float)cloud.getDuration());
                    cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 3));

                    this.level().addFreshEntity(cloud);
                }

                boolean flag = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.0f, flag, Level.ExplosionInteraction.NONE);

                // Build immune list
                List<Entity> immuneEntities = List.of(Objects.requireNonNull(owner)); // owner (player or dragon)

                // Add passengers of owner (mounted case)
                immuneEntities = new java.util.ArrayList<>(immuneEntities);
                immuneEntities.addAll(owner.getPassengers());

                // Add the dragon itself if owner is a dragon riding entity (optional)
                if (owner instanceof LivingEntity) {
                    immuneEntities.add(owner);
                }

                // Add all nearby StormDragonBreathBall instances (to prevent chain fire)
                immuneEntities.add(this); // the snowball itself

                // Damage entities caught in the explosion
                double radius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), e -> e != this);
                for (Entity entity : entities) {
                    if (!immuneEntities.contains(entity)) {
                        if (owner instanceof LivingEntity livingOwner) {
                            entity.hurt(level().damageSources().mobProjectile(this, livingOwner), 6.0f);
                        }
                    }
                }
            }

            // Remove the fireball entity
            this.discard();
        }
    }
}