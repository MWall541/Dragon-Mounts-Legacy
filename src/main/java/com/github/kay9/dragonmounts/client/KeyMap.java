package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KeyMap
{
    private static final List<KeyMapping> REGISTRY = new ArrayList<>();

    public static final KeyMapping FLIGHT_DESCENT = keymap("flight_descent", GLFW.GLFW_KEY_Z, "key.categories.movement");

    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        var keymap = new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
        REGISTRY.add(keymap);
        return keymap;
    }

    public static void register(Consumer<KeyMapping> method)
    {
        REGISTRY.forEach(method);
    }
}
