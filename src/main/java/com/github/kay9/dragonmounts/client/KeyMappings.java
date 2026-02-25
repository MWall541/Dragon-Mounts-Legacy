package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.ForgeModImpl;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.network.FireballPacket;
import com.github.kay9.dragonmounts.network.OpenDragonInventoryPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class KeyMappings
{
    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z, "key.categories.movement");
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6, "key.categories.movement");
    public static final KeyMapping SHOOT_FIREBALL = keymap("shoot_fireball", InputConstants.KEY_G, "key.categories.movement");
    public static final KeyMapping DRAGON_INVENTORY = keymap("dragon_inventory", InputConstants.KEY_I, "key.categories.movement");

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping, String category)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, category);
    }

    public static void registerKeybinds(Consumer<KeyMapping> registrar)
    {
        registrar.accept(FLIGHT_DESCENT_KEY);
        registrar.accept(CAMERA_CONTROLS);
        registrar.accept(SHOOT_FIREBALL);
        registrar.accept(DRAGON_INVENTORY);
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        // Only run in-game
        if (mc.player == null || mc.level == null) {
            return;
        }

        var player = mc.player;

        /*
         * CAMERA TOGGLE (PRESS ONLY)
         */
        while (CAMERA_CONTROLS.consumeClick()) {
            if (player.getVehicle() instanceof TameableDragon d) {

                DMLConfig.CAMERA_DRIVEN_FLIGHT.set(!DMLConfig.cameraDrivenFlight());

                player.displayClientMessage(
                        Component.translatable(
                                "mount.dragon.camera_controls." +
                                        (DMLConfig.cameraDrivenFlight()
                                                ? "enabled"
                                                : "disabled"),
                                d.getDisplayName()
                        ),
                        true
                );
            }
        }

        /*
         * FIREBALL (HELD KEY)
         */
        if (SHOOT_FIREBALL.isDown() && player.getVehicle() instanceof TameableDragon) {
            ForgeModImpl.NETWORK.sendToServer(new FireballPacket());
        }


        /*
         * OPEN DRAGON INVENTORY
         */
        // OPEN DRAGON INVENTORY USING KEYBIND
        if (DRAGON_INVENTORY.consumeClick()) {
            TameableDragon dragon = null;

            // First, check if riding
            if (player.getVehicle() instanceof TameableDragon d) {
                dragon = d;
            } else {
                // Raytrace / attack-range check
                Vec3 eyePos = player.getEyePosition(1.0f);
                Vec3 look = player.getLookAngle();
                Vec3 endPos = eyePos.add(look.scale(4.0)); // 4 block reach

                // Raytrace to hit entities
                double closestDist = Double.MAX_VALUE;
                for (TameableDragon td : player.level().getEntitiesOfClass(TameableDragon.class,
                        player.getBoundingBox().expandTowards(look.scale(5.0)))) {

                    if (!td.hasChest()) continue;

                    // Get bounding box and check intersection with ray
                    var bb = td.getBoundingBox();
                    var optionalHit = bb.clip(eyePos, endPos); // returns Optional<Vec3>
                    if (optionalHit.isPresent()) {
                        double distSqr = eyePos.distanceToSqr(optionalHit.get()); // unwrap
                        if (distSqr < closestDist) {
                            closestDist = distSqr;
                            dragon = td;
                        }
                    }
                }
            }

            // send packet if we found a dragon
            if (dragon != null) {
                ForgeModImpl.NETWORK.sendToServer(
                        new OpenDragonInventoryPacket(dragon.getId())
                );
            }
        }
    }
}
