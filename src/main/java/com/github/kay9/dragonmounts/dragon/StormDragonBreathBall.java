package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StormDragonBreathBall extends LargeFireball {

    private double startX;
    private double startY;
    private double startZ;

    // Constructor for the Registry and Loading from NBT
    public StormDragonBreathBall(EntityType<? extends StormDragonBreathBall> type, Level level) {
        super(type, level);
        // These might be 0 until the entity is actually spawned
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
    }

    // Constructor for Dragon to use
    public StormDragonBreathBall(Level level, LivingEntity shooter, double dx, double dy, double dz, int power) {
        // Call our OWN first constructor using the Registry Type
        this(DMLRegistry.STORM_BREATH.get(), level);

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
        return ParticleTypes.ELECTRIC_SPARK;
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
    protected void onHitEntity(@NotNull EntityHitResult result) { super.onHitEntity(result); }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner != null) {
                // 1. Roll for the 10% Storm Event (Lightning + Cloud)
                if (this.random.nextFloat() < 0.10f) {
                    this.triggerStormEvent(result, owner);
                }

                // 2. Handle standard projectile damage
                this.applyAreaEffectDamage(owner);
            }
            this.discard();
        }
    }

    /**
     * Handles the creation of the lightning bolt, secondary lightning effects, and the static cloud.
     */
    private void triggerStormEvent(HitResult result, Entity owner) {
        net.minecraft.world.entity.LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
        if (lightning != null) {
            lightning.moveTo(this.getX(), this.getY(), this.getZ());
            lightning.setVisualOnly(true);

            if (owner instanceof net.minecraft.server.level.ServerPlayer player) {
                lightning.setCause(player);
            }

            this.level().addFreshEntity(lightning);

            // Handle Block-based lightning logic
            if (result instanceof BlockHitResult blockResult) {
                BlockPos hitPos = blockResult.getBlockPos();
                this.spawnLightningFire(this.level(), hitPos, owner);
                this.onCopperHit(this.level(), hitPos);
            }

            // Handle Entity-based lightning strikes
            this.applyLightningStrikeToEntities(lightning, owner);
        }

        // Spawn the lingering static cloud
        this.spawnStormCloud(owner);
    }

    /**
     * Specifically handles the "thunderHit" logic for entities near the strike point.
     */
    private void applyLightningStrikeToEntities(net.minecraft.world.entity.LightningBolt lightning, Entity owner) {
        List<Entity> strikeTargets = this.level().getEntities(this, this.getBoundingBox().inflate(3.0D));
        for (Entity strikeTarget : strikeTargets) {
            if (strikeTarget instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                if (!ForgeEventFactory.onEntityStruckByLightning(livingTarget, lightning)) {
                    livingTarget.thunderHit((net.minecraft.server.level.ServerLevel)this.level(), lightning);
                }
            }
        }
    }

    /**
     * Creates the AreaEffectCloud that slows down enemies with electric sparks.
     */
    private void spawnStormCloud(Entity owner) {
        DragonBreathCloud cloud = new DragonBreathCloud(this.level(), this.getX(), this.getY(), this.getZ(), owner);
        if (owner instanceof LivingEntity livingOwner) {
            cloud.setOwner(livingOwner);
        }

        cloud.setParticle(ParticleTypes.ELECTRIC_SPARK);
        cloud.setRadius(2.0F);
        cloud.setDuration(60);
        cloud.setRadiusPerTick((2.0F - cloud.getRadius()) / (float)cloud.getDuration());

        this.level().addFreshEntity(cloud);
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
                    entity.hurt(this.level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                }
            }
        }
    }

    private void spawnLightningFire(Level level, BlockPos pos, Entity owner) {
        boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
        boolean fireTicks = this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);

        if (fireTicks && canGrief) {
            // Try to ignite the hit block
            BlockState fireState = net.minecraft.world.level.block.BaseFireBlock.getState(level, pos);
            if (level.getBlockState(pos).isAir() && fireState.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, fireState);
            }

            // Try to ignite neighbors (the extra sparks)
            for (int i = 0; i < 4; ++i) {
                BlockPos randomPos = pos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                fireState = net.minecraft.world.level.block.BaseFireBlock.getState(level, randomPos);
                if (level.getBlockState(randomPos).isAir() && fireState.canSurvive(level, randomPos)) {
                    level.setBlockAndUpdate(randomPos, fireState);
                }
            }
        }
    }

    private void onCopperHit(Level level, BlockPos pos) {
        // Define the 3x3x3 area (radius of 1 around the hit position)
        int radius = 1;
        for (BlockPos targetPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(targetPos);

            // 1. Handle Lightning Rods (Powering + Visuals)
            if (state.is(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD)) {
                ((net.minecraft.world.level.block.LightningRodBlock) state.getBlock()).onLightningStrike(state, level, targetPos);
            }

            // 2. Handle Weathering Copper (Cleaning Oxidation)
            // This checks if the block is any form of copper that can oxidize (stairs, slabs, blocks, etc.)
            if (state.getBlock() instanceof net.minecraft.world.level.block.WeatheringCopper) {
                // getFirst() returns the "Unaffected" (clean) version of that specific copper block
                level.setBlockAndUpdate(targetPos, net.minecraft.world.level.block.WeatheringCopper.getFirst(state));

                // Add a small visual "clean" particle effect
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            5, 0.2, 0.2, 0.2, 0.05);
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
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
                        }
                    }
                }
            }
        }
    }
}