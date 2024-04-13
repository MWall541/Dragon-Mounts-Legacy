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
        if (!canFireWeapon(dragon)) return;

        super.tick(dragon);

        if (isEnabled(dragon))
        {
            if (isWeaponPrimed(dragon)) tickWeapon(dragon);

            usageTime++;
        }
    }

    @Override
    public void setEnabled(TameableDragon dragon, boolean enabled)
    {
        super.setEnabled(dragon, enabled);

        if (!dragon.isServer()) dragon.getAnimator().setOpenJaw(enabled);
    }

    @Override
    public void onDisabled(TameableDragon dragon)
    {
        usageTime = 0;
    }

    public int getUsageTime()
    {
        return usageTime;
    }

    public boolean isWeaponPrimed(TameableDragon dragon)
    {
        return usageTime >= MOUTH_OPEN_TIME_FOR_ATTACK;
    }

    public boolean canFireWeapon(TameableDragon dragon)
    {
        return !dragon.isHatchling();
    }

    public abstract void tickWeapon(TameableDragon dragon);
}
