package wolfshotz.dml.entity.dragonegg;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.entity.dragons.ai.LifeStageController;
import wolfshotz.dml.util.network.NetworkUtils;

public class DragonEggEntity extends Entity implements IEntityAdditionalSpawnData
{
    // constants
    public static final float WIDTH = 0.9f; // Roughly the same size as the dragon egg block box
    public static final float HEIGHT = 0.9f;
    public static final int CHECK_HABITAT_INTERVAL = 100;
    public static final int DEFAULT_HATCH_TIME = 12000;
    public static final String HATCH_TIME = "HatchTime";
    public static final String BREED = "Breed";

    public int hatchTime;
    public EntityType<TameableDragonEntity> dragonBreed;
    public EggBreedTypes breed; // used on the client

    // used for client garbage. Yes I hate it to.
    public DragonEggEntity(World world)
    {
        super(DMLEntities.EGG.get(), world);
    }

    public DragonEggEntity(EntityType<TameableDragonEntity> breed, World world)
    {
        super(DMLEntities.EGG.get(), world);
        this.dragonBreed = breed;
        this.hatchTime = DEFAULT_HATCH_TIME;
    }

    @Override
    protected void registerData() {}

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        this.hatchTime = compound.getInt(HATCH_TIME);
        this.dragonBreed = (EntityType<TameableDragonEntity>) EntityType.byKey(compound.getString(BREED)).get();
    }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        compound.putInt(HATCH_TIME, hatchTime);
        compound.putString(BREED, EntityType.getKey(dragonBreed).toString());
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buf)
    {
        buf.writeEnumValue(EggBreedTypes.getByEntityType(dragonBreed));
    }

    @Override
    public void readSpawnData(PacketBuffer buf)
    {
        this.breed = buf.readEnumValue(EggBreedTypes.class);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (ticksExisted % CHECK_HABITAT_INTERVAL == 0) updateHabitat();

        if (!world.isRemote)
        {
            if (dragonBreed == null)
            { // Somehow this happened, we need to remove it :(
                DragonMountsLegacy.L.error("null entity type for egg, removing...");
                remove();
                return;
            }

            if (--hatchTime <= 0)
            {
                NetworkUtils.sendEggHatchPacket(this); // notify clients
                hatch();
            }

            if (!hasNoGravity())
            {
                setMotion(getMotion().add(0, -0.04d, 0));
                move(MoverType.SELF, getMotion());
            }
        }
        else
        {
            double px = getPosX() + (rand.nextDouble() - 0.5);
            double py = getPosY() + (rand.nextDouble() - 0.5);
            double pz = getPosZ() + (rand.nextDouble() - 0.5);

            if (breed == EggBreedTypes.ENDER)
            {
                double ox = (rand.nextDouble() - 0.5) * 2;
                double oy = (rand.nextDouble() - 0.5) * 2;
                double oz = (rand.nextDouble() - 0.5) * 2;
                world.addParticle(ParticleTypes.PORTAL, px, py, pz, ox, oy, oz);
            }
            else world.addParticle(new RedstoneParticleData(1, 1, 1, 1), px, py + 1, pz, 0, 0, 0);
        }
    }

    private void updateHabitat()
    {
        EntityType<TameableDragonEntity> type = EggBreedTypes.getTypeByTest(this);
        if (type == null) return;
        dragonBreed = type;
        if (world.isRemote) breed = EggBreedTypes.getByEntityType(type);
    }

    public void hatch()
    {
        if (world.isRemote)
        {

        }
        else
        {
            TameableDragonEntity dragon = dragonBreed.create(world);
            dragon.lifeStageController.setLifeStage(LifeStageController.EnumLifeStage.HATCHLING);
            dragon.setPosition(getPosX(), getPosY(), getPosZ());
            world.addEntity(dragon);
        }

        remove();
    }
}
