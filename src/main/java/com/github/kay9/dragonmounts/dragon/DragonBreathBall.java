package com.github.kay9.dragonmounts.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class DragonBreathBall extends LargeFireball {

    private final double startX;
    private final double startY;
    private final double startZ;

    public DragonBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz, int power) {
        super(level, shooter, dx, dy, dz, power);
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
        if (target instanceof DragonBreathBall) {
            return false;
        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();

        // Only set the entity on fire if it's not fire-immune
        if (!target.fireImmune()) {
            target.setSecondsOnFire(5); // 5 seconds of fire
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            // Check if owner is alive to prevent null pointer crashes
            if (owner != null) {
                boolean flag = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.5f, flag, Level.ExplosionInteraction.MOB);

                // Build immune list
                List<Entity> immuneEntities = List.of(Objects.requireNonNull(owner)); // owner (player or dragon)

                // Add passengers of owner (mounted case)
                immuneEntities = new java.util.ArrayList<>(immuneEntities);
                immuneEntities.addAll(owner.getPassengers());

                // Add the dragon itself if owner is a dragon riding entity (optional)
                if (owner instanceof LivingEntity) {
                    immuneEntities.add(owner);
                }

                // Add all nearby DragonBreathBall instances (to prevent chain fire)
                immuneEntities.add(this); // the fireball itself

                // Set entities caught in the explosion on fire AND damage them
                double radius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), e -> e != this);
                for (Entity entity : entities) {
                    if (!immuneEntities.contains(entity)) {
                        // Set on fire if possible
                        if (!entity.fireImmune()) {
                            entity.setSecondsOnFire(5);
                        }
                        // Deal damage to everyone not immune, even fire-immune mobs
                        if (owner instanceof LivingEntity livingOwner) {
                            entity.hurt(level().damageSources().mobProjectile(this, livingOwner), 6.0f);
                        }
                    }
                }

                // Set fire to blocks hit
                if (result.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockResult = (BlockHitResult) result;
                    BlockPos hitPos = blockResult.getBlockPos();

                    // Iterate through the hit block and its immediate neighbors (3x3x3 area or just 6 faces)
                    // For a "Breath" effect, checking the 6 cardinal directions is usually most efficient:
                    for (Direction direction : Direction.values()) {
                        BlockPos targetPos = hitPos.relative(direction);
                        BlockState targetState = level().getBlockState(targetPos);

                        // CASE 1: The block is already flammable (Wood, Leaves, etc.)
                        // We "consume" it instantly by replacing it with fire.
                        if (targetState.isFlammable(level(), targetPos, direction.getOpposite())) {
                            level().setBlockAndUpdate(targetPos, Blocks.FIRE.defaultBlockState());
                        }
                        // CASE 2: The block is air, but we want to set the "face" of the hit block on fire
                        else if (level().isEmptyBlock(targetPos)) {
                            BlockState hitState = level().getBlockState(hitPos);
                            if (hitState.isFlammable(level(), hitPos, direction.getOpposite())) {
                                level().setBlockAndUpdate(targetPos, Blocks.FIRE.defaultBlockState());
                            }
                        }
                    }

                    // Also try to replace the hit block itself if it's flammable
                    BlockState hitState = level().getBlockState(hitPos);
                    if (hitState.isFlammable(level(), hitPos, blockResult.getDirection())) {
                        level().setBlockAndUpdate(hitPos, Blocks.FIRE.defaultBlockState());
                    }
                }
            }

            // Remove the fireball entity
            this.discard();
        }
    }
}