package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
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

        dragon.registerAbilityData(this, new Data());
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        var attacking = isAttacking(dragon);
        var timer = dragon.<Data>getAbilityData(this);
        if (dragon.isControlledByLocalInstance())
        {
            var keyDown = DMLRegistry.WEAPON_KEY.getAsBoolean();
            if (keyDown != attacking)
            {
                setAttacking(dragon, keyDown);
                if (keyDown) attack(dragon);
                WeaponAbilityPacket.send(dragon, this, keyDown);
            }
        }

        if (attacking) timer.incrTime();
    }

    /**
     * Perform a one-shot attack.
     */
    public void attack(TameableDragon dragon)
    {
    }

    public boolean isAttacking(TameableDragon dragon)
    {
        return dragon.getEntityData().get(ATTACKING);
    }

    public void setAttacking(TameableDragon dragon, boolean attacking)
    {
        dragon.getEntityData().set(ATTACKING, attacking);
        dragon.<Data>getAbilityData(this).setAttackTime(0);
    }

    public static class Data implements Ability.Data
    {
        private int attackTime = 0;

        public void setAttackTime(int time)
        {
            this.attackTime = time;
        }

        public int getAttackTime()
        {
            return attackTime;
        }

        public void incrTime()
        {
            attackTime++;
        }
    }
}
