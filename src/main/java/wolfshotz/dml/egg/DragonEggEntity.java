package wolfshotz.dml.egg;


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
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.dragons.TameableDragonEntity;
import wolfshotz.dml.dragons.ai.LifeStageController;
import wolfshotz.dml.network.EggHatchPacket;

import java.util.Comparator;


public class DragonEggEntity extends Entity
{
    // constants
    public static final float WIDTH = 0.9f; // Roughly the same size as the dragon egg block box
    public static final float HEIGHT = 0.9f;
    public static final int CHECK_HABITAT_INTERVAL = 200;
    public static final int DEFAULT_HATCH_TIME = 12000;
    public static final float EGG_WIGGLE_THRESHOLD = DragonEggEntity.DEFAULT_HATCH_TIME * 0.25f;
    public static final String NBT_HATCH_TIME = "HatchTime";
    public static final String NBT_BREED = "Type";
    private static final float EGG_CRACK_THRESHOLD = EGG_WIGGLE_THRESHOLD * 0.9f;
    private static final float EGG_WIGGLE_BASE_CHANCE = 20;

    public static final DataParameter<Integer> EGG_TYPE = EntityDataManager.createKey(DragonEggEntity.class, DataSerializers.VARINT);

    public int hatchTime;
    public DragonEggType egg;
//    float eggWiggleX, eggWiggleZ;

    public DragonEggEntity(EntityType<? extends DragonEggEntity> type, World world)
    {
        super(type, world);
    }

    public DragonEggEntity(DragonEggType egg, World world)
    {
        super(DMLRegistry.EGG.get(), world);
        setEggType(egg);
        this.hatchTime = DEFAULT_HATCH_TIME;
    }

    @Override
    protected void registerData() { dataManager.register(EGG_TYPE, -1); }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        this.hatchTime = compound.getInt(NBT_HATCH_TIME);
        setEggType(DragonEggType.INSTANCES.get(compound.getInt(NBT_BREED)));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        compound.putInt(NBT_HATCH_TIME, hatchTime);
        compound.putInt(NBT_BREED, DragonEggType.INSTANCES.indexOf(egg));
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (key.equals(EGG_TYPE)) egg = DragonEggType.INSTANCES.get(dataManager.get(EGG_TYPE));
    }

    public void setEggType(DragonEggType type) { dataManager.set(EGG_TYPE, DragonEggType.INSTANCES.indexOf(type)); }

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
        if (!hasNoGravity())
        {
            setMotion(getMotion().add(0, -0.04d, 0));
        }

        move(MoverType.SELF, getMotion());
        setMotion(getMotion().mul(0.3d, 0.98d, 0.3d));
        world.getEntitiesInAABBexcluding(this, getBoundingBox(), e -> !(e instanceof PlayerEntity))
                .forEach(this::applyEntityCollision);

        if (!world.isRemote)
        {
            if (egg == null)
            { // Somehow this happened, we need to remove it :(
                DragonMountsLegacy.L.error("null entity type for egg, removing...");
                remove();
                return;
            }

            // Update habitat - before hatch to give that one tick chance
            if (ticksExisted % CHECK_HABITAT_INTERVAL == 0) updateHabitat();

            // hatch!
            if (--hatchTime <= 0)
            {
                EggHatchPacket.send(this); // notify clients
                hatch();
                return; // Were hatching! drop the cock and lets go!
            }

//            // wiggle todo
//            if (hatchTime > EGG_WIGGLE_THRESHOLD)
//            {
//                float wiggleChance = (hatchTime - EGG_WIGGLE_THRESHOLD) / EGG_WIGGLE_BASE_CHANCE * (1 - EGG_WIGGLE_THRESHOLD);
//
//                if (eggWiggleX > 0) eggWiggleX--;
//                else if (rand.nextFloat() < wiggleChance)
//                {
//                    eggWiggleX = rand.nextBoolean() ? 10 : 20;
//                    if (hatchTime > EGG_CRACK_THRESHOLD) crack();
//                }
//
//                if (eggWiggleZ > 0) eggWiggleZ--;
//                else if (rand.nextFloat() < wiggleChance)
//                {
//                    eggWiggleZ = rand.nextBoolean() ? 10 : 20;
//                    if (hatchTime > EGG_CRACK_THRESHOLD) crack();
//                }
//            }
        }
        else
        {
            double px = getPosX() + (rand.nextDouble() - 0.5);
            double py = getPosY() + (rand.nextDouble() - 0.5);
            double pz = getPosZ() + (rand.nextDouble() - 0.5);

            if (egg.getBreed() == DMLRegistry.ENDER_DRAGON.get())
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
        entityDropItem(egg.getEggBlock());
        remove();
        return false;
    }

    public void updateHabitat()
    {
        DragonEggType.INSTANCES.stream()
                .filter(t -> t.getHabitatPoints(this) > 2)
                .max(Comparator.comparingInt(t -> t.getHabitatPoints(this)))
                .ifPresent(this::setEggType);
    }

    public void hatch()
    {
        if (world.isRemote) crack();
        else
        {
            TameableDragonEntity dragon = egg.getBreed().create(world);
            dragon.getLifeStageController().setLifeStage(LifeStageController.EnumLifeStage.HATCHLING);
            dragon.setPosition(getPosX(), getPosY(), getPosZ());
            dragon.setCustomName(getCustomName());
            world.addEntity(dragon);
        }

        remove();
    }

    public void crack()
    {
        world.playEvent(2001, getPosition(), Block.getStateId(egg.getEggBlock().getDefaultState()));
        world.playSound(getPosX(), getPosY(), getPosZ(), SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 1, 1, false);
    }
}
