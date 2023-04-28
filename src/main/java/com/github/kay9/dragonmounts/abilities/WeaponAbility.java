package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.WeaponAbilityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;

public abstract class WeaponAbility implements Ability
{
    // needed for syncing
    public static final EntityDataAccessor<Boolean> ATTACKING = TameableDragon.createDataKey(EntityDataSerializers.BOOLEAN);

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
        if (dragon.isControlledByLocalInstance())
        {
            var keyDown = DMLRegistry.WEAPON_KEY.getAsBoolean();
            if (keyDown != attacking)
            {
                setAttacking(dragon, keyDown);
                WeaponAbilityPacket.send(dragon, this, keyDown);
            }
        }

        var data = dragon.<Data>getAbilityData(this);
        if (attacking) data.incrTime();

        tickWeapon(dragon, attacking, data.getAttackTime()); // intention is for overrides without needing to re-retrieve anything more than once every tick.
    }

    public void tickWeapon(TameableDragon dragon, boolean attacking, int attackTime)
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
