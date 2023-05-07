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
    private static final BreathEnvironmentEffects EFFECTS = new BreathEnvironmentEffects();

    private static final int BASE_FIRE_SPREAD = 10;
    private static final int FIRE_SECONDS = 7;

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
        var direction = getShootDirection(shooter.getRandom(), shooter.getXRot(), shooter.getYHeadRot(), 6);
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

        var damage = getBaseDamage() * getIntensityScale();
        var entity = result.getEntity();

        if (entity.fireImmune()) damage *= 0.25;
        else entity.setSecondsOnFire((int) (FIRE_SECONDS * getIntensityScale()));

        hurtEntity(entity, getDamageSource(), damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult result)
    {
        super.onHitBlock(result);

        var dir = result.getDirection();
        var entityDir = Direction.getNearest(getMoveDirection().x(), getMoveDirection().y(), getMoveDirection().z());
        var directContact = entityDir.getOpposite() == dir;
        if (directContact) age += 1;

        if (getLevel().isClientSide()) return;
        if (!(getOwner() instanceof TameableDragon dragon) || !DMLConfig.canGrief(dragon)) return;

        var pos = result.getBlockPos();
        var state = getLevel().getBlockState(pos);
        var relative = pos.relative(dir);
        var raining = getLevel().isRainingAt(pos);

        if (BaseFireBlock.canBePlacedAt(getLevel(), relative, dir))
        {
            int fireChance = 2 + state.getFireSpreadSpeed(getLevel(), pos, dir);
            if (directContact) fireChance *= 1.25;
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
            var chance = random.nextDouble();
            if (directContact) chance *= 0.5;
            EFFECTS.affectEnvironment(getLevel(), pos, state, chance);
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
    public boolean displayFireAnimation()
    {
        return false;
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

    static
    {
        EFFECTS.registerBasicReplacer(Blocks.SAND, Blocks.GLASS.defaultBlockState(), 0.005);
        EFFECTS.registerBasicReplacer(Blocks.RED_SAND, Blocks.GLASS.defaultBlockState(), 0.005);
        EFFECTS.registerBasicReplacer(Blocks.SNOW_BLOCK, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 14), 0.05);
        EFFECTS.registerBasicReplacer(Blocks.SNOW, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2), 0.1);
        EFFECTS.registerBasicReplacer(Blocks.ICE, Blocks.WATER.defaultBlockState(), 0.15);
        EFFECTS.registerBasicReplacer(Blocks.PACKED_ICE, Blocks.WATER.defaultBlockState(), 0.075);
        EFFECTS.registerBasicReplacer(Blocks.BLUE_ICE, Blocks.WATER.defaultBlockState(), 0.01);
        EFFECTS.registerBasicReplacer(Blocks.WATER, Blocks.AIR.defaultBlockState(), 0.01);
        EFFECTS.registerBasicReplacer(Blocks.GRASS_BLOCK, Blocks.DIRT.defaultBlockState(), 0.1);
        EFFECTS.registerBasicReplacer(Blocks.STONE, Blocks.BLACKSTONE.defaultBlockState(), 0.01);
        EFFECTS.registerBasicReplacer(Blocks.COBBLESTONE, Blocks.BLACKSTONE.defaultBlockState(), 0.01);
        EFFECTS.registerBasicReplacer(Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK.defaultBlockState(), 0.001);
        EFFECTS.registerBasicReplacer(Blocks.MAGMA_BLOCK, Blocks.LAVA.defaultBlockState(), 0.0005);
    }
}
