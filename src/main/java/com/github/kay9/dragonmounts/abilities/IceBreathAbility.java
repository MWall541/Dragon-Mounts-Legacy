package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.client.SoundRegistry;
import com.github.kay9.dragonmounts.client.WeaponSound;
import com.github.kay9.dragonmounts.entity.breath.IceBreathNode;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.mojang.serialization.Codec;

public class IceBreathAbility extends MouthWeaponAbility<MouthWeaponAbility.SoundData>
{
    public static final IceBreathAbility INSTANCE = new IceBreathAbility();
    public static final Codec<IceBreathAbility> CODEC = Codec.unit(INSTANCE);

    @Override
    public void tickWeapon(TameableDragon dragon, boolean attacking, SoundData soundData)
    {
        super.tickWeapon(dragon, attacking, soundData);

        if (dragon.isServer() && soundData.getAttackTime() >= MouthWeaponAbility.OPEN_JAW_DELAY)
            dragon.getLevel().addFreshEntity(IceBreathNode.shoot(dragon));
    }

    @Override
    public WeaponSound createWeaponSound(TameableDragon dragon)
    {
        return new WeaponSound(dragon, SoundRegistry.ICE_BREATH_START.get(), SoundRegistry.ICE_BREATH_LOOP.get(), SoundRegistry.ICE_BREATH_STOP.get());
    }

    @Override
    public String type()
    {
        return Ability.ICE_BREATH;
    }
}
