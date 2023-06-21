package com.github.kay9.dragonmounts.dragon.egg;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.habitats.Habitat;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class HatchableEggBlockEntity extends BlockEntity
{
    public static final int MIN_HABITAT_POINTS = 2;
    public static final int BREED_TRANSITION_TIME = 200;

    private final TransitionHandler transitioner = new TransitionHandler();

    private Supplier<DragonBreed> breed = () -> null;
    private Component customName;

    public HatchableEggBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(DMLRegistry.EGG_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        if (getBreed() != null)
            tag.putString(HatchableEggBlock.NBT_BREED, getBreed().id(getLevel().m_9598_()).toString());

        if (getCustomName() != null)
            tag.putString(HatchableEggBlock.NBT_NAME, Component.Serializer.toJson(customName));

        if (getTransition().isRunning())
        {
            var transitionTag = new CompoundTag();
            getTransition().save(transitionTag);
            tag.put(TransitionHandler.NBT_TRANSITIONER, transitionTag);
        }
    }

    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);

        setBreed(Suppliers.memoize(() -> BreedRegistry.get(pTag.getString(HatchableEggBlock.NBT_BREED), getLevel().m_9598_())));

        var name = pTag.getString(HatchableEggBlock.NBT_NAME);
        if (!name.isBlank()) setCustomName(Component.Serializer.fromJson(name));

        var transitioner = pTag.getCompound(TransitionHandler.NBT_TRANSITIONER);
        if (!transitioner.isEmpty()) getTransition().load(transitioner);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public DragonBreed getBreed()
    {
        return breed.get();
    }

    public void setBreed(Supplier<DragonBreed> breed)
    {
        this.breed = breed;
    }

    public Component getCustomName()
    {
        return customName;
    }

    public void setCustomName(Component name)
    {
        this.customName = name;
    }

    public TransitionHandler getTransition()
    {
        return transitioner;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        getTransition().tick(getLevel().getRandom());
    }

    public void updateHabitat()
    {
        DragonBreed winner = null;
        int prevPoints = 0;
        for (var breed : BreedRegistry.registry(getLevel().m_9598_()))
        {
            int points = 0;
            for (Habitat habitat : breed.habitats()) points += habitat.getHabitatPoints(level, getBlockPos());
            if (points > MIN_HABITAT_POINTS && points > prevPoints)
            {
                winner = breed;
                prevPoints = points;
            }
        }
        if (winner != null && winner != getBreed())
        {
            getTransition().begin(winner);
            if (getLevel() instanceof ServerLevel sl)
                sl.getChunkSource().blockChanged(getBlockPos());
        }
    }

    public class TransitionHandler
    {
        private static final String NBT_TRANSITIONER = "TransitionerTag";
        private static final String NBT_TRANSITION_BREED = "TransitionBreed";
        private static final String NBT_TRANSITION_TIME = "TransitionTime";

        public Supplier<DragonBreed> transitioningBreed;
        public int transitionTime;

        public void tick(RandomSource random)
        {
            if (isRunning())
            {
                if (--transitionTime == 0)
                {
                    setBreed(transitioningBreed);
                }

                if (getLevel().isClientSide)
                {
                    for (var i = 0; i < (BREED_TRANSITION_TIME - transitionTime) * 0.25; i++)
                    {
                        var pos = getBlockPos();
                        var px = pos.getX() + random.nextDouble();
                        var py = pos.getY() + random.nextDouble();
                        var pz = pos.getZ() + random.nextDouble();
                        var particle = HatchableEggBlock.dustParticleFor(transitioningBreed.get(), random);

                        getLevel().addParticle(particle, px, py, pz, 0, 0, 0);
                    }
                }
            }
        }

        public void begin(Supplier<DragonBreed> transitioningBreed, int transitionTime)
        {
            this.transitioningBreed = transitioningBreed;
            this.transitionTime = transitionTime;
        }

        public void begin(DragonBreed transitioningBreed)
        {
            begin(() -> transitioningBreed, BREED_TRANSITION_TIME);
        }

        public boolean isRunning()
        {
            return transitionTime > 0;
        }

        public void save(CompoundTag tag)
        {
            tag.putString(NBT_TRANSITION_BREED, transitioningBreed.get().id(getLevel().m_9598_()).toString());
            tag.putInt(NBT_TRANSITION_TIME,  transitionTime);
        }

        public void load(CompoundTag tag)
        {
            begin(Suppliers.memoize(() -> BreedRegistry.get(tag.getString(NBT_TRANSITION_BREED), getLevel().m_9598_())), tag.getInt(NBT_TRANSITION_TIME));
        }
    }
}
