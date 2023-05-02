package com.github.kay9.dragonmounts.entity.breath;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class IceBreathNode extends BreathNode
{
    private static final int DEFAULT_MAX_AGE = 50;
    private static final float DEFAULT_MAX_SPEED = 0.8f;
    private static final float DEFAULT_MAX_SIZE = 4f;

    public IceBreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    protected IceBreathNode(Entity shooter, Vec3 startPos, Vec3 direction)
    {
        super(DMLRegistry.ICE_BREATH.get(), shooter, startPos, direction);
    }

    public static IceBreathNode shoot(TameableDragon shooter)
    {
        var direction = getShootDirection(shooter.getRandom(), shooter.getXRot(), shooter.getYHeadRot(), 1);
        return new IceBreathNode(shooter, shooter.getMouthPos(), direction);
    }

    @Override
    public int variedMaxAge()
    {
        return DEFAULT_MAX_AGE + (random.nextInt(10) - 5);
    }

    @Override
    public float variedSpeed()
    {
        return DEFAULT_MAX_SPEED + (0.5f * random.nextFloat() - 0.25f);
    }

    @Override
    public float variedMaxSize()
    {
        return DEFAULT_MAX_SIZE + (0.5f * random.nextFloat() - 0.25f);
    }

    @Override
    protected float getCurrentSize()
    {
        return calculateLinearExpansion();
    }

    @Override
    public void tick()
    {
        if (isInLava())
        {
            melt();
            return;
        }

        super.tick();

        ageBySpeed();

        if (getLevel().isClientSide())
        {
            var motion = getDeltaMovement();
            for (int i = 0; i < 1; i++)
            {
                var x = getRandomX(0.2) + motion.x();
                var y = getRandomY() + motion.y();
                var z = getRandomZ(0.2) + motion.z();
                getLevel().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, (2 * random.nextDouble() - 1) * 0.1, 0.05, (2 * random.nextDouble() - 1) * 0.1);
            }
        }
    }

    public void melt()
    {
        if (getLevel().isClientSide())
        {
            if (random.nextDouble() <= 0.25) playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        }

        discard();
    }

    @Override
    public void expire()
    {
        if (getLevel().isClientSide())
        {
            getLevel().addDestroyBlockEffect(blockPosition(), Blocks.ICE.defaultBlockState());
            playSound(SoundEvents.GLASS_BREAK, 1f, 1);
        }
        super.expire();
    }

}
