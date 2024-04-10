package com.github.kay9.dragonmounts.abilities.weapons;

import com.github.kay9.dragonmounts.abilities.ControlledAbility;
import com.github.kay9.dragonmounts.dragon.TameableDragon;

public abstract class MouthWeaponAbility extends ControlledAbility
{
    public static final int MOUTH_OPEN_TIME_FOR_ATTACK = 20; // ticks;

    private int usageTime;

    @Override
    public void tick(TameableDragon dragon)
    {
        super.tick(dragon);

        if (isEnabled(dragon))
        {
            usageTime++;
        }
    }

    @Override
    public void enable(TameableDragon dragon, boolean enabled)
    {
        super.enable(dragon, enabled);

        if (!dragon.isServer()) dragon.getAnimator().setOpenJaw(enabled);
    }
}
