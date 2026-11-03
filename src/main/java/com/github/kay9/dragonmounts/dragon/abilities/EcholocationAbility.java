package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Periodic sonic pulse (independent of movement, unlike {@link FootprintAbility}) that
 * marks nearby living entities with Glowing, like a Warden's echolocation.
 */
public class EcholocationAbility implements Ability, Ability.Factory<EcholocationAbility>
{
    public static final MapCodec<EcholocationAbility> CODEC = MapCodec.unit(EcholocationAbility::new);

    private static final int COOLDOWN = 200; // 10s
    private static final int RADIUS = 16;
    private static final int GLOW_DURATION = 100; // 5s

    private int cooldown;

    @Override
    public void tick(TameableDragon dragon)
    {
        if (!(dragon.level() instanceof ServerLevel level)) return;
        if (cooldown > 0)
        {
            cooldown--;
            return;
        }
        cooldown = COOLDOWN;

        level.playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 2f, 0.9f);
        level.sendParticles(ParticleTypes.SONIC_BOOM, dragon.getX(), dragon.getY() + dragon.getBbHeight() * 0.5, dragon.getZ(), 1, 0, 0, 0, 0);

        var area = dragon.getBoundingBox().inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != dragon && e.isAlive()))
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0, false, true));
    }

    @Override
    public void write(TameableDragon dragon, ValueOutput output)
    {
        output.putInt("cooldown", cooldown);
    }

    @Override
    public void read(TameableDragon dragon, ValueInput input)
    {
        cooldown = input.getIntOr("cooldown", 0);
    }

    @Override
    public EcholocationAbility create()
    {
        return new EcholocationAbility();
    }

    @Override
    public MapCodec<? extends Factory<? extends Ability>> codec()
    {
        return CODEC;
    }
}
