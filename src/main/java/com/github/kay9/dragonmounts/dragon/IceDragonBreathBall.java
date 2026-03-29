package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IceDragonBreathBall extends LargeFireball {

    private double startX;
    private double startY;
    private double startZ;

    // Constructor for the Registry and Loading from NBT
    public IceDragonBreathBall(EntityType<? extends IceDragonBreathBall> type, Level level) {
        super(type, level);
        // These might be 0 until the entity is actually spawned
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
    }

    // Constructor for Dragon to use
    public IceDragonBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz, int power) {
        // Call our OWN first constructor using the Registry Type
        this(DMLRegistry.ICE_BREATH.get(), level);

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
        // FLUID DETECTION: LargeFireball normally ignores water.
        // We perform a manual clip to catch water surfaces.
        if (!this.level().isClientSide) {
            Vec3 pos = this.position();
            Vec3 nextPos = pos.add(this.getDeltaMovement());
            BlockHitResult fluidHit = this.level().clip(new net.minecraft.world.level.ClipContext(
                    pos, nextPos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.ANY, this));

            if (fluidHit.getType() != HitResult.Type.MISS) {
                this.onHit(fluidHit);
            }
        }

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
        return ParticleTypes.SNOWFLAKE;
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
                // 1. Roll for 10% Cloud Event
                if (this.random.nextFloat() < 0.10f) {
                    this.spawnIceCloud(owner);
                }

                // 2. Handle Entity Freeze & Damage
                this.applyAreaEffectDamage(owner);

                // 3. Handle Block/Fluid Transformations
                if (result instanceof BlockHitResult blockResult) {
                    this.applyAreaBlockEffects(blockResult);
                }
            }

            // Remove the iceball entity
            this.discard();
        }
    }

    private void spawnIceCloud(Entity owner) {
        DragonBreathCloud cloud = new DragonBreathCloud(this.level(), this.getX(), this.getY(), this.getZ(), owner);
        if (owner instanceof LivingEntity livingOwner) cloud.setOwner(livingOwner);

        cloud.setParticle(ParticleTypes.SNOWFLAKE);
        cloud.setRadius(2.0F);
        cloud.setDuration(60);
        cloud.setRadiusPerTick((2.0F - cloud.getRadius()) / 60.0F);
        this.level().addFreshEntity(cloud);
    }

    private void applyAreaEffectDamage(Entity owner) {
        double radius = 1.5;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), e -> e != this);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                if (owner instanceof LivingEntity livingOwner) {
                    entity.hurt(this.level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                    if (entity.canFreeze()) {
                        entity.setTicksFrozen(560);
                    }
                }
            }
        }
    }

    private void applyAreaBlockEffects(BlockHitResult blockResult) {
        BlockPos hitPos = blockResult.getBlockPos();
        int radius = 1;

        // Iterate in a 3x3x3 area around the impact
        for (BlockPos targetPos : BlockPos.betweenClosed(hitPos.offset(-radius, -radius, -radius), hitPos.offset(radius, radius, radius))) {
            BlockState state = this.level().getBlockState(targetPos);

            // 1. Transform Fluids (Water to Ice, Lava to Obsidian/Cobble)
            this.processFluidConversion(targetPos);

            // 2. Extinguish Fire
            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                this.level().removeBlock(targetPos, false);
                this.playExtinguishSound(targetPos);
            }

            // 3. Un-light blocks (Campfires, Candles, etc.)
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                this.level().setBlockAndUpdate(targetPos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, false));
                this.playExtinguishSound(targetPos);
            }
        }

        // Also check the projectile's exact current position just in case it's inside a fluid
        this.processFluidConversion(this.blockPosition());
    }

    private void playExtinguishSound(BlockPos pos) {
        this.level().playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.8F);
    }

    private void processFluidConversion(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        if (!fluidState.isEmpty()) {
            if (fluidState.is(FluidTags.WATER)) {
                this.level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                this.level().levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(Blocks.ICE.defaultBlockState()));
            } else if (fluidState.is(FluidTags.LAVA)) {
                BlockState newState = fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
                this.level().setBlockAndUpdate(pos, newState);
                this.level().playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
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