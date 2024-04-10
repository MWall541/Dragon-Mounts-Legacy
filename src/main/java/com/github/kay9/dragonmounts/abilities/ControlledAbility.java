package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.client.KeyMappings;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.ControlAbilityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;

public abstract class ControlledAbility implements Ability
{
    // Synced entity data for when AI enables the ability and client needs to be aware of it.
    public static final EntityDataAccessor<Boolean> ENABLED = TameableDragon.createOutsideData(EntityDataSerializers.BOOLEAN);

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
                enable(dragon, keyDown);
                ControlAbilityPacket.send(dragon.getUUID(), dragon.getAbilities().indexOf(this), keyDown);
            }
        }

        // forcibly set each tick to disallow rogue ability usage
        var shouldEnable = shouldEnable(dragon);
        if (shouldEnable != isEnabled(dragon))
            enable(dragon, shouldEnable);
    }

    public boolean isEnabled(TameableDragon dragon)
    {
        return dragon.getEntityData().get(ENABLED);
    }

    public void enable(TameableDragon dragon, boolean enabled)
    {
        dragon.getEntityData().set(ENABLED, enabled);

        if (enabled) onEnabled(dragon);
        else onDisabled(dragon);
    }

    protected boolean shouldEnable(TameableDragon dragon)
    {
        return enabledByDriver(dragon) || enabledBySelf(dragon);
    }

    protected boolean enabledByDriver(TameableDragon dragon)
    {
        // already being enabled is a requirement since the keybind/packet handles it.
        return dragon.hasControllingPassenger() && isEnabled(dragon);
    }

    protected abstract boolean enabledBySelf(TameableDragon dragon);

    public void onEnabled(TameableDragon dragon) {}

    public void onDisabled(TameableDragon dragon) {}
}
