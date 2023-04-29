package com.github.kay9.dragonmounts.entity;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import org.jetbrains.annotations.Nullable;

// quick notes: Weapon nodes should be heavily affected by the dragons scale.
// Dragons have dynamic growth, as well as possible custom scales.
// Damage, grief, and other effects should reflect this.
public class BreathNode extends Projectile
{
    public static final float DEFAULT_MAX_SIZE = 2f;
    public static final int DEFAULT_MAX_AGE = 40;
    public static final float DEFAULT_SPEED = 1.2f;

    private final float maxSize = variedMaxSize();
    private final float maxAge = variedMaxAge();
    private final float variedSpeed = variedSpeed();

    private float intensityScale = 1f;
    private Vec3 direction = Vec3.ZERO;
    protected int age = 0;

    public BreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    public BreathNode(EntityType<? extends BreathNode> type, Entity shooter, Vec3 startPos, Vec3 direction)
    {
        super(type, shooter.getLevel());

        this.direction = direction;

        setOwner(shooter);
        moveTo(startPos);
        reapplyPosition();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick()
    {
        if (!getLevel().hasChunkAt(blockPosition()))
        {
            discard();
            return;
        }

        if (age++ > getMaxAge())
        {
            expire();
            return;
        }

        super.tick();

        var prevPos = position();
        refreshDimensions();
        setPos(prevPos);

        setDeltaMovement(getMoveDirection().scale(getSpeed()));
        move(MoverType.SELF, getDeltaMovement());
        ProjectileUtil.rotateTowardsMovement(this, 1f);

        contactMethod();
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
                var normal = Vec3.upFromBottomCenterOf(pos, 0.5).subtract(position()).normalize();
                var dir = Direction.getNearest(normal.x(), normal.y(), normal.z());
                onHit(new BlockHitResult(Vec3.atLowerCornerOf(pos), dir, pos, false));

            }
        }
        for (var entity : getLevel().getEntities(null, box))
            onHit(new EntityHitResult(entity));
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
    public EntityDimensions getDimensions(Pose pose)
    {
        var size = calculateCurrentSize();
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        var owner = getOwner();
        var ownerId = owner == null? 0 : owner.getId();
        return new ClientboundAddEntityPacket(getId(), getUUID(), getX(), getY(), getZ(), getXRot(), getYRot(), getType(), ownerId, direction);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
        direction = new Vec3(packet.getXa(), packet.getYa(), packet.getZa());
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
        return 0.95f;
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

    public float getMaxAge()
    {
        return maxAge;
    }

    public float getSpeed()
    {
        return variedSpeed;
    }

    public float getAgeFraction()
    {
        return Mth.clamp(age / getMaxAge(), 0, 1);
    }

    public float getIntensityScale()
    {
        return intensityScale;
    }

    public Vec3 getMoveDirection()
    {
        return direction;
    }

    protected float calculateCurrentSize()
    {
        var speed = getMaxAge() * 0.25f; // grows to full size in about 1/4 time of its max age.
        var ageFraction = Mth.clamp(age / speed, 0, 1f);
        var maxNodeSize = getMaxSize() * getIntensityScale();
        var startSize = 0.2f * maxNodeSize;
        return Mth.lerp(ageFraction, startSize, maxNodeSize);
    }
}
