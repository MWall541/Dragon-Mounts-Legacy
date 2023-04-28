package com.github.kay9.dragonmounts.entity;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class FireBreathNode extends BreathNode
{
    public FireBreathNode(EntityType<? extends FireBreathNode> type, Level level)
    {
        super(type, level);
    }

    protected FireBreathNode(EntityType<? extends AbstractHurtingProjectile> type, double xPos, double yPos, double zPos, double xMove, double yMove, double zMove, Level level)
    {
        super(type, xPos, yPos, zPos, xMove, yMove, zMove, level);
    }

    public static FireBreathNode shoot(TameableDragon shooter)
    {
        var mouthPos = shooter.getMouthPos();
        var innacX = 0.2f * shooter.getRandom().nextFloat() - 0.1f;
        var innacY = 0.2f * shooter.getRandom().nextFloat() - 0.1f;
        var direction = Vec3.directionFromRotation((shooter.getXRot() * 0.2f) + innacX, shooter.getYRot() + innacY);
        return new FireBreathNode(DMLRegistry.FIRE_BREATH.get(), mouthPos.x(), mouthPos.y(), mouthPos.z(), direction.x(), direction.y(), direction.z(), shooter.getLevel());
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

        if (getLevel().isClientSide())
        {
            // AbstractHurtingProjectile already features a trailing particle of smoke,
            // so we don't have to do that here.
            var motion = getDeltaMovement();
            var x = getRandomX(0.2) + motion.x();
            var y = getRandomY() + motion.y();
            var z = getRandomZ(0.2) + motion.z();
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);

        if (!getLevel().isClientSide()) return;

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
    public boolean isOnFire()
    {
        return true;
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
}
