package wolfshotz.dml.entity.dragons;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.util.BetterBlockMatcher;

public class IceDragonEntity extends TameableDragonEntity
{
    public IceDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        AxisAlignedBB aabb = egg.getBoundingBox().grow(2);
        BlockPos pos1 = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos pos2 = new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ);
        return BlockPos.getAllInBox(pos1, pos2)
                .map(p -> egg.world.getBlockState(p).getBlock())
                .anyMatch(new BetterBlockMatcher(BlockTags.ICE.getAllElements()).add(Blocks.SNOW, Blocks.SNOW_BLOCK));
    }
}
