package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.client.SoundRegistry;
import com.github.kay9.dragonmounts.client.WeaponSound;
import com.github.kay9.dragonmounts.entity.breath.FireBreathNode;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.mojang.serialization.Codec;

public class FireBreathAbility extends MouthWeaponAbility<MouthWeaponAbility.SoundData>
{
    public static final FireBreathAbility INSTANCE = new FireBreathAbility();
    public static final Codec<FireBreathAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void tickWeapon(TameableDragon dragon, boolean attacking, SoundData soundData)
    {
        super.tickWeapon(dragon, attacking, soundData);

        if (dragon.isServer() && soundData.getAttackTime() >= MouthWeaponAbility.OPEN_JAW_DELAY)
            dragon.getLevel().addFreshEntity(FireBreathNode.shoot(dragon));
    }

    @Override
    public WeaponSound createWeaponSound(TameableDragon dragon)
    {
        return new WeaponSound(dragon, SoundRegistry.ADULT_FIRE_BREATH_START.get(), SoundRegistry.ADULT_FIRE_BREATH_LOOP.get(), SoundRegistry.ADULT_FIRE_BREATH_STOP.get());
    }

    @Override
    public String type()
    {
        return Ability.FIRE_BREATH;
    }
}
