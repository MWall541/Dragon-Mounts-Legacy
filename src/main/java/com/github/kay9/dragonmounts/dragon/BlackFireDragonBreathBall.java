package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlackFireDragonBreathBall extends LargeFireball {

    private double startX;
    private double startY;
    private double startZ;

    // Constructor for the Registry and Loading from NBT
    public BlackFireDragonBreathBall(EntityType<? extends BlackFireDragonBreathBall> type, Level level) {
        super(type, level);
        // These might be 0 until the entity is actually spawned
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
    }

    // Constructor for Dragon to use
    public BlackFireDragonBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz, int power) {
        // Call our OWN first constructor using the Registry Type
        this(DMLRegistry.BLACK_FIRE_BREATH.get(), level);

        // Manually set the owner (shooter)
        this.setOwner(shooter);

        // Manually set starting position to the shooter
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        this.reapplyPosition();

        this.setDeltaMovement(dx * 1.5D, dy * 1.5D, dz * 1.5D);

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
        double maxRange = DMLConfig.getBreathRange();
        double distSq = this.distanceToSqr(startX, startY, startZ);
        if (distSq > (maxRange * maxRange)) {
            this.onHit(new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @NotNull ParticleOptions getTrailParticle() {
        return ParticleTypes.SCULK_SOUL;
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
        if (target instanceof BlackFireDragonBreathBall) {
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
            if (owner != null) {
                boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);

                // 1. Initial Explosion
                // this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.5f, canGrief, Level.ExplosionInteraction.MOB);

                // 2. Handle Entity Damage & Ignite
                this.applyAreaEffectDamage(owner);

                // 3. Handle Block Interactions (Ignition, TNT, etc.)
                if (result instanceof BlockHitResult blockResult) {
                    this.applyAreaBlockEffects(blockResult, owner, canGrief);
                }
            }
            this.discard();
        }
    }

    /**
     * Finds and damages entities within the blast radius, protecting the 'Dragon Crew'.
     */
    private void applyAreaEffectDamage(Entity owner) {
        double blastRadius = 1.5;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(blastRadius), e -> e != this);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                if (owner instanceof LivingEntity livingOwner) {
                    entity.hurt(level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                    if (!entity.fireImmune()) {
                        entity.setSecondsOnFire(5);
                    }
                }
            }
        }
    }

    /**
     * Handles lighting campfires, candles, TNT, and spreading fire to air blocks near surfaces.
     */
    private void applyAreaBlockEffects(BlockHitResult blockResult, Entity owner, boolean canGrief) {
        BlockPos hitPos = blockResult.getBlockPos();
        boolean fireTicks = this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);

        // CASE A: Logic for specific blocks (Lamps, TNT, Campfires)
        int radius = 1;
        for (BlockPos targetPos : BlockPos.betweenClosed(hitPos.offset(-radius, -radius, -radius), hitPos.offset(radius, radius, radius))) {
            BlockState targetState = level().getBlockState(targetPos);

            // Light campfires/candles/lamps WITHOUT replacing the block
            if (targetState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                if (!targetState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                    level().setBlockAndUpdate(targetPos, targetState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true));
                }
            }
            // Ignite TNT
            else if (targetState.is(Blocks.TNT)) {
                LivingEntity igniter = (owner instanceof LivingEntity) ? (LivingEntity) owner : null;
                targetState.getBlock().onCaughtFire(targetState, this.level(), targetPos, blockResult.getDirection(), igniter);
                this.level().removeBlock(targetPos, false);
            }
        }

        // CASE B: Spreading Fire to AIR blocks only
        if (fireTicks && canGrief) {
            for (Direction direction : Direction.values()) {
                BlockPos sidePos = hitPos.relative(direction);

                if (level().isEmptyBlock(sidePos)) {
                    // LOOK DOWN: Check the block beneath the potential fire
                    BlockState baseBlockState = level().getBlockState(sidePos.below());

                    // CHECK TAG: Does this block belong to the #minecraft:soul_fire_base_blocks tag?
                    boolean startsSoulFire = baseBlockState.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);

                    // SELECT TYPE: Pick Soul Fire if the base is valid, otherwise use normal Fire
                    BlockState fireToPlace = startsSoulFire ? Blocks.SOUL_FIRE.defaultBlockState() : Blocks.FIRE.defaultBlockState();

                    // VALIDATE: Ensure the fire can actually exist there (standard Minecraft physics)
                    boolean isFlammable = level().getBlockState(hitPos).isFlammable(level(), hitPos, direction.getOpposite());
                    boolean canSurvive = fireToPlace.canSurvive(level(), sidePos);

                    if (isFlammable || canSurvive) {
                        level().setBlockAndUpdate(sidePos, fireToPlace);
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
}