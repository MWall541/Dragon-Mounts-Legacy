package wolfshotz.dml.dragons;

import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import wolfshotz.dml.egg.DragonEggEntity;

public class WaterDragonEntity extends TameableDragonEntity
{
    public WaterDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        return egg.world.getBlockState(egg.getPosition()).getBlock() == Blocks.WATER;
    }

    @Override
    public void travel(Vec3d vec3d)
    {
//        if (isServer() && isInWater())
//        {
//            moveRelative(getAIMoveSpeed(), getMotion());
//            move(MoverType.SELF, getMotion());
//            setMotion(getMotion().scale(0.98d));
//        }
        super.travel(vec3d);
    }

    @Override
    public boolean shouldFly()
    { // we can fly in water!
        return canFly() && getAltitude() > ALTITUDE_FLYING_THRESHOLD;
    }

    @Override
    public CreatureAttribute getCreatureAttribute() { return CreatureAttribute.WATER; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean isPushedByWater() { return false; }

    @Override
    public boolean canBeRiddenInWater(Entity rider) { return true; }

    @Override
    protected float getWaterSlowDown() { return 1; }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePos = egg.getPosition();
        points += (int) BlockPos.getAllInBox(basePos.add(1, 1, 1), basePos.add(-1, -1, -1))
                .filter(pos -> egg.world.getFluidState(pos).isTagged(FluidTags.WATER))
                .count();
        if (egg.isInWater()) ++points;
        Biome inBiome = egg.world.getBiome(basePos);
        if (BiomeDictionary.hasType(inBiome, BiomeDictionary.Type.WATER)) points += 2;
        return points;
    }
}
