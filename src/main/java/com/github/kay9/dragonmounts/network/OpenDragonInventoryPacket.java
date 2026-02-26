package com.github.kay9.dragonmounts.network;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenDragonInventoryPacket {

    private final int entityId;

    // Constructor
    public OpenDragonInventoryPacket(int entityId) {
        this.entityId = entityId;
    }

    /*
     * ENCODE
     */
    public static void encode(OpenDragonInventoryPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    /*
     * DECODE
     */
    public static OpenDragonInventoryPacket decode(FriendlyByteBuf buf) {
        return new OpenDragonInventoryPacket(buf.readInt());
    }

    /*
     * HANDLE (SERVER SIDE)
     */
    public static void handle(OpenDragonInventoryPacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Entity entity = player.level().getEntity(msg.entityId);

            if (entity instanceof TameableDragon dragon) {

                // must be tamed and has an owner
                if (!dragon.isTame() || dragon.getOwnerUUID() == null)
                    return;

                // must have a chest
                if (!dragon.hasChest())
                    return;

                // If player is riding
                if (player.getVehicle() == dragon) {

                    // must actually be passenger
                    if (!dragon.getPassengers().contains(player))
                        return;

                    // open the inventory while riding the dragon
                    dragon.openChestInventory(player);
                    return;
                }

                // if player is in interaction range
                double range = player.getEntityReach() + 1.0;
                if (player.distanceToSqr(dragon) > range * range)
                    return;

                // must see dragon (no opening through walls)
                if (!player.hasLineOfSight(dragon))
                    return;

                // anyone in interaction range can open through keybind
                dragon.openChestInventory(player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}