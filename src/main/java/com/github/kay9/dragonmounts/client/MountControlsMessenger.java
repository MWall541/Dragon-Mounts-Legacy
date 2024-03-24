package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * A heavily hardcoded class to display a message after the dismount controls display when a player mounts a dragon.
 * {@link MountControlsMessenger#sendControlsMessage()} is called from a dragon when the LocalPlayer mounts.
 * Messages include information about flight controls, such as how to ascend/descend.
 * A hardcoded design was decided as an expanded functionality doesn't really make any sense for the
 * direction of the mod, and would instead be wasted resources.
 */
public class MountControlsMessenger
{
    private static int delay = 0;

    public static void sendControlsMessage()
    {
        // the length the initial "dismount" message is displayed for, in ticks.
        // Our message displays after 60 ticks (after the dismount message.)
        // taken from Gui#setOverlayMessage.
        delay = 60;
    }

    @SuppressWarnings("ConstantConditions")
    public static void tick()
    {
        if (delay > 0)
        {
            var player = Minecraft.getInstance().player;
            if (!(player.getVehicle() instanceof TameableDragon))
            {
                delay = 0;
                return;
            }

            --delay;

            if (delay == 0)
                player.displayClientMessage(new TranslatableComponent("mount.dragon.vertical_controls",
                        Minecraft.getInstance().options.keyJump.getTranslatedKeyMessage(),
                        KeyMappings.FLIGHT_DESCENT_KEY.getTranslatedKeyMessage()), true);
        }
    }
}
