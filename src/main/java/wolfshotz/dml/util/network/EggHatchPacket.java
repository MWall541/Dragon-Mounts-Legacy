package wolfshotz.dml.util.network;

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

    }

    public void encode(PacketBuffer buf)
    {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {

    }
}
