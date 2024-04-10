package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class KeyMappings
{
    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, KeyMapping.CATEGORY_MOVEMENT);
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6, KeyMapping.CATEGORY_MOVEMENT);
    public static final KeyMapping MOUNT_ABILITY = keymap("mount_ability", GLFW.GLFW_KEY_F, KeyMapping.CATEGORY_GAMEPLAY);

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
    }

    public static void registerKeybinds(Consumer<KeyMapping> registrar)
    {
        registrar.accept(FLIGHT_DESCENT_KEY);
        registrar.accept(CAMERA_CONTROLS);
        registrar.accept(MOUNT_ABILITY);
    }

    public static void handleKeyPress(int key, int action)
    {
        if (key == CAMERA_CONTROLS.getKey().getValue()
                && action == GLFW.GLFW_PRESS
                && Minecraft.getInstance().player.getVehicle() instanceof TameableDragon d)
        {
            DMLConfig.CAMERA_DRIVEN_FLIGHT.set(!DMLConfig.cameraDrivenFlight());
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("mount.dragon.camera_controls." + (DMLConfig.cameraDrivenFlight()? "enabled" : "disabled"), d.getDisplayName()), true);
        }
    }
}
