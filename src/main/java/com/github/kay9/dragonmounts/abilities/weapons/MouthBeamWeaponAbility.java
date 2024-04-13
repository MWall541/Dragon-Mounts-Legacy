package com.github.kay9.dragonmounts.abilities.weapons;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class MouthBeamWeaponAbility extends MouthWeaponAbility
{
    private static final double DEFAULT_BEAM_DISTANCE = 64;

    @Override
    public void tickWeapon(TameableDragon dragon)
    {
        var hit = ProjectileUtil.getHitResultOnViewVector(dragon, EntitySelector.CAN_BE_COLLIDED_WITH, getBeamDistance());
        if (hit.getType() != HitResult.Type.MISS) onHit(hit);
    }

    public void onHit(HitResult hr)
    {
        if (hr instanceof EntityHitResult ehr) onEntityHit(ehr);
        else onBlockHit((BlockHitResult) hr);
    }

    public abstract void onEntityHit(EntityHitResult ehr);

    public abstract void onBlockHit(BlockHitResult bhr);

    protected double getBeamDistance()
    {
        return DEFAULT_BEAM_DISTANCE;
    }

    public void damageTarget(TameableDragon dragon, Entity target, DamageSource source)
    {
        if (target.hurt(source, getDamageStrength(dragon)))
            dragon.doEnchantDamageEffects(dragon, target);
    }

    public float getDamageStrength(TameableDragon dragon)
    {
        return (float) dragon.getAttributeBaseValue(DMLRegistry.RANGED_DAMAGE.get());
    }
}
