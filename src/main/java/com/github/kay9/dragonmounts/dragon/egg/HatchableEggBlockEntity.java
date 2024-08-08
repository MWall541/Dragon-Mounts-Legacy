package com.github.kay9.dragonmounts.dragon.egg;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.habitats.Habitat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class HatchableEggBlockEntity extends BlockEntity implements Nameable
{
    public static final String NBT_BREED = TameableDragon.NBT_BREED;
    public static final String NBT_NAME = "CustomName";

    public static final int MIN_HABITAT_POINTS = 2;
    public static final int BREED_TRANSITION_TIME = 200;

    private final TransitionHandler transitioner = new TransitionHandler();

    private Holder.Reference<DragonBreed> breed;
    private Component customName; // necessary since breeding/commands can change this

    public HatchableEggBlockEntity(BlockPos pPos, BlockState pBlockState)
    {
        super(DMLRegistry.EGG_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    // for saving to world
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        super.saveAdditional(tag, lookup);

        tag.putString(NBT_BREED, breed.getRegisteredName());

        if (customName != null)
            tag.putString(NBT_NAME, Component.Serializer.toJson(customName, lookup));

        if (getTransition().isRunning())
        {
            var transitionTag = new CompoundTag();
            getTransition().save(transitionTag);
            tag.put(TransitionHandler.NBT_TRANSITIONER, transitionTag);
        }
    }

    // for loading from world
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        super.loadAdditional(tag, lookup);

        Holder.Reference<DragonBreed> parsedBreed = DragonBreed.parse(tag.getString(NBT_BREED), lookup);
        if (parsedBreed != null)
            setBreed(parsedBreed);

        if (tag.contains(NBT_NAME, 8))
            setCustomName(parseCustomNameSafe(tag.getString("CustomName"), lookup));

        var transitioner = tag.getCompound(TransitionHandler.NBT_TRANSITIONER);
        if (!transitioner.isEmpty())
            getTransition().load(transitioner, lookup);

        // todo is this needed at component syncing?
//        if (getLevel() != null && getLevel().isClientSide()) // client needs to be aware of new changes
//            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
    }

    // for destroying block to item
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);

        components.set(DMLRegistry.DRAGON_BREED_COMPONENT.get(), getBreedHolder().key());
        components.set(DataComponents.CUSTOM_NAME, getCustomName());
    }

    // for placing block from item
    @Override
    protected void applyImplicitComponents(DataComponentInput components)
    {
        super.applyImplicitComponents(components);

        // todo: is a level passed here yet?
        setBreed(DragonBreed.get(components.get(DMLRegistry.DRAGON_BREED_COMPONENT.get()), getLevel().registryAccess()));
        setCustomName(components.get(DataComponents.CUSTOM_NAME));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup)
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, lookup);
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

    public Holder.Reference<DragonBreed> getBreedHolder()
    {
        return breed;
    }

    public void setBreed(Holder.Reference<DragonBreed> breed)
    {
        this.breed = breed;
    }

    public boolean hasBreed()
    {
        return breed != null;
    }

    public void setCustomName(Component name)
    {
        this.customName = name;
    }

    @Override
    @Nullable
    public Component getCustomName()
    {
        return components().get(DataComponents.CUSTOM_NAME);
    }

    @Override
    @Nullable
    public Component getName()
    {
        Component customName = getCustomName();
        if (customName != null) return customName;

        return Component.translatable(DMLRegistry.EGG_BLOCK_ITEM.get().getDescriptionId(),
                Component.translatable(DragonBreed.getTranslationKey(getBreedHolder().key().location().toString())));
    }

    public TransitionHandler getTransition()
    {
        return transitioner;
    }

    @SuppressWarnings({"ConstantConditions", "unused"}) // guarded
    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        // if this passes, we may not receive a breed; resolve a random one.
        // could be caused by /setblock, or other natural means.
        if (!pLevel.isClientSide() && !hasBreed())
        {
            DragonBreed.registry(pLevel.registryAccess())
                    .getRandom(pLevel.getRandom())
                    .ifPresent(this::setBreed);
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }

        getTransition().tick(getLevel().getRandom());
    }

    @SuppressWarnings("ConstantConditions") // level exists at this point
    public void updateHabitat()
    {
        DragonBreed.registry(getLevel().registryAccess())
                .holders()
                .max(Comparator.comparingInt(breed -> // compare habitat environment by point value
                {
                    int points = 0;
                    for (Habitat habitat : breed.get().habitats())
                        points += habitat.getHabitatPoints(getLevel(), getBlockPos());

                    return points < MIN_HABITAT_POINTS? 0 : points; // habitats have MINIMUM requirements
                }))
                .filter(breed -> breed != getBreedHolder()) // don't update again if environment hasn't changed.
                .ifPresent(breed ->
                {
                    getTransition().begin(breed);
                    getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE); // needed for clients to receive changes
                });
    }

    @SuppressWarnings("ConstantConditions") // level exists at this point
    public class TransitionHandler
    {
        private static final String NBT_TRANSITIONER = "TransitionerTag";
        private static final String NBT_TRANSITION_BREED = "TransitionBreed";
        private static final String NBT_TRANSITION_TIME = "TransitionTime";

        public Holder.Reference<DragonBreed> transitioningBreed;
        public int transitionTime;

        public void tick(RandomSource random)
        {
            if (isRunning())
            {
                if (transitioningBreed.get() == null) // invalid breed id, etc.
                {
                    transitionTime = 0;
                    return;
                }

                if (--transitionTime == 0)
                {
                    setBreed(transitioningBreed);
                    getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
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

        public void startFrom(Holder.Reference<DragonBreed> transitioningBreed, int transitionTime)
        {
            this.transitioningBreed = transitioningBreed;
            this.transitionTime = transitionTime;
        }

        public void begin(Holder.Reference<DragonBreed> transitioningBreed)
        {
            startFrom(transitioningBreed, BREED_TRANSITION_TIME);
        }

        public boolean isRunning()
        {
            return transitionTime > 0;
        }

        public void save(CompoundTag tag)
        {
            tag.putString(NBT_TRANSITION_BREED, transitioningBreed.key().location().toString());
            tag.putInt(NBT_TRANSITION_TIME,  transitionTime);
        }

        public void load(CompoundTag tag, HolderLookup.Provider lookup)
        {
            Holder.Reference<DragonBreed> parsedBreed = DragonBreed.parse(tag.getString(NBT_TRANSITION_BREED), lookup);
            if (parsedBreed != null)
                startFrom(parsedBreed, tag.getInt(NBT_TRANSITION_TIME));
        }
    }
}
