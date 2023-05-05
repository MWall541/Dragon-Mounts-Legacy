package com.github.kay9.dragonmounts.entity.breath;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class IceBreathNode extends BreathNode
{
    private static final int DEFAULT_MAX_AGE = 50;
    private static final float DEFAULT_MAX_SPEED = 0.8f;
    private static final float DEFAULT_MAX_SIZE = 4f;
    private static final int TICKS_PER_FREEZE = 4;
    private static final int MAX_ADDED_FREEZE_TICKS = 140; // 7 seconds

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
    public void contactMethod()
    {
        touch();
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
                getLevel().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, getMoveDirection().x * 0.5/*(2 * random.nextDouble() - 1) * 0.1*/, getMoveDirection().y * 0.05, getMoveDirection().z * 0.5/*(2 * random.nextDouble() - 1) * 0.1*/);
            }
        }
    }

    /**
     * Freezes entities.
     * Vanilla implements its own freezing mechanics to entities;
     * such as screen effects to players, and slowing entity movements.
     * Therefore, we don't have to do that ourselves.
     * Cold damage is incremented based on the armor of the entity.
     * If the armor is considered metallic, the damage is multiplied.
     * Leather and diamond reduces damage.
     * Physical damage is determined by how far frozen the entity is
     * OR if the entity is immune, base attack damage is divided by 1/4.
     */
    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);

        var entity = result.getEntity();
        entity.setDeltaMovement(entity.getDeltaMovement().add(getDeltaMovement().scale(0.01))); // blizzard wind pushes entities.

        if (getLevel().isClientSide()) return; // server calculations from this point on

        var coldIntensity = getIntensityScale();
        var damage = (float) TameableDragon.BASE_DAMAGE;
        TameableDragon shooter = null;

        if (entity instanceof LivingEntity living)
            coldIntensity *= getConductiveIntensity(living);

        if (getOwner() instanceof TameableDragon dragon)
        {
            shooter = dragon;
            damage = (float) dragon.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        damage *= coldIntensity;

        if (entity.canFreeze())
        {
            var frozenTicks = entity.getTicksFrozen() + (int) (TICKS_PER_FREEZE * coldIntensity);
            var max = entity.getTicksRequiredToFreeze() + MAX_ADDED_FREEZE_TICKS;
            var dmgFraction = entity.getPercentFrozen();

            entity.setTicksFrozen(Math.min(max, frozenTicks));
            damage *= dmgFraction;
        }
        else damage *= 0.25f;

        entity.clearFire();
        entity.setSharedFlagOnFire(false);

        if (damage != 0 && entity.hurt(getDamageSource(), damage) && shooter != null)
            doEnchantDamageEffects(shooter, entity);
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

    public DamageSource getDamageSource()
    {
        var shooter = getOwner();
        var owned = shooter != null;
        var name = owned? "iceBreath" : "freeze";
        if (!owned) shooter = this;
        return new IndirectEntityDamageSource(name, this, shooter).setProjectile();
    }

    private static float getConductiveIntensity(LivingEntity living)
    {
        var intensity = 1f; // unmodified; living is fully insulated in leather.
        for (var item : living.getArmorSlots())
        {
            if (!item.is(ItemTags.FREEZE_IMMUNE_WEARABLES))
                intensity += 0.1f;
        }
        return intensity;
    }
}
