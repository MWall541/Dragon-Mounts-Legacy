package com.github.kay9.dragonmounts.network;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateBreedsPacket
{
    public static final UpdateBreedsPacket INSTANCE = new UpdateBreedsPacket(); // breeds collection is known on the server, no need to make new instances

    private final Collection<DragonBreed> breeds;

    private UpdateBreedsPacket()
    {
        this.breeds = Collections.emptyList();
    }

    public UpdateBreedsPacket(FriendlyByteBuf buf)
    {
        this.breeds = buf.readList(UpdateBreedsPacket::fromBytes);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeCollection(BreedManager.getBreeds(), UpdateBreedsPacket::toBytes);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> BreedManager.populate(breeds::forEach));
        ctx.get().setPacketHandled(true);
    }

    private static void toBytes(FriendlyByteBuf buf, DragonBreed breed)
    {
        buf.writeResourceLocation(breed.id());
        buf.writeInt(breed.primaryColor());
        buf.writeInt(breed.secondaryColor());
        int id = breed.hatchParticles().map(i -> Registry.PARTICLE_TYPE.getId(i.getType())).orElse(-1);
        buf.writeVarInt(id);
        if (id != -1) breed.hatchParticles().get().writeToNetwork(buf);
        var props = breed.modelProperties();
        buf.writeBoolean(props.middleTailScales());
        buf.writeBoolean(props.tailHorns());
        buf.writeBoolean(props.thinLegs());
        buf.writeInt(breed.growthTime());
    }

    /**
     * Create only the bare minimum information clients need for breeds to be functional.
     * Some of this stuff can easily be done on the server with no client work needed.
     */
    private static DragonBreed fromBytes(FriendlyByteBuf buf)
    {
        return new DragonBreed(buf.readResourceLocation(),
                buf.readInt(),
                buf.readInt(),
                readPossibleParticle(buf),
                new DragonBreed.ModelProperties(buf.readBoolean(), buf.readBoolean(), buf.readBoolean()),
                ImmutableMap.of(),
                ImmutableList.of(),
                ImmutableList.of(),
                ImmutableSet.of(),
                Optional.empty(),
                BuiltInLootTables.EMPTY,
                buf.readInt());
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private static <T extends ParticleOptions> Optional<ParticleOptions> readPossibleParticle(FriendlyByteBuf buf)
    {
        var id = buf.readVarInt();
        if (id != -1)
        {
            var type = (ParticleType<T>) Registry.PARTICLE_TYPE.byId(id);
            return Optional.of(type.getDeserializer().fromNetwork(type, buf));
        }
        return Optional.empty();
    }

    public static void send(@Nullable ServerPlayer player)
    {
        var target = player == null? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
        DragonMountsLegacy.NETWORK.send(target, INSTANCE);
    }
}
