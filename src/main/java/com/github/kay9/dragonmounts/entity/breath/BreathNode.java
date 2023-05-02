package com.github.kay9.dragonmounts.entity.breath;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
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
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BreathNode extends Projectile implements IEntityAdditionalSpawnData
{
    private static final byte EXPIRE_EVENT = 8;

    public static final float DEFAULT_MAX_SIZE = 2f;
    public static final int DEFAULT_MAX_AGE = 40;
    public static final float DEFAULT_SPEED = 0.95f;

    private Vec3 direction = Vec3.ZERO;
    private float movementSpeed = DEFAULT_SPEED;
    private float maxSize = DEFAULT_MAX_SIZE;
    private int maxAge = DEFAULT_MAX_AGE;
    private float intensityScale = 1f;
    protected int age = 0;

    public BreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    public BreathNode(EntityType<? extends BreathNode> type, Entity shooter, Vec3 startPos, Vec3 direction)
    {
        super(type, shooter.getLevel());

        this.movementSpeed = variedSpeed();
        this.maxSize = variedMaxSize();
        this.maxAge = variedMaxAge();
        this.direction = direction;

        setOwner(shooter);
        moveTo(startPos);
        reapplyPosition();
    }

    protected static Vec3 getShootDirection(Random random, float dirX, float dirY, float inaccuracy)
    {
        var offset = inaccuracy * 2;
        var innacX = offset * random.nextFloat() - inaccuracy;
        var innacY = offset * random.nextFloat() - inaccuracy;
        return Vec3.directionFromRotation(dirX + innacX, dirY + innacY);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick()
    {
        age++;

        if (!getLevel().isClientSide())
        {
            if (!getLevel().hasChunkAt(blockPosition()))
            {
                discard();
                return;
            }

            if (age > getMaxAge())
            {
                expire();
                return;
            }
        }


        super.tick();

        var prevPos = position();
        refreshDimensions();
        setPos(prevPos);

        setDeltaMovement(getMoveDirection().scale(getMovementSpeed()));
        move(MoverType.SELF, getDeltaMovement());
        Vec3 vec3 = getDeltaMovement();
        if (vec3.lengthSqr() != 0)
        {
            double d0 = vec3.horizontalDistance();
            setYRot((float) (Mth.atan2(vec3.z, vec3.x) * (180 / Math.PI)) + 90f);
            setXRot((float) (Mth.atan2(d0, vec3.y) * (180 / Math.PI)) - 90f);
        }

        contactMethod();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
        tag.put("direction", newDoubleList(direction.x, direction.y, direction.z));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");

        if (tag.contains("direction", 9))
        {
            var list = tag.getList("direction", 6);
            if (list.size() == 3)
                direction = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
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
    protected MovementEmission getMovementEmission()
    {
        return MovementEmission.NONE;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        var size = getCurrentSize();
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        var owner = getOwner();
        var ownerId = owner == null? 0 : owner.getId();
        buffer.writeInt(ownerId);
        buffer.writeFloat(movementSpeed);
        buffer.writeFloat(maxSize);
        buffer.writeInt(maxAge);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        var ownerId = buffer.readInt();
        if (ownerId != 0) setOwner(getLevel().getEntity(ownerId));
        movementSpeed = buffer.readFloat();
        maxSize = buffer.readFloat();
        maxAge = buffer.readInt();
    }

    /**
     * Describes the method of contact on objects in the environment. Called every tick.
     */
    public void contactMethod()
    {
        rayTrace();
    }

    /**
     * A method of contact that uses raytracing, pointing in the direction of the movement.
     * Useful for less needy results.
     */
    public void rayTrace()
    {
        var hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitResult))
            onHit(hitResult);
    }

    /**
     * A method of contact that uses the bounding box to interact with objects in the environment.
     * Anything within the box, from blocks to entities, is hit.
     * This is useful when the projectile is rather large
     * or when the projectile may "brush" by any objects as it flies by them. (e.g. fire passes by leaves, and sets them on fire)
     */
    public void touch()
    {
        var box = getBoundingBox().inflate(0.1);
        for (var pos : BlockPos.betweenClosed((int) box.minX, (int) box.minY, (int) box.minZ, (int) box.maxX, (int) box.maxY, (int) box.maxZ))
        {
            if (!level.getBlockState(pos).isAir())
            {
                var normal = position().subtract(Vec3.atBottomCenterOf(pos)).normalize();
                var dir = Direction.getNearest(normal.x(), normal.y(), normal.z());
                onHit(new BlockHitResult(Vec3.atLowerCornerOf(pos), dir, pos, false));
            }
        }
        for (var entity : getLevel().getEntities(null, box))
            onHit(new EntityHitResult(entity));
    }

    /**
     * Slow moving or stuck nodes shouldn't stick around long.
     */
    public void ageBySpeed()
    {
        var currentSpeed = getDeltaMovement().length();
        var dif = getMovementSpeed() - currentSpeed;
        if (dif > 0.1) age += 5 * dif;
    }

    public void expire()
    {
        if (!getLevel().isClientSide())
        {
            getLevel().broadcastEntityEvent(this, EXPIRE_EVENT);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == EXPIRE_EVENT) expire();
        else super.handleEntityEvent(id);
    }

    public float variedMaxSize()
    {
        return DEFAULT_MAX_SIZE + (0.5f * random.nextFloat() - 0.25f);
    }

    public int variedMaxAge()
    {
        return DEFAULT_MAX_AGE + (random.nextInt(10) - 5);
    }

    public float variedSpeed()
    {
        return DEFAULT_SPEED + (0.3f * random.nextFloat() - 0.15f);
    }

    @SuppressWarnings("ConstantConditions")
    public float variedIntensityScale()
    {
        if (getOwner() instanceof TameableDragon dragon)
            return dragon.getScale() + (0.4f * random.nextFloat() - 0.2f);
        return 1f;
    }

    public float getMaxSize()
    {
        return maxSize;
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public float getMovementSpeed()
    {
        return movementSpeed;
    }

    public float getAgeFraction()
    {
        return Mth.clamp((float) age / (float) getMaxAge(), 0f, 1f);
    }

    public float getIntensityScale()
    {
        return intensityScale;
    }

    public Vec3 getMoveDirection()
    {
        return direction;
    }

    protected float getCurrentSize()
    {
        return calculateRapidExpansion();
    }

    protected float calculateRapidExpansion()
    {
        var speed = getMaxAge() * 0.25f; // grows to full size in about 1/4 time of its max age.
        var ageFraction = Mth.clamp(age / speed, 0, 1f);
        var maxNodeSize = getMaxSize() * getIntensityScale();
        var startSize = 0.2f * maxNodeSize;
        return Mth.lerp(ageFraction, startSize, maxNodeSize);
    }

    protected float calculateLinearExpansion()
    {
        var ageFraction = getAgeFraction();
        var maxSize = getMaxSize() * getIntensityScale();
        var startSize = 0.2f * maxSize;
        return Mth.lerp(ageFraction, startSize, maxSize);
    }
}
