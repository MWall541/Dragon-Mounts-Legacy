package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class KeyMappings
{
    private static final String CATEGORY = "key.categories.dragonmounts";

    public static final KeyMapping FLIGHT_ASCENT_KEY = keymap("flight_ascent", GLFW.GLFW_KEY_SPACE, CATEGORY);
    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping DISMOUNT = keymap("dismount", GLFW.GLFW_KEY_LEFT_SHIFT, CATEGORY);
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6, CATEGORY);

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
    }

    public static void registerKeybinds(Consumer<KeyMapping> registrar)
    {
        registrar.accept(FLIGHT_ASCENT_KEY);
        registrar.accept(FLIGHT_DESCENT_KEY);
        registrar.accept(DISMOUNT);
        registrar.accept(CAMERA_CONTROLS);
    }

    public static void handleKeyPress(int key, int action)
    {
        if (action != GLFW.GLFW_PRESS) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (Minecraft.getInstance().screen != null) return;

        if (isKeyPressed(key, CAMERA_CONTROLS)) {
            handleCameraControlsToggle(player);
        }

        if (isKeyPressed(key, DISMOUNT)) {
            handleDismount(player);
        }
    }

    private static boolean isKeyPressed(int key, KeyMapping mapping) {
        return key == mapping.getKey().getValue();
    }

    private static void handleCameraControlsToggle(Player player) {
        if (player.getVehicle() instanceof TameableDragon d) {
            DMLConfig.CAMERA_DRIVEN_FLIGHT.set(!DMLConfig.cameraDrivenFlight());
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("mount.dragon.camera_controls." + (DMLConfig.cameraDrivenFlight()? "enabled" : "disabled"), d.getDisplayName()), true);
        }
    }

    private static void handleDismount(Player player) {
        if (player.getVehicle() instanceof TameableDragon) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send(new ServerboundPlayerCommandPacket(
                        player,
                        ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY
                ));
            }
            player.stopRiding();
            MountCameraManager.onDragonDismount();
        }
    }

}
