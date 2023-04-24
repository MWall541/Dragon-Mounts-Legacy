package com.github.kay9.dragonmounts.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;

public class BreathNode extends AbstractHurtingProjectile
{
    public BreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    public BreathNode(EntityType<? extends AbstractHurtingProjectile> type, double xPos, double yPos, double zPos, double xMove, double yMove, double zMove, Level level)
    {
        super(type, xPos, yPos, zPos, xMove, yMove, zMove, level);
    }

    @Override
    protected boolean canHitEntity(Entity entity)
    {
        if (entity == this) return false;
        if (getOwner() != null && getOwner().isAlliedTo(entity)) return false;
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
}
