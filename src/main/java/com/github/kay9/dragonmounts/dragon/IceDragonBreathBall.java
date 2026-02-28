package com.github.kay9.dragonmounts.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IceDragonBreathBall extends LlamaSpit {

    private final double startX;
    private final double startY;
    private final double startZ;

    public IceDragonBreathBall(Level level, LivingEntity shooter) {
        super(net.minecraft.world.entity.EntityType.LLAMA_SPIT, level);
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
        if (target instanceof IceDragonBreathBall) {
            return false;
        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        if (!isPartOfDragonCrew(target, owner)) {
            // Only freeze the entity if it's not freeze-immune
            if (target.canFreeze()) {
                target.setTicksFrozen(560);
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            // Check if owner is alive to prevent null pointer crashes
            if (owner != null) {

                // 10% chance to summon a cloud
                if (this.random.nextFloat() < 0.10f) {
                    DragonBreathCloud cloud = new DragonBreathCloud(this.level(), this.getX(), this.getY(), this.getZ(), owner);
                    if (owner instanceof LivingEntity livingOwner) {
                        cloud.setOwner(livingOwner);
                    }

                    cloud.setParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE);
                    cloud.setRadius(2.0F);
                    cloud.setDuration(60); // 3 seconds
                    cloud.setRadiusPerTick((2.0F - cloud.getRadius()) / (float)cloud.getDuration());

                    this.level().addFreshEntity(cloud);
                }

                // Extinguish fire in a cubic area
                if (result instanceof BlockHitResult blockResult) {
                    BlockPos hitPos = blockResult.getBlockPos();

                    // Iterate in a small 3x3x3 area around the impact
                    int radius = 1;
                    for (BlockPos targetPos : BlockPos.betweenClosed(hitPos.offset(-radius, -radius, -radius), hitPos.offset(radius, radius, radius))) {
                        BlockState state = this.level().getBlockState(targetPos);

                        // Check for vanilla fire or soul fire
                        if (state.is(net.minecraft.world.level.block.Blocks.FIRE) || state.is(net.minecraft.world.level.block.Blocks.SOUL_FIRE)) {
                            this.level().removeBlock(targetPos, false);
                            // Path fixed: net.minecraft.sounds.SoundSource
                            this.level().playSound(null, targetPos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 2.6F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.8F);
                        }

                        // Make blocks like campfire, candles, lamps, etc unlit
                        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                            this.level().setBlockAndUpdate(targetPos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, false));
                            this.level().playSound(null, targetPos, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 2.6F);
                        }
                    }
                }

                boolean flag = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.0f, flag, Level.ExplosionInteraction.NONE);

                // Damage entities caught in the explosion
                double radius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), e -> e != this);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingTarget) {
                        boolean isProtected = isPartOfDragonCrew(livingTarget, owner);
                        if (!isProtected) {
                            if (owner instanceof LivingEntity livingOwner) {
                                entity.hurt(level().damageSources().mobProjectile(this, livingOwner), 6.0f);
                                // apply freeze effect to the entity if possible
                                if (entity.canFreeze()) {
                                    entity.setTicksFrozen(560);
                                }
                            }
                        }
                        // else {
                            // // FORCE clear freezing for the dragon crew just in case the explosion or vanilla logic tries to tick it up
                            // entity.setTicksFrozen(0);
                        // }
                    }
                }
            }

            // Remove the iceball entity
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

    public static class DragonBreathCloud extends AreaEffectCloud {
        private final Entity dragonOwner;

        public DragonBreathCloud(Level level, double x, double y, double z, Entity owner) {
            super(level, x, y, z);
            this.dragonOwner = owner;
        }

        @Override
        public void tick() {
            // Run base entity logic (movement, etc)
            super.baseTick();

            if (this.level().isClientSide) {
                super.tick(); // Allow particles to render normally
                return;
            }

            if (this.tickCount >= this.getWaitTime() + this.getDuration()) {
                this.discard();
                return;
            }

            // Handle Radius growth/shrink
            float currentRadius = this.getRadius();
            if (this.getRadiusPerTick() != 0.0F) {
                currentRadius += this.getRadiusPerTick();
                if (currentRadius < 0.5F) {
                    this.discard();
                    return;
                }
                this.setRadius(currentRadius);
            }

            // Application logic (every 5 ticks)
            if (this.tickCount % 5 == 0) {
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());

                for (LivingEntity target : targets) {
                    if (isPartOfDragonCrew(target, this.dragonOwner)) {
                        continue;
                    }

                    if (target.isAffectedByPotions()) {
                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        double distSq = dx * dx + dz * dz;

                        if (distSq <= (double) (currentRadius * currentRadius)) {
                            // Apply effects manually to valid targets
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
                        }
                    }
                }
            }
        }
    }
}