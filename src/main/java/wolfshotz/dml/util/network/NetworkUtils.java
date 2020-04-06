package wolfshotz.dml.util.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

import java.util.Optional;

public class NetworkUtils
{
    public static void registerPackets()
    {
        int index = 0;
        DragonMountsLegacy.NETWORK.registerMessage(++index, EggHatchPacket.class, EggHatchPacket::encode, EggHatchPacket::new, EggHatchPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Notify the client of the egg hatching. (Do the hatch effects etc)
     *
     * @param egg the hatching egg
     */
    public static void sendEggHatchPacket(DragonEggEntity egg)
    {
        if (!egg.world.isRemote)
            DragonMountsLegacy.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> egg), new EggHatchPacket(egg));
    }
}