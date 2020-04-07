package wolfshotz.dml.util.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

import java.util.function.Supplier;

public class EggHatchPacket
{
    private int entityID;

    public EggHatchPacket(DragonEggEntity egg)
    {
        this.entityID = egg.getEntityId();
    }

    public EggHatchPacket(PacketBuffer buf)
    {
        this.entityID = buf.readInt();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(entityID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> ((DragonEggEntity) Minecraft.getInstance().world.getEntityByID(entityID)).hatch());
        ctx.get().setPacketHandled(true);
    }
}
