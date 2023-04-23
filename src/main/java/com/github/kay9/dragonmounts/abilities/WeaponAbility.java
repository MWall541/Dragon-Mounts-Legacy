package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.WeaponAbilityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public abstract class WeaponAbility implements Ability
{
    // needed for syncing
    public static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);

    @Override
    public void initialize(TameableDragon dragon)
    {
        // unfortunately, there is no elegant way to remove data accessors, if/when the breed closes. should be fine.
        if (!dragon.getEntityData().itemsById.containsKey(ATTACKING.getId()))
            dragon.getEntityData().define(ATTACKING, false);
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        var attacking = isAttacking(dragon);
        if (dragon.isControlledByLocalInstance())
        {
            var keyDown = DMLRegistry.WEAPON_KEY.getAsBoolean();
            if (keyDown != attacking)
            {
                setAttacking(dragon, keyDown);
                WeaponAbilityPacket.send(dragon, this, keyDown);
            }
        }
    }

    /**
     * Perform a one-time or continous attack
     * @param dragon
     */
    public void attack(TameableDragon dragon) {}

    /**
     * Is only called if this weapon is continuous
     * @param dragon
     */
    public void endAttack(TameableDragon dragon) {}

    /**
     * Determines whether the attack is continuous or not.
     * For example, a continuous attack would be a constant stream of fire.
     * A non-continuous would be a single-shot fireball.
     * @return if the weapon is continuous.
     */
    public boolean isContinuous()
    {
        return false;
    }

    public void receiveCommand(TameableDragon dragon, boolean attacking)
    {
        setAttacking(dragon, attacking);

        if (isContinuous())
        {
            if (!attacking) endAttack(dragon);
        }
        else if (attacking) attack(dragon);
    }

    public boolean isAttacking(TameableDragon dragon)
    {
        return dragon.getEntityData().get(ATTACKING);
    }

    public void setAttacking(TameableDragon dragon, boolean attacking)
    {
        dragon.getEntityData().set(ATTACKING, attacking);
    }
}
