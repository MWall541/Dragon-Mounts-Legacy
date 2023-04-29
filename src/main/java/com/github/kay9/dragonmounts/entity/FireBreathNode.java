package com.github.kay9.dragonmounts.entity;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
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
    private static final Map<BlockState, ScorchResult> SCORCH_RESULTS = new HashMap<>(); //todo: eventually make this data-driven?

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
        var mouthPos = shooter.getMouthPos();
        var innacX = 2 * shooter.getRandom().nextFloat() - 1;
        var innacY = 2 * shooter.getRandom().nextFloat() - 1;
        var direction = Vec3.directionFromRotation(shooter.getXRot() + innacX, shooter.getYHeadRot() + innacY);
        return new FireBreathNode(shooter, mouthPos, direction);
    }

    @Override
    public void tick()
    {
        if (isInWater())
        {
            extinguish();
            return;
        }

        // rain puts out fire quicker
        if (getLevel().isRainingAt(blockPosition())) age += 1;

        super.tick();

        if (getLevel().isClientSide())
        {
            var motion = getDeltaMovement();
            var particle = ParticleTypes.FLAME;
            for (int i = 0; i < 2; i++)
            {
                var x = getRandomX(0.2) + motion.x();
                var y = getRandomY() + motion.y();
                var z = getRandomZ(0.2) + motion.z();
                level.addParticle(particle, x, y, z, 0, 0, 0);
                particle = ParticleTypes.SMOKE;
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

        var entityDir = getDirection();
        var directContact = result.getDirection() == entityDir.getOpposite();
        if (directContact) age += 1;

        if (!(getOwner() instanceof TameableDragon dragon) || !DMLConfig.canGrief(dragon)) return;

        var pos = result.getBlockPos();
        var dir = result.getDirection();
        var state = getLevel().getBlockState(pos);
        var relative = pos.relative(dir);
        var raining = getLevel().isRainingAt(pos);

        if (BaseFireBlock.canBePlacedAt(getLevel(), relative, entityDir))
        {
            int fireChance = state.getFireSpreadSpeed(getLevel(), pos, dir);
            if (directContact) fireChance *= 1.35;
            if (raining) fireChance *= 0.5;
            fireChance *= getIntensityScale();
            fireChance *= DMLConfig.getFireSpreadMultiplier();

            if (random.nextInt(100) < fireChance)
                getLevel().setBlock(relative, BaseFireBlock.getState(getLevel(), relative), Block.UPDATE_ALL_IMMEDIATE);
        }

        if (!directContact) return;

        var burn = state.getFlammability(getLevel(), pos, dir);
        if (raining) burn *= 0.5;
        burn *= getIntensityScale();
        burn *= DMLConfig.getBreathBurnMultiplier();

        if (burn > 0 && random.nextInt(60) < burn)
            getLevel().removeBlock(pos, false); // Fire gets hot enough and incinerates its path.
        else
        {
            var scorched = getScorchedResult(state);
            if (scorched != null) getLevel().setBlock(pos, scorched, Block.UPDATE_ALL); // unburnable blocks get scorched instead.
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

    @Nullable
    private BlockState getScorchedResult(BlockState previous)
    {
        var result = SCORCH_RESULTS.get(previous);
        if (result != null)
        {
            var chance = result.chance() * DMLConfig.getBreathBurnMultiplier();
            if (random.nextDouble() < chance) return result.result();
        }
        return null;
    }

    private void extinguish()
    {
        discard();
        if (random.nextDouble() <= 0.25d) playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        for (int i = 0; i < 15; i++)
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
    private record ScorchResult(double chance, BlockState result) {}

    public static void registerScorchResult(BlockState from, double chance, BlockState result)
    {
        SCORCH_RESULTS.put(from, new ScorchResult(chance, result));
    }

    static
    {
        registerScorchResult(Blocks.SNOW_BLOCK.defaultBlockState(), 0.175, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2));
        registerScorchResult(Blocks.SNOW.defaultBlockState(), 0.3, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 8));
        registerScorchResult(Blocks.ICE.defaultBlockState(), 0.15, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.PACKED_ICE.defaultBlockState(), 0.075, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.BLUE_ICE.defaultBlockState(), 0.01, Blocks.WATER.defaultBlockState());
        registerScorchResult(Blocks.WATER.defaultBlockState(), 0.05, Blocks.AIR.defaultBlockState());
        registerScorchResult(Blocks.GRASS.defaultBlockState(), 0.25, Blocks.DIRT.defaultBlockState());
        registerScorchResult(Blocks.STONE.defaultBlockState(), 0.05, Blocks.BLACKSTONE.defaultBlockState());
        registerScorchResult(Blocks.COBBLESTONE.defaultBlockState(), 0.05, Blocks.BLACKSTONE.defaultBlockState());
        registerScorchResult(Blocks.BLACKSTONE.defaultBlockState(), 0.015, Blocks.MAGMA_BLOCK.defaultBlockState());
        registerScorchResult(Blocks.MAGMA_BLOCK.defaultBlockState(), 0.001, Blocks.LAVA.defaultBlockState());
    }
}
