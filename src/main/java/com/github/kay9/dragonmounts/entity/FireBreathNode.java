package com.github.kay9.dragonmounts.entity;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

    public static FireBreathNode create(TameableDragon shooter)
    {
        var mouthPos = shooter.getMouthPos();
        var direction = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
        return new FireBreathNode(DMLRegistry.FIRE_BREATH.get(), mouthPos.x(), mouthPos.y(), mouthPos.z(), direction.x(), direction.y(), direction.z(), shooter.getLevel());
    }

    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);

        if (!getLevel().isClientSide()) return;

        var damage = (float) TameableDragon.BASE_DAMAGE;
        LivingEntity living = null;
        if (getOwner() instanceof LivingEntity shooter)
        {
            damage = (float) shooter.getAttributeValue(Attributes.ATTACK_DAMAGE);
            living = shooter;
        }

        var entity = result.getEntity();
        if (entity.fireImmune()) damage *= 0.25;
        else entity.setSecondsOnFire(7);

        if (entity.hurt(getDamageSource(), damage) && living != null)
            doEnchantDamageEffects(living, entity);
    }

    @Override
    public boolean isOnFire()
    {
        return true;
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
