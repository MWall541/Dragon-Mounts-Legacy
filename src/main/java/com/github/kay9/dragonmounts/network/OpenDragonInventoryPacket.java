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

                // security checks (VERY important)
                if (!dragon.isOwnedBy(player))
                    return;

                if (player.getVehicle() != dragon)
                    return;

                dragon.openChestInventory(player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}