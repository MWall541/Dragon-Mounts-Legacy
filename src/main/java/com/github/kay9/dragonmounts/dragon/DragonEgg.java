package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.data.BreedManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * todo: Entity may not be needed. All this logic could possibly be implemented in a BlockEntity...?
 */
public class DragonEgg extends Entity
{
    // constants
    public static final float WIDTH = 0.9f; // Roughly the same size as the dragon egg block box
    public static final float HEIGHT = 0.9f;
    public static final int HABITAT_UPDATE_INTERVAL = 200; // every 10 seconds (give or take, this depends on lag)
    public static final int DEFAULT_HATCH_TIME = 12000;
    public static final int BREED_TRANSITION_TIME = 200;
    public static final int MIN_HABITAT_POINTS = 2;
    public static final byte HATCH_ID = 1;
    public static final byte WIGGLE_ID = 2;

    public static final String NBT_HATCH_TIME = "HatchTime";

    public static final EntityDataAccessor<String> BREED = SynchedEntityData.defineId(DragonEgg.class, EntityDataSerializers.STRING);

    public DragonBreed breed;
    public final TransitionHandler transitioner;
    private int hatchTime;
//    private final LerpedFloat wiggleTime; todo: wiggle animations
//    private boolean wiggling;

    public DragonEgg(EntityType<? extends Entity> type, Level level)
    {
        super(type, level);

        breed = BreedManager.getFallback();
        hatchTime = breed.hatchTime();
        transitioner = new TransitionHandler();
//        wiggleTime = LerpedFloat.unit();
    }

    @Override
    protected void defineSynchedData()
    {
        entityData.define(BREED, "");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putInt(NBT_HATCH_TIME, hatchTime);
        tag.putString(TameableDragon.NBT_BREED, breed.id().toString());
        transitioner.save(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        setHatchTime(tag.getInt(NBT_HATCH_TIME));
        setEggBreed(BreedManager.read(tag.getString(TameableDragon.NBT_BREED)));
        transitioner.read(tag);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key)
    {
        if (key.equals(BREED)) breed = BreedManager.read(entityData.get(BREED));
        else if (key.equals(TransitionHandler.TRANSITION_BREED))
        {
            // the transitioner can have a null value
            var breed = BreedManager.getNullable(ResourceLocation.tryParse(entityData.get(TransitionHandler.TRANSITION_BREED)));
            if (breed != null) transitioner.begin(breed);
            else transitioner.abort();
        }
        super.onSyncedDataUpdated(key);
    }

    public void setEggBreed(DragonBreed breed)
    {
        entityData.set(BREED, breed.id().toString());
    }

    public void setHatchTime(int time)
    {
        hatchTime = time;
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean isPushable()
    {
        return isAlive();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return isAlive();
    }

    @Override
    public boolean fireImmune()
    {
        return true;
    }

    @Override
    public void tick()
    {
        // update motion - should fall
        if (!isNoGravity()) setDeltaMovement(getDeltaMovement().add(0, -0.04d, 0));

        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.3d, 0.98d, 0.3d));
        level.getEntities(this, getBoundingBox(), e -> !(e instanceof Player)).forEach(this::push);
        transitioner.tick();

        if (!level.isClientSide)
        {
            // Update habitat
            if (DMLConfig.updateHabitats() && hatchTime > BREED_TRANSITION_TIME && !transitioner.isRunning() && tickCount % HABITAT_UPDATE_INTERVAL == 0)
                updateHabitat();

            // hatch!
            if (--hatchTime <= 0)
            {
                hatch();
                return; // Were hatching! drop the cock and lets go!
            }

            if (hatchTime < breed.hatchTime() * 0.25f && random.nextInt(Math.max(10, hatchTime)) == 0) wiggle();
        }
        else
        {
//            wiggleTime.add(wiggling? 0.1f : -0.1f);
//            if (wiggleTime.get() == 1) wiggling = false;

            addHatchingParticles();
        }

        super.tick();
    }

    public void addHatchingParticles()
    {
        double px = getX() + (random.nextDouble() - 0.5);
        double py = getY() + random.nextDouble();
        double pz = getZ() + (random.nextDouble() - 0.5);
        double ox = 0;
        double oy = 0;
        double oz = 0;

        var particle = breed.getHatchParticles(random);
        if (particle.getType() == ParticleTypes.DUST) py = getY() + (random.nextDouble() - 0.5) + 1;
        if (particle.getType() == ParticleTypes.PORTAL)
        {
            ox = (random.nextDouble() - 0.5) * 2;
            oy = (random.nextDouble() - 0.5) * 2;
            oz = (random.nextDouble() - 0.5) * 2;
        }

        level.addParticle(particle, px, py, pz, ox, oy, oz);
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.getEntity() instanceof Player)
        {
            spawnAtLocation(DMLEggBlock.Item.create(breed, hatchTime));
            discard();
        }
        else if (amount > 4f) discard();

