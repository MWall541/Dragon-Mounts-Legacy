package com.github.kay9.dragonmounts.network;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.ForgeModImpl;
import com.github.kay9.dragonmounts.abilities.ControlledAbility;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record ControlAbilityPacket(UUID dragonId, int abilityId, boolean enabling)
{
    public ControlAbilityPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readUUID(), buffer.readInt(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUUID(dragonId());
        buffer.writeInt(abilityId());
        buffer.writeBoolean(enabling());
    }

    public void process(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            var entity = ((ServerLevel) ctx.get().getSender().level()).getEntity(dragonId());
            if (!(entity instanceof TameableDragon dragon))
            {
                DragonMountsLegacy.LOG.warn("Packet to control ability received with unknown entity; ignoring.");
                return;
            }

            if (!dragon.hasControllingPassenger())
            {
                DragonMountsLegacy.LOG.warn("Packet to control ability received with no driver; ignoring.");
                return;
            }

            var ability = dragon.getAbilities().get(abilityId());
            if (!(ability instanceof ControlledAbility controlled))
            {
                DragonMountsLegacy.LOG.warn("Packet to control ability received with invalid ability id; ignoring.");
                return;
            }

            controlled.enable(dragon, enabling());
        });
        ctx.get().setPacketHandled(true);
    }

    public static void send(UUID dragonId, int abilityId, boolean enabling)
    {
        ForgeModImpl.NETWORK.sendToServer(new ControlAbilityPacket(dragonId, abilityId, enabling));
    }
}
