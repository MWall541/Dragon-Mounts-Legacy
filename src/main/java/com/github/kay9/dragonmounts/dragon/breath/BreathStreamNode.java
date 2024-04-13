package com.github.kay9.dragonmounts.dragon.breath;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public abstract class BreathStreamNode extends Projectile
{
    private static final String NBT_MAX_SIZE = "MaxSize";
    private static final String NBT_MAX_AGE = "MaxAge";
    private static final String NBT_INTENSITY = "Intensity";
    private static final String NBT_AGE = "Age";

    private static final float MINIMUM_SIZE_FACTOR = 0.2f;
    private static final byte EXPIRE_EVENT = 8; // seems unused in base entities...

    private float maxSize;
    private int maxAge; // in ticks
    private float intensity;

    private int age;

    protected BreathStreamNode(EntityType<? extends Projectile> entityType, Level level)
    {
        super(entityType, level);
    }

    public BreathStreamNode(EntityType<? extends BreathStreamNode> type, Level level, TameableDragon shooter, float initialSpeed, float shotInaccuracy, float averageSize, float sizeDif, int averageAge, int ageDif)
    {
        this(type, level);

        setOwner(shooter);

        intensity = Mth.nextFloat(random, 0.9f, 1.1f);
        intensity *= shooter.getScale();

        maxSize = averageSize + Mth.nextFloat(random, -sizeDif, sizeDif);
        maxSize *= intensity;

        maxAge = averageAge + Mth.nextInt(random, -ageDif, ageDif);
        maxAge *= intensity;

        initialSpeed *= intensity;
        shotInaccuracy *= intensity;

        refreshDimensions();
//        moveTo(shooter.getMouthPos()); todo
        shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0, initialSpeed, shotInaccuracy);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putFloat(NBT_MAX_SIZE, maxSize);
        tag.putInt(NBT_MAX_AGE, maxAge);
        tag.putFloat(NBT_INTENSITY, intensity);
        tag.putInt(NBT_AGE, age);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        this.maxSize = tag.getFloat(NBT_MAX_SIZE);
        this.maxAge = tag.getInt(NBT_MAX_AGE);
        this.intensity = tag.getFloat(NBT_INTENSITY);
        this.age = tag.getInt(NBT_AGE);

        refreshDimensions();
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public float getMaxSize()
    {
        return maxSize;
    }

    public float getIntensity()
    {
        return intensity;
    }

    public int getAge()
    {
        return age;
    }

    public float getAgeFraction()
    {
        return (float) getAge() / (float) getMaxAge();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick()
    {
        ++age;

        if (!level().isClientSide())
        {
            if (!level().hasChunkAt(blockPosition()))
            {
                discard();
                return;
            }

            if (getAge() > getMaxAge())
            {
                expire();
                return;
            }
        }

        super.tick();
        checkInsideBlocks();

        if (getBbWidth() < getMaxSize()) refreshDimensions();

        setDeltaMovement(scaleMovement(getDeltaMovement()));
        move(MoverType.SELF, getDeltaMovement());

        contactEnvironment();
    }

    public void expire()
    {
        if (!level().isClientSide())
        {
            level().broadcastEntityEvent(this, EXPIRE_EVENT);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == EXPIRE_EVENT) expire();
        else super.handleEntityEvent(id);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount)
    {
        return false;
    }

    @Override
    protected MovementEmission getMovementEmission()
    {
        return MovementEmission.NONE;
    }

    protected Vec3 scaleMovement(Vec3 currentDelta)
    {
        return currentDelta;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose)
    {
        var scale = getGrowthScale();
        scale = Math.min(scale, getMaxSize());
        return EntityDimensions.scalable(scale, scale);
    }

    protected abstract float getGrowthScale();

    protected abstract void contactEnvironment();

    public void touch()
    {
        var box = getBoundingBox().inflate(1);
        for (var pos : BlockPos.betweenClosed((int) box.minX, (int) box.minY, (int) box.minZ, (int) box.maxX, (int) box.maxY, (int) box.maxZ))
        {
            if (!level().getBlockState(pos).isAir())
            {
                var normal = position().subtract(Vec3.atBottomCenterOf(pos)).normalize();
                var dir = Direction.getNearest(normal.x(), normal.y(), normal.z());
                onHit(new BlockHitResult(Vec3.atLowerCornerOf(pos), dir, pos, false));
            }
        }
        for (var entity : level().getEntities(this, box, this::canHitEntity))
            onHit(new EntityHitResult(entity));
    }

    public void rayTrace()
    {
        var hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitResult))
            onHit(hitResult);
    }

    public void damageEntity(Entity entity, DamageSource source)
    {
        if (entity.hurt(source, getDamageStrength()) && getOwner() instanceof LivingEntity l)
            doEnchantDamageEffects(l, entity);
    }

    protected float getDamageStrength()
    {
        float dmg = getOwner() instanceof LivingEntity l?
                (float) l.getAttributeBaseValue(DMLRegistry.RANGED_DAMAGE.get()) :
                TameableDragon.BASE_PROJECTILE_DAMAGE;
        return dmg * getIntensity();
    }
}