        return super.hurt(source, amount);
    }

    public void updateHabitat()
    {
        DragonBreed winner = null;
        int prevPoints = 0;
        for (var breed : BreedManager.getBreeds())
        {
            int points = breed.getHabitatPoints(level, blockPosition());
            if (points > MIN_HABITAT_POINTS && points > prevPoints)
            {
                winner = breed;
                prevPoints = points;
            }
        }
        if (winner != null && winner != breed) transitioner.begin(winner);
    }

    @Override
    public void handleEntityEvent(byte id)
    {
        switch (id)
        {
            case HATCH_ID -> hatch();
            case WIGGLE_ID -> wiggle();
            default -> super.handleEntityEvent(id);
        }
    }

    public void hatch()
    {
        if (level.isClientSide)
        {
//            level.globalLevelEvent(2001, blockPosition(), Block.getStateId(breed.getDefaultState()));
            level.playLocalSound(getX(), getY(), getZ(), SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1, 1, false);
        }
        else
        {
            level.broadcastEntityEvent(this, HATCH_ID);
            TameableDragon dragon = DMLRegistry.DRAGON.get().create(level);
            dragon.setBreed(breed);
            dragon.setBaby(true);
            dragon.setPos(getX(), getY(), getZ());
            dragon.setCustomName(getCustomName());
            level.addFreshEntity(dragon);
        }

        discard();
    }

    public void wiggle()
    {
        if (level.isClientSide)
        {
//            if (wiggling || wiggleTime.get() > 0) return;
            level.playLocalSound(getX(), getY(), getZ(), SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 1, 1, false);
//            this.wiggling = true;
        }
        else level.broadcastEntityEvent(this, WIGGLE_ID);
    }

    public class TransitionHandler
    {
        private static final EntityDataAccessor<String> TRANSITION_BREED = SynchedEntityData.defineId(DragonEgg.class, EntityDataSerializers.STRING);
        private static final String NBT_TRANSITION_BREED = "TransitionBreed";
        private static final String NBT_TRANSITION_TIME = "TransitionTime";

        public DragonBreed transitioningBreed;
        public int transitionTime;

        public TransitionHandler()
        {
            entityData.define(TRANSITION_BREED, "");
        }

        public void tick()
        {
            if (isRunning())
            {
                if (--transitionTime == 0 && !level.isClientSide)
                {
                    setEggBreed(transitioningBreed);
                    entityData.set(TRANSITION_BREED, "");
                }

                if (level.isClientSide)
                {
                    for (var i = 0; i < BREED_TRANSITION_TIME - transitionTime; i++)
                    {
                        var px = getX() + (random.nextDouble() - 0.5);
                        var py = getY() + random.nextDouble();
                        var pz = getZ() + (random.nextDouble() - 0.5);
                        var particle = transitioningBreed.getDustParticles(random);

                        level.addParticle(particle, px, py, pz, 0, 0, 0);
                    }
                }
            }
        }

        public void begin(DragonBreed transitioningBreed, int transitionTime)
        {
            this.transitioningBreed = transitioningBreed;
            this.transitionTime = transitionTime;
            entityData.set(TRANSITION_BREED, transitioningBreed.id().toString());
        }

        public void begin(DragonBreed transitioningBreed)
        {
            begin(transitioningBreed, BREED_TRANSITION_TIME);
        }

        public void abort()
        {
            transitioningBreed = null;
            transitionTime = 0;
        }

        public boolean isRunning()
        {
            return transitionTime > 0;
        }

        public void save(CompoundTag tag)
        {
            if (transitioningBreed != null)
            {
                tag.putString(NBT_TRANSITION_BREED, transitioningBreed.id().toString());
                tag.putInt(NBT_TRANSITION_TIME, transitionTime);
            }
        }

        public void read(CompoundTag tag)
        {
            var breed = BreedManager.getNullable(ResourceLocation.tryParse(tag.getString(NBT_TRANSITION_BREED)));
            if (breed != null) begin(breed, tag.getInt(NBT_TRANSITION_TIME));
        }
    }
}