package com.github.kay9.dragonmounts.entity;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// quick notes: Weapon nodes should be heavily affected by the dragons scale.
// Dragons have dynamic growth, as well as possible custom scales.
// Damage, grief, and other effects should reflect this.
public class BreathNode extends AbstractHurtingProjectile
{
    public static final float DEFAULT_MAX_SIZE = 2f;
    public static final int DEFAULT_MAX_AGE = 40;
    public static final float DEFAULT_SPEED = 1.2f;

    private final float maxSize = variedMaxSize();
    private final float maxAge = variedMaxAge();
    private final float speed = variedSpeed();

    private float intensityScale = 1f;
    private int age = 0;

    public BreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    public BreathNode(EntityType<? extends AbstractHurtingProjectile> type, double xPos, double yPos, double zPos, double xMove, double yMove, double zMove, Level level)
    {
        super(type, xPos, yPos, zPos, xMove, yMove, zMove, level);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (age++ > getMaxAge())
        {
            expire();
            return;
        }

        refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");
    }

    @Override
    protected boolean canHitEntity(Entity entity)
    {
        if (entity == this) return false;
        if (getOwner() != null && getOwner().isAlliedTo(entity)) return false; // todo: do we want this?
        return super.canHitEntity(entity);
    }

    @Override
    public boolean hurt(DamageSource p_36839_, float p_36840_)
    {
        return false;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public void setOwner(@Nullable Entity entity)
    {
        super.setOwner(entity);
        intensityScale = variedIntensityScale();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        var size = calculateCurrentSize();
        return EntityDimensions.scalable(size, size);
    }

    public void expire()
    {
        discard();
    }

    public float variedMaxSize()
    {
        return DEFAULT_MAX_SIZE + (0.5f * random.nextFloat() - 0.25f);
    }

    public float variedMaxAge()
    {
        return DEFAULT_MAX_AGE + (0.5f * random.nextFloat() - 0.25f);
    }

    public float variedSpeed()
    {
        return 0.8f;
    }

    @SuppressWarnings("ConstantConditions")
    public float variedIntensityScale()
    {
        if (getOwner() instanceof TameableDragon dragon)
            return dragon.getScale() * (2.2f * random.nextFloat() - 1.1f);
        return 1f;
    }

    public float getMaxSize()
    {
        return maxSize;
    }

    public float getMaxAge()
    {
        return maxAge;
    }

    @Override
    protected float getInertia()
    {
        return speed;
    }

    public float getAgeFraction()
    {
        return Mth.clamp(age / getMaxAge(), 0, 1);
    }

    public float getIntensityScale()
    {
        return intensityScale;
    }

    protected float calculateCurrentSize()
    {
        var speed = getMaxAge(); // grows to full size in about 1/4 time of its max age.
        var ageFraction = Mth.clamp(age / speed, 0, 1f);
        var maxNodeSize = getMaxSize() * getIntensityScale();
        var startSize = 0.2f * maxNodeSize;
        return startSize + (maxNodeSize * ageFraction);
    }
}
