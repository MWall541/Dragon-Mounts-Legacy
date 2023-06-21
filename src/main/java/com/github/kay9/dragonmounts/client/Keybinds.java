package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class Keybinds
{
    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, "key.categories.movement");
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6, "key.categories.movement");

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
    }

    public static void registerKeybinds(RegisterKeyMappingsEvent evt)
    {
        evt.register(FLIGHT_DESCENT_KEY);
        evt.register(CAMERA_CONTROLS);
    }

    public static void handleKeyPress(InputEvent.Key evt)
    {
        if (evt.getKey() == CAMERA_CONTROLS.getKey().getValue()
                && evt.getAction() == GLFW.GLFW_PRESS
                && Minecraft.getInstance().player.getVehicle() instanceof TameableDragon d)
        {
            DMLConfig.cameraFlight = !DMLConfig.cameraFlight();
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("mount.dragon.camera_controls." + (DMLConfig.cameraFlight()? "enabled" : "disabled"), d.getDisplayName()), true);
        }
    }
}
