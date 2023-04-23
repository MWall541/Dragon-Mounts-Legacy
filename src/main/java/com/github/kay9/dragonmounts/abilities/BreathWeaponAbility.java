package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;

public abstract class BreathWeaponAbility extends WeaponAbility
{
    @Override
    public void attack(TameableDragon dragon)
    {
        if (!dragon.isServer()) dragon.getAnimator().setOpenJaw(true);
    }

    @Override
    public void endAttack(TameableDragon dragon)
    {
        if (!dragon.isServer()) dragon.getAnimator().setOpenJaw(false);
    }

    @Override
    public boolean isContinuous()
    {
        return true;
    }
}
