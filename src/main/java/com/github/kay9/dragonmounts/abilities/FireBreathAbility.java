package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.entity.FireBreathNode;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.mojang.serialization.Codec;

public class FireBreathAbility extends MouthWeaponAbility
{
    public static final FireBreathAbility INSTANCE = new FireBreathAbility();
    public static final Codec<FireBreathAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void tickWeapon(TameableDragon dragon, boolean attacking, int attackTime)
    {
        super.tickWeapon(dragon, attacking, attackTime);
        if (attackTime >= MouthWeaponAbility.OPEN_JAW_DELAY)
            dragon.getLevel().addFreshEntity(FireBreathNode.shoot(dragon));
    }

    @Override
    public String type()
    {
        return Ability.FIRE_BREATH;
    }
}
