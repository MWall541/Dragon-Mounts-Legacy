package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class MountControlsMessenger {

    private static int controlMessageTicks = 0;

    public static void sendControlsMessage()
    {
        // the length the initial "dismount" message is displayed for, in ticks.
        // Our message displays after 60 ticks (after the dismount message.)
        // taken from Gui#setOverlayMessage.
        controlMessageTicks = 60;
    }

    @SuppressWarnings("ConstantConditions")
    public static void tick()
    {
        if (controlMessageTicks > 0) {
            var player = Minecraft.getInstance().player;
            if (player == null || !(Minecraft.getInstance().player.getVehicle() instanceof TameableDragon)) {
                controlMessageTicks = 0;
                return;
            }
            Component controlsText = Component.translatable("mount.dragon.dragon_controls",
                    KeyMappings.FLIGHT_ASCENT_KEY.getTranslatedKeyMessage(),
                    KeyMappings.FLIGHT_DESCENT_KEY.getTranslatedKeyMessage(),
                    KeyMappings.DISMOUNT.getTranslatedKeyMessage()
            );
            Minecraft.getInstance().gui.setOverlayMessage(controlsText, false);
            controlMessageTicks--;
        }
    }
}