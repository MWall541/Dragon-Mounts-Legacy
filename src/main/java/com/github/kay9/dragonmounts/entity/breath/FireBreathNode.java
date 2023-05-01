package com.github.kay9.dragonmounts.entity.breath;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FireBreathNode extends BreathNode
{
    private static final Map<Block, ScorchResult> SCORCH_RESULTS = new HashMap<>(); //todo: eventually make this data-driven?

    public FireBreathNode(EntityType<? extends FireBreathNode> type, Level level)
    {
        super(type, level);
    }

    protected FireBreathNode(TameableDragon shooter, Vec3 startPos, Vec3 direction)
    {
        super(DMLRegistry.FIRE_BREATH.get(), shooter, startPos, direction);
    }

    public static FireBreathNode shoot(TameableDragon shooter)
    {
        var direction = getShootDirection(shooter.getRandom(), shooter.getXRot(), shooter.getYHeadRot(), 1);
        return new FireBreathNode(shooter, shooter.getMouthPos(), direction);
    }

    @Override
    public void tick()
    {
        if (isInWater())
        {
            extinguish();
            return;
        }

        super.tick();

        // rain puts out fire quicker
        if (getLevel().isRainingAt(blockPosition())) age += 1;

        ageBySpeed();

        if (getLevel().isClientSide())
        {
            var motion = getDeltaMovement();
            for (var particle : new ParticleOptions[] {ParticleTypes.FLAME, ParticleTypes.SMOKE})
            {
                var x = getRandomX(0.2) + motion.x();
                var y = getRandomY() + motion.y();
                var z = getRandomZ(0.2) + motion.z();
                level.addParticle(particle, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);

        if (getLevel().isClientSide()) return;

        var damage = (float) TameableDragon.BASE_DAMAGE;
        TameableDragon dragon = null;
        if (getOwner() instanceof TameableDragon shooter)
        {
            damage = (float) shooter.getAttributeValue(Attributes.ATTACK_DAMAGE);
            dragon = shooter;
        }

        var entity = result.getEntity();
        if (entity.fireImmune()) damage *= 0.25;
        else entity.setSecondsOnFire((int) (7 * getIntensityScale()));

        if (entity.hurt(getDamageSource(), damage) && dragon != null)
            doEnchantDamageEffects(dragon, entity);
    }

    @Override
    protected void onHitBlock(BlockHitResult result)
    {
        super.onHitBlock(result);

        if (getLevel().isClientSide()) return;

        var dir = result.getDirection();
        var entityDir = Direction.getNearest(getMoveDirection().x(), getMoveDirection().y(), getMoveDirection().z());
        var directContact = entityDir.getOpposite() == dir;
        if (directContact) age += 1;

        if (!(getOwner() instanceof TameableDragon dragon) || !DMLConfig.canGrief(dragon)) return;

        var pos = result.getBlockPos();
        var state = getLevel().getBlockState(pos);
        var relative = pos.relative(dir);
        var raining = getLevel().isRainingAt(pos);

        if (BaseFireBlock.canBePlacedAt(getLevel(), relative, dir))
        {
            int fireChance = 5 + state.getFireSpreadSpeed(getLevel(), pos, dir);
            if (directContact) fireChance *= 1.35;
            if (raining) fireChance *= 0.5;
            fireChance *= getIntensityScale();
            fireChance *= DMLConfig.getFireSpreadMultiplier();

            if (random.nextInt(100) < fireChance)
                getLevel().setBlockAndUpdate(relative, BaseFireBlock.getState(getLevel(), relative));
        }

        var burn = state.getFlammability(getLevel(), pos, dir);
        if (directContact && burn > 0)
        {
            if (raining) burn *= 0.5;
            burn *= getIntensityScale();
            burn *= DMLConfig.getBreathBurnMultiplier();
            if (random.nextInt(100) < burn)
                getLevel().removeBlock(pos, false); // Fire gets hot enough and incinerates its path.
        }
        else
        {
            var scorched = getScorchedResult(state, directContact);
            if (scorched != null)
                getLevel().setBlockAndUpdate(pos, scorched); // unburnable blocks get scorched instead.
        }
    }

    @Override
    public void contactMethod()
    {
        touch();
    }

    @Override
    public boolean isOnFire()
    {
        return true;
    }

    @Override
    public float getBrightness()
    {
        return 1f;
    }

    @Override
    public void expire()
    {
        if (getLevel().isClientSide()) burnOutParticles();
        super.expire();
    }

    private void extinguish()
    {
        if (!getLevel().isClientSide()) discard();
        if (random.nextDouble() <= 0.25) playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        burnOutParticles();
    }

    private void burnOutParticles()
    {
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getRandomX(0.75), getY() + getBbHeight(), getRandomZ(0.75), 0, 0.05, 0);
        level.addParticle(ParticleTypes.LAVA, getRandomX(0.75), getY() + getBbHeight(), getRandomZ(0.75), 0, 0, 0);
        for (int i = 0; i < 10; i++)
            level.addParticle(ParticleTypes.SMOKE, getRandomX(1), getRandomY(), getRandomZ(1), 0, random.nextDouble() * 0.08f, 0);
    }

    private DamageSource getDamageSource()
    {
        var shooter = getOwner();
        var owned = shooter != null;
        var name = owned? "fireBreath" : "onFire";
        if (!owned) shooter = this;
        return new IndirectEntityDamageSource(name, this, shooter).setIsFire().setProjectile();
    }

    @Nullable
    private BlockState getScorchedResult(BlockState previous, boolean directContact)
    {
        var result = SCORCH_RESULTS.get(previous.getBlock());
        if (result != null)
        {
            var chance = result.chance() * DMLConfig.getBreathBurnMultiplier();
            if (!directContact) chance *= 0.25f;
            if (random.nextDouble() < chance) return result.result();
        }
        return null;
    }

    private record ScorchResult(double chance, BlockState result) {}

    public static void registerScorchResult(Block from, double chance, BlockState result)
    {
        SCORCH_RESULTS.put(from, new ScorchResult(chance, result));
    }

    static
    {
        registerScorchResult(Blocks.SAND, 0.005, Blocks.GLASS.defaultBlockState());
        registerScorchResult(Blocks.RED_SAND, 0.005, Blocks.GLASS.defaultBlockState());
        registerScorchResult(Blocks.SNOW_BLOCK, 0.05, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 14));
        registerScorchResult(Blocks.SNOW, 0.1, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
        registerScorchResult(Blocks.ICE, 0.15, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.PACKED_ICE, 0.075, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.BLUE_ICE, 0.01, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.WATER, 0.01, Blocks.AIR.defaultBlockState());
        registerScorchResult(Blocks.GRASS_BLOCK, 0.1, Blocks.DIRT.defaultBlockState());
        registerScorchResult(Blocks.STONE, 0.01, Blocks.BLACKSTONE.defaultBlockState());
        registerScorchResult(Blocks.COBBLESTONE, 0.01, Blocks.BLACKSTONE.defaultBlockState());
        registerScorchResult(Blocks.BLACKSTONE, 0.001, Blocks.MAGMA_BLOCK.defaultBlockState());
        registerScorchResult(Blocks.MAGMA_BLOCK, 0.0005, Blocks.LAVA.defaultBlockState());
    }
}
