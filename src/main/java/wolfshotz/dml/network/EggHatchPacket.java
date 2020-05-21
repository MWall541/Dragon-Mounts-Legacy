package wolfshotz.dml.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.egg.DragonEggEntity;

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

    /**
     * Notify the client of the egg hatching. (Do the hatch effects etc)
     *
     * @param egg the hatching egg
     */
    public static void send(DragonEggEntity egg)
    {
        if (!egg.world.isRemote)
            DragonMountsLegacy.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> egg), new EggHatchPacket(egg));
    }
}
