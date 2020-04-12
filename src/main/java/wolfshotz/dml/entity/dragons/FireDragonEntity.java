package wolfshotz.dml.entity.dragons;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.util.BetterBlockMatcher;

public class FireDragonEntity extends TameableDragonEntity
{
    public FireDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.ON_FIRE, DamageSource.IN_FIRE, DamageSource.LAVA);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (isInWater()) attackEntityFrom(DamageSource.DROWN, 2.0f);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        AxisAlignedBB aabb = egg.getBoundingBox().grow(2);
        BlockPos pos1 = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos pos2 = new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ);
        return BlockPos.getAllInBox(pos1, pos2)
                .map(p -> egg.world.getBlockState(p).getBlock())
                .anyMatch(new BetterBlockMatcher(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK));
    }
}
