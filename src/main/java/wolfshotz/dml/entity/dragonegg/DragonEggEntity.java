package wolfshotz.dml.entity.dragonegg;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import wolfshotz.dml.util.network.NetworkUtils;

public class DragonEggEntity extends Entity
{
    // constants
    public static final float WIDTH = 0.9f; // Roughly the same size as the dragon egg block box
    public static final float HEIGHT = 0.9f;
    public static final String HATCH_TIME = "HatchTime";

    public int hatchTime;

    public DragonEggEntity(EntityType<?> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {}

    @Override
    protected void readAdditional(CompoundNBT compound)
    {
        this.hatchTime = compound.getInt(HATCH_TIME);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound)
    {
        compound.putInt(HATCH_TIME, hatchTime);
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (--hatchTime <= 0)
        {
            NetworkUtils.sendEggHatchPacket(this); // notify clients
            hatch();
            return; // We're hatching! don't do anything else!
        }


    }

    public void hatch()
    {

    }
}
