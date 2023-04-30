package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.client.WeaponSound;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;

public abstract class MouthWeaponAbility<T extends MouthWeaponAbility.SoundData> extends WeaponAbility<T>
{
    public static final int OPEN_JAW_DELAY = 5; // ticks
    private static final float JAW_SPEED = 1f / OPEN_JAW_DELAY;

    @Override
    public void tickWeapon(TameableDragon dragon, boolean attacking, T soundData)
    {
        super.tickWeapon(dragon, attacking, soundData);

        // open/close jaw
        if (!dragon.isServer())
        {
            dragon.getAnimator().getJawTimer().add(attacking? JAW_SPEED : -JAW_SPEED);
            soundData.getSound().tick(attacking);
        }
    }

    @Override
    protected Data createWeaponData(TameableDragon dragon)
    {
        var sound = dragon.isServer()? null : createWeaponSound(dragon);
        return new SoundData(sound);
    }

    public abstract WeaponSound createWeaponSound(TameableDragon dragon);

    public static class SoundData extends Data
    {
        private final WeaponSound sound;

        public SoundData(WeaponSound sound)
        {
            this.sound = sound;
        }

        public WeaponSound getSound()
        {
            return sound;
        }
    }
}
