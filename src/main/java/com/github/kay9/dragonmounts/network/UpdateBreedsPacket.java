package com.github.kay9.dragonmounts.network;

import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateBreedsPacket
{
    private final Collection<DragonBreed> breeds;

    public UpdateBreedsPacket(Collection<DragonBreed> breeds)
    {
        this.breeds = breeds;
    }

    public UpdateBreedsPacket(FriendlyByteBuf buf)
    {
        this.breeds = buf.readList(UpdateBreedsPacket::fromBytes);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeCollection(breeds, UpdateBreedsPacket::toBytes);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> BreedManager.update(breeds::forEach));
        ctx.get().setPacketHandled(true);
    }

    public static void toBytes(FriendlyByteBuf buf, DragonBreed breed)
    {
        buf.writeResourceLocation(breed.id());
        buf.writeInt(breed.primaryColor());
        buf.writeInt(breed.secondaryColor());
        buf.writeBoolean(breed.showMiddleTailScales());
        buf.writeBoolean(breed.showTailSpikes());
        buf.writeInt(breed.growthTime());
    }

    /**
     * Create only the bare minimum information clients need for breeds to be functional.
     * Some of this stuff can easily be done on the server with no client work needed.
     */
    public static DragonBreed fromBytes(FriendlyByteBuf buf)
    {
        return new DragonBreed(buf.readResourceLocation(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                ImmutableMap.of(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                buf.readInt());
    }
}
