package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    protected void onHitEntity(@NotNull EntityHitResult result) { super.onHitEntity(result); }

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

                        // This prevents the bolt from damaging the crew or setting fire
                        lightning.setVisualOnly(true);

                        // Link the lightning to the shooter (for death messages/advancements)
                        if (owner instanceof net.minecraft.server.level.ServerPlayer player) {
                            lightning.setCause(player);
                        }

                        // Add it to the world
                        this.level().addFreshEntity(lightning);

                        // MANUALLY TRIGGER VANILLA SECONDARY EFFECTS
                        if (result instanceof BlockHitResult blockResult) {
                            BlockPos hitPos = blockResult.getBlockPos();
                            int extraIgnitions = 4;

                            // Spawn fire only on the ground
                            this.spawnLightningFire(this.level(), hitPos, extraIgnitions, owner);

                            // Clean copper only on the ground
                            this.cleanCopper(this.level(), hitPos);
                        }

                        List<Entity> strikeTargets = this.level().getEntities(this, this.getBoundingBox().inflate(3.0D));
                        for (Entity strikeTarget : strikeTargets) {
                            if (strikeTarget instanceof LivingEntity livingTarget && !isPartOfDragonCrew(livingTarget, owner)) {
                                // This ensures they get the "Struck by Lightning" tag/logic without hitting the crew
                                if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(livingTarget, lightning)) {
                                    livingTarget.thunderHit((net.minecraft.server.level.ServerLevel)this.level(), lightning);
                                }
                            }
                        }
                    }

                    DragonBreathCloud cloud = new DragonBreathCloud(this.level(), this.getX(), this.getY(), this.getZ(), owner);
                    if (owner instanceof LivingEntity livingOwner) {
                        cloud.setOwner(livingOwner);
                    }

                    cloud.setParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK);
                    cloud.setRadius(2.0F);
                    cloud.setDuration(60); // 3 seconds
                    cloud.setRadiusPerTick((2.0F - cloud.getRadius()) / (float)cloud.getDuration());

                    this.level().addFreshEntity(cloud);
                }

                // Damage entities caught in the explosion
                double radius = 1.5; // slightly larger than the explosion to catch entities around
                List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), e -> e != this);
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingTarget) {
                        boolean isProtected = isPartOfDragonCrew(livingTarget, owner);
                        if (!isProtected) {
                            if (owner instanceof LivingEntity livingOwner) {
                                entity.hurt(level().damageSources().mobProjectile(this, livingOwner), DMLConfig.getBreathDamage());
                            }
                        }
                    }
                }
            }

            // Remove the storm ball entity
            this.discard();
        }
    }

    private void spawnLightningFire(Level level, BlockPos pos, int extraIgnitions, Entity owner) {
        boolean canGrief = ForgeEventFactory.getMobGriefingEvent(this.level(), owner);
        boolean fireTicks = this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);

        if (fireTicks && canGrief) {
            // Try to ignite the hit block
            BlockState fireState = net.minecraft.world.level.block.BaseFireBlock.getState(level, pos);
            if (level.getBlockState(pos).isAir() && fireState.canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, fireState);
            }

            // Try to ignite neighbors (the extra sparks)
            for (int i = 0; i < extraIgnitions; ++i) {
                BlockPos randomPos = pos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                fireState = net.minecraft.world.level.block.BaseFireBlock.getState(level, randomPos);
                if (level.getBlockState(randomPos).isAir() && fireState.canSurvive(level, randomPos)) {
                    level.setBlockAndUpdate(randomPos, fireState);
                }
            }
        }
    }

    private void cleanCopper(Level level, BlockPos pos) {
        // This is essentially a manual call to the vanilla logic
        // It's a bit complex to rewrite, but hitting a Lightning Rod or Copper with fire handles most visual cases.
        BlockState state = level.getBlockState(pos);
        if (state.is(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD)) {
            ((net.minecraft.world.level.block.LightningRodBlock)state.getBlock()).onLightningStrike(state, level, pos);
        }
        // Vanilla also cleans oxidation from copper in a small area
        if (state.getBlock() instanceof net.minecraft.world.level.block.WeatheringCopper) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.WeatheringCopper.getFirst(state));
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