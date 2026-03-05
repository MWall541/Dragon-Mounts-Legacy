package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
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
        Entity owner = this.getOwner();

        if (!isPartOfDragonCrew(target, owner)) {
            // Only set the entity on fire if it's not fire-immune
            if (!target.fireImmune()) {
                target.setSecondsOnFire(5);
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            // Check if owner is alive to prevent null pointer crashes
            if (owner != null) {
                boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
                boolean fireTicks = this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);

                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.5f, canGrief, Level.ExplosionInteraction.MOB);

                // Set entities caught in the explosion on fire AND damage them
                double blastRadius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(blastRadius), e -> e != this);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingTarget) {
                        boolean isProtected = isPartOfDragonCrew(livingTarget, owner);
                        if (!isProtected) {
                            if (owner instanceof LivingEntity livingOwner) {
                                // Deal damage to everyone not immune, even fire-immune mobs
                                entity.hurt(level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                                // set on fire if possible
                                if (!entity.fireImmune()) {
                                    entity.setSecondsOnFire(5);
                                }
                            }
                        }
                    }
                }

                if (result instanceof BlockHitResult blockResult) {
                    BlockPos hitPos = blockResult.getBlockPos();
                    BlockState hitState = level().getBlockState(hitPos);

                    // Define the blast radius for lighting up blocks (1 = 3x3x3 area), this is different from the explosion radius above
                    int radius = 1;
                    for (BlockPos targetPos : BlockPos.betweenClosed(hitPos.offset(-radius, -radius, -radius), hitPos.offset(radius, radius, radius))) {
                        BlockState targetState = level().getBlockState(targetPos);

                        // Ignite Special Blocks (Campfires, Candles, etc.) in the area
                        if (targetState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                                && !targetState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                            level().setBlockAndUpdate(targetPos, targetState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true));
                        }

                        // TNT Special Case in the area
                        else if (targetState.is(net.minecraft.world.level.block.Blocks.TNT)) {
                            LivingEntity igniter = (owner instanceof LivingEntity) ? (LivingEntity) owner : null;
                            (targetState.getBlock()).onCaughtFire(targetState, this.level(), targetPos, blockResult.getDirection(), igniter);
                            this.level().removeBlock(targetPos, false);
                        }
                    }

                    // Only spawn fire on blocks if the world allows fire to tick/spread
                    if (fireTicks && canGrief) {
                        if (result.getType() == HitResult.Type.BLOCK) {
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
                                    if (hitState.isFlammable(level(), hitPos, direction.getOpposite())) {
                                        level().setBlockAndUpdate(targetPos, Blocks.FIRE.defaultBlockState());
                                    }
                                }
                            }

                            // Also try to replace the hit block itself if it's flammable
                            if (hitState.isFlammable(level(), hitPos, blockResult.getDirection())) {
                                level().setBlockAndUpdate(hitPos, Blocks.FIRE.defaultBlockState());
                            }
                        }
                    }
                }
            }

            // Remove the fireball entity
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
}