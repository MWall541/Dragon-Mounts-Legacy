package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.client.KeyMappings;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.EnableControlledAbilityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public abstract class ControlledAbility implements Ability
{
    // Synced entity data for when AI uses the ability and client needs to be aware of it.
    public static final EntityDataAccessor<Boolean> ENABLED = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);

    @Override
    public void initialize(TameableDragon dragon)
    {
        if (!dragon.getEntityData().hasItem(ENABLED))
            dragon.getEntityData().define(ENABLED, false);
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        if (dragon.hasLocalDriver())
        {
            var keyDown = KeyMappings.MOUNT_ABILITY.isDown();
            if (keyDown != isEnabled(dragon))
            {
                setEnabled(dragon, keyDown);
                EnableControlledAbilityPacket.send(keyDown);
            }
        }

        var shouldEnable = isDriverEnabled(dragon) || isAiEnabled(dragon);
        if (shouldEnable != isEnabled(dragon))
            setEnabled(dragon, shouldEnable);
    }

    public boolean isEnabled(TameableDragon dragon)
    {
        return dragon.getEntityData().get(ENABLED);
    }

    public void setEnabled(TameableDragon dragon, boolean enabled)
    {
        dragon.getEntityData().set(ENABLED, enabled);
    }

    protected boolean isDriverEnabled(TameableDragon dragon)
    {
        return dragon.hasControllingPassenger() && isEnabled(dragon);
    }

    protected boolean isAiEnabled(TameableDragon dragon)
    {
        return false;
    }
}
