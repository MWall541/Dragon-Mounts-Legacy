package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyMappings
{
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(DragonMountsLegacy.id(DragonMountsLegacy.MOD_ID));

    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z);
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6);

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, KEY_CATEGORY);
    }

    /** Called every client tick; consumes camera-toggle key presses. */
    public static void tick()
    {
        while (CAMERA_CONTROLS.consumeClick())
        {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.getVehicle() instanceof TameableDragon d)
            {
                DMLConfig.setCameraDrivenFlight(!DMLConfig.cameraDrivenFlight());
                player.sendOverlayMessage(Component.translatable("mount.dragon.camera_controls." + (DMLConfig.cameraDrivenFlight()? "enabled" : "disabled"), d.getDisplayName()));
            }
        }
    }
}
