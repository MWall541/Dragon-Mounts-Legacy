package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.TickEvent;

/**
 * A heavily hardcoded class to display a couple messages after a player mounts a dragon.
 * {@link MountControlsMessenger#sendControlsMessage()} is called from a dragon when the LocalPlayer mounts.
 * Messages include information about flight controls, such as ascending and the camera controls.
 * A hardcoded design was decided as an expanded functionality doesn't really make any sense for the
 * direction of the mod, and would instead be wasted resources.
 */
public class MountControlsMessenger
{
    // taken from Gui#setOverlayMessage
    private static final int MESSAGE_LENGTH_INTERVAL = 60;

    private static int delay = 0;

    public static void sendControlsMessage()
    {
        delay = MESSAGE_LENGTH_INTERVAL;
    }

    public static void tick(TickEvent.ClientTickEvent evt)
    {
        if (evt.phase == TickEvent.Phase.END && delay > 0)
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
                        Keybinds.FLIGHT_DESCENT_KEY.getTranslatedKeyMessage()), true);
        }
    }
}
