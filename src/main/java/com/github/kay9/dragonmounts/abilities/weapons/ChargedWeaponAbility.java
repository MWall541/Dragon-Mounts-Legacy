package com.github.kay9.dragonmounts.abilities.weapons;

import com.github.kay9.dragonmounts.dragon.TameableDragon;

public abstract class ChargedWeaponAbility extends MouthWeaponAbility
{
    @Override
    public void onDisabled(TameableDragon dragon)
    {
        if (isWeaponPrimed(dragon)) fireChargedWeapon(dragon);

        super.onDisabled(dragon);
    }

    public abstract void fireChargedWeapon(TameableDragon dragon);
}
