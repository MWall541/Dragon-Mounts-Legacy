package com.github.kay9.dragonmounts.abilities.weapons;

import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.client.KeyMappings;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public abstract class WeaponAbility implements Ability
{
    public static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);

    private int timeAttacking = 0;

    @Override
    public void initialize(TameableDragon dragon)
    {
        if (!dragon.getEntityData().hasItem(ATTACKING))
            dragon.getEntityData().define(ATTACKING, false);
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        if (!canAttack(dragon)) return;

        // if neither driver nor AI is handling attacks, forcibly stop attacking to prevent uncontrolled attacks
        var shouldAttack = isDriverAttacking(dragon) || isAIAttacking(dragon);
        if (shouldAttack != isAttacking(dragon))
            setAttacking(dragon, shouldAttack);

        if (isAttacking(dragon)) timeAttacking++;
    }

    public boolean isAttacking(TameableDragon dragon)
    {
        return dragon.getEntityData().get(ATTACKING);
    }

    public void setAttacking(TameableDragon dragon, boolean attacking)
    {
        dragon.getEntityData().set(ATTACKING, attacking);
        timeAttacking = 0;
    }

    public int getTimeAttacking()
    {
        return timeAttacking;
    }

    public boolean canAttack(TameableDragon dragon)
    {
        return !dragon.isHatchling();
    }

    public boolean isDriverAttacking(TameableDragon dragon)
    {
        return KeyMappings.MOUNT_ABILITY.isDown(); //todo: this doesn't work on server...
    }

    public abstract boolean isAIAttacking(TameableDragon dragon);
}
