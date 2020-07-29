package wolfshotz.dml.entities;


import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.client.anim.TickFloat;
import wolfshotz.dml.entities.ai.LifeStageController;
import wolfshotz.dml.misc.DragonEggBlock;

import java.util.Comparator;


public class DragonEggEntity extends Entity
{
    // constants
    public static final float WIDTH = 0.9f; // Roughly the same size as the dragon egg block box
    public static final float HEIGHT = 0.9f;
    public static final int CHECK_HABITAT_INTERVAL = 200; // every 10 seconds (give or take, this depends on lag)
    public static final int DEFAULT_HATCH_TIME = 12000;
    public static final float EGG_WIGGLE_THRESHOLD = DEFAULT_HATCH_TIME * 0.25f;
    public static final String NBT_HATCH_TIME = "HatchTime";
    public static final String NBT_BREED = "Type";
    public static final byte HATCH_ID = 1;
    public static final byte WIGGLE_ID = 2;

    public static final DataParameter<String> EGG_TYPE = EntityDataManager.createKey(DragonEggEntity.class, DataSerializers.STRING);
    private final TickFloat wiggleTime = new TickFloat().setLimit(0, 1);
    public DragonEggBlock egg;
    private int hatchTime;
    private boolean wiggling;

    public DragonEggEntity(EntityType<? extends Entity> type, World world)
    {
        super(type, world);
        setEggType(DMLRegistry.AETHER_EGG_BLOCK.get());
        this.hatchTime = DEFAULT_HATCH_TIME;
    }

    @Override
    protected void registerData() { dataManager.register(EGG_TYPE, ""); }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        compound.putInt(NBT_HATCH_TIME, hatchTime);
        compound.putString(NBT_BREED, egg.getRegistryName().getPath());
    }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        this.hatchTime = compound.getInt(NBT_HATCH_TIME);
        setEggType(ForgeRegistries.BLOCKS.getValue(DragonMountsLegacy.rl(compound.getString(NBT_BREED))));
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (key.equals(EGG_TYPE))
            egg = (DragonEggBlock) ForgeRegistries.BLOCKS.getValue(DragonMountsLegacy.rl(dataManager.get(EGG_TYPE)));
    }

    public void setEggType(Block block)
    {
        if (!(block instanceof DragonEggBlock))
            throw new IllegalArgumentException("This damned block is not a dragon egg block. Wth are u doing?: " + block.getRegistryName());
        dataManager.set(EGG_TYPE, block.getRegistryName().getPath());
    }

    @Override
    public IPacket<?> createSpawnPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    public boolean canBePushed() { return isAlive(); }

    @Override
    public boolean canBeCollidedWith() { return isAlive(); }

    @Override
    public void tick()
    {
        // update motion - should fall
        if (!hasNoGravity()) setMotion(getMotion().add(0, -0.04d, 0));

        move(MoverType.SELF, getMotion());
        setMotion(getMotion().mul(0.3d, 0.98d, 0.3d));
        world.getEntitiesInAABBexcluding(this, getBoundingBox(), e -> !(e instanceof PlayerEntity)).forEach(this::applyEntityCollision);

        if (!world.isRemote)
        {
            // Update habitat - before hatch to give that one tick chance
            if (ticksExisted % CHECK_HABITAT_INTERVAL == 0) updateHabitat();

            // hatch!
            if (--hatchTime <= 0)
            {
                hatch();
                return; // Were hatching! drop the cock and lets go!
            }

            if (hatchTime < EGG_WIGGLE_THRESHOLD && rand.nextInt(Math.max(10, hatchTime)) == 0) wiggle();
        }
        else
        {
            wiggleTime.add(wiggling? 0.1f : -0.1f);
            if (wiggleTime.get() == 1) wiggling = false;

            double px = getPosX() + (rand.nextDouble() - 0.5);
            double py = getPosY() + (rand.nextDouble() - 0.5);
            double pz = getPosZ() + (rand.nextDouble() - 0.5);

            if (egg == DMLRegistry.ENDER_EGG_BLOCK.get())
            {
                double ox = (rand.nextDouble() - 0.5) * 2;
                double oy = (rand.nextDouble() - 0.5) * 2;
                double oz = (rand.nextDouble() - 0.5) * 2;
                world.addParticle(ParticleTypes.PORTAL, px, py, pz, ox, oy, oz);
            }
            else
            {
                boolean primary = rand.nextInt(3) != 0;
                float r = egg.getColorR(primary);
                float g = egg.getColorG(primary);
                float b = egg.getColorB(primary);
                world.addParticle(new RedstoneParticleData(r, g, b, 1), px, py + 1, pz, 0, 0, 0);
            }
        }

        super.tick();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        super.attackEntityFrom(source, amount);
        entityDropItem(egg);
        remove();
        return false;
    }

    public void updateHabitat()
    {
        DMLRegistry.BLOCKS
                .getEntries()
                .stream()
                .filter(b -> b.get() instanceof DragonEggBlock)
                .map(b -> (DragonEggBlock) b.get())
                .filter(b -> b.getHabitatPoints(this) > 2)
                .max(Comparator.comparingInt(b -> b.getHabitatPoints(this)))
                .ifPresent(this::setEggType);
    }

    @Override
    public void handleStatusUpdate(byte id)
    {
        switch (id)
        {
            case HATCH_ID:
                hatch(); break;
            case WIGGLE_ID:
                wiggle(); break;
            default:
                super.handleStatusUpdate(id);
        }
    }

    public void hatch()
    {
        if (world.isRemote)
        {
            world.playEvent(2001, getPosition(), Block.getStateId(egg.getDefaultState()));
            world.playSound(getPosX(), getPosY(), getPosZ(), SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 1, 1, false);
        }
        else
        {
            world.setEntityState(this, HATCH_ID);
            TameableDragonEntity dragon = egg.breed.get().create(world);
            dragon.getLifeStageController().setLifeStage(LifeStageController.EnumLifeStage.HATCHLING);
            dragon.setPosition(getPosX(), getPosY(), getPosZ());
            dragon.setCustomName(getCustomName());
            world.addEntity(dragon);
        }

        remove();
    }

    public void wiggle()
    {
        if (world.isRemote)
        {
            if (wiggling || wiggleTime.get() > 0) return;
            world.playSound(getPosX(), getPosY(), getPosZ(), SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 1, 1, false);
            this.wiggling = true;
        }
        else world.setEntityState(this, WIGGLE_ID);
    }
}
