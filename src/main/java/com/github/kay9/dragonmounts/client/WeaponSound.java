package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;

public class WeaponSound
{
    private final TameableDragon dragon;
    private final Instance startSound;
    private final Instance loopSound;
    private final Instance stopSound;
    private Phase phase = Phase.IDLE;

    public WeaponSound(TameableDragon dragon, SoundRegistry startSound, SoundRegistry loopSound, SoundRegistry stopSound)
    {
        this.dragon = dragon;
        this.startSound = new Instance(startSound, dragon, false);
        this.loopSound = new Instance(loopSound, dragon, true);
        this.stopSound = new Instance(stopSound, dragon, false);
    }

    public void tick(boolean play)
    {
        if (!play && phase != Phase.IDLE)
        {
            startSound.stopPlaying();
            loopSound.stopPlaying();
            Minecraft.getInstance().getSoundManager().play(stopSound);
            phase = Phase.IDLE;
        }

        switch (phase)
        {
            case IDLE ->
            {
                if (play)
                {
                    phase = Phase.START;
                    startSound.reset();
                    loopSound.reset();
                    stopSound.reset();
                    Minecraft.getInstance().getSoundManager().play(startSound);
                }
            }
            case START ->
            {
                if (startSound.finishing())
                {
                    phase = Phase.LOOP;
                    Minecraft.getInstance().getSoundManager().play(loopSound);
                }
            }
        }
    }

    private enum Phase { IDLE, START, LOOP, STOP }

    private static class Instance extends EntityBoundSoundInstance
    {
        private final TameableDragon dragon;
        private final int durationInTicks;
        private int time;
        private boolean stopped;

        public Instance(SoundRegistry sound, TameableDragon dragon, boolean loop)
        {
            super(sound, dragon.getSoundSource(), 1f, 1f, dragon);
            this.dragon = dragon;
            this.durationInTicks = sound.getDurationInTicks();
            this.looping = loop;
            this.delay = 0;
        }

        @Override
        public void tick()
        {
            super.tick();

            ++time;
            if (!looping && time >= durationInTicks) stopPlaying();

            volume = dragon.getSoundVolume();
            pitch = dragon.getVoicePitch();
        }

        @Override
        public WeighedSoundEvents resolve(SoundManager pHandler)
        {
            return super.resolve(pHandler);
        }

        public boolean finishing()
        {
            return !looping && time > durationInTicks - 20;
        }

        public void stopPlaying()
        {
            stopped = true;
        }

        @Override
        public boolean isStopped()
        {
            return stopped;
        }

        public void reset()
        {
            time = 0;
            stopped = false;
        }
    }
}
