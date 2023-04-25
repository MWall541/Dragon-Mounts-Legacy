package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;

public abstract class MouthWeaponAbility extends WeaponAbility
{
    public static final int OPEN_JAW_DELAY = 5; // ticks
    private static final float JAW_SPEED = 1f / OPEN_JAW_DELAY;

    @Override
    public void tickWeapon(TameableDragon dragon, boolean attacking, int attackTime)
    {
        super.tickWeapon(dragon, attacking, attackTime);

        // open/close jaw
        if (!dragon.isServer()) dragon.getAnimator().getJawTimer().add(attacking? JAW_SPEED : -JAW_SPEED);
    }
}
