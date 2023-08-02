package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class Keybinds
{
    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, "key.categories.movement");
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6, "key.categories.movement");

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        var keymap = new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
        ClientRegistry.registerKeyBinding(keymap);
        return keymap;
    }

    public static void handleKeyPress(int key, int action)
    {
        if (key == CAMERA_CONTROLS.getKey().getValue()
                && action == GLFW.GLFW_PRESS
                && Minecraft.getInstance().player.getVehicle() instanceof TameableDragon d)
        {
            DMLConfig.cameraFlight = !DMLConfig.cameraFlight();
            Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("mount.dragon.camera_controls." + (DMLConfig.cameraFlight()? "enabled" : "disabled"), d.getDisplayName()), true);
        }
    }
}
