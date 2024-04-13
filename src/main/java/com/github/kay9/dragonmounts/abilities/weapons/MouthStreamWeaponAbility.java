package com.github.kay9.dragonmounts.abilities.weapons;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breath.BreathStreamNode;
import net.minecraft.world.entity.EntityType;

public abstract class MouthStreamWeaponAbility extends MouthWeaponAbility
{
    @Override
    @SuppressWarnings("ConstantConditions")
    public void tickWeapon(TameableDragon dragon)
    {
        if (!dragon.isServer()) return;

        var level = dragon.level();
        var node = getNodeType().create(level);

//        node.shootFrom(...) todo

        level.addFreshEntity(node);
    }

    public abstract EntityType<? extends BreathStreamNode> getNodeType();
}
