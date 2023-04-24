package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.entity.BreathNode;
import com.github.kay9.dragonmounts.entity.FireBreathNode;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BreathWeaponAbility extends WeaponAbility
{

    private static final Map<String, BreathType> BREATH_TYPES = new HashMap<>();
    public static final BreathType FIRE = registerBreathType("fire", FireBreathNode::create);

    private static final int BREATH_START_DELAY = 5; // ticks

    public static final Codec<BreathWeaponAbility> CODEC = Codec.STRING.xmap(s -> new BreathWeaponAbility(BREATH_TYPES.get(s)), a -> a.breathType.name());

    private final BreathType breathType;
    public BreathWeaponAbility(BreathType breathType)
    {
        this.breathType = breathType;
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        super.tick(dragon);
        var attacking = isAttacking(dragon);

        var jawSpeed = 1 / BREATH_START_DELAY;
        if (!dragon.isServer()) dragon.getAnimator().getJawTimer().add(attacking? jawSpeed : -jawSpeed);
        if (dragon.<WeaponAbility.Data>getAbilityData(this).getAttackTime() > BREATH_START_DELAY)
            dragon.getLevel().addFreshEntity(breathType.createBreathEntity(dragon));
    }

    @Override
    public String type()
    {
        return Ability.BREATH_WEAPON;
    }

    public static BreathType registerBreathType(String name, Function<TameableDragon, BreathNode> factory)
    {
        return BREATH_TYPES.put(name, new BreathType(name, factory));
    }

    public record BreathType(String name, Function<TameableDragon, BreathNode> factory)
    {
        public BreathNode createBreathEntity(TameableDragon dragon)
        {
            return factory.apply(dragon);
        }
    }
}
