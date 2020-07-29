package wolfshotz.dml.entities;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wolfshotz.dml.util.BetterBlockMatcher;

public class ForestDragonEntity extends TameableDragonEntity
{
    public ForestDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePos = egg.getPosition();
        BetterBlockMatcher matcher = new BetterBlockMatcher(BlockTags.LEAVES.getAllElements()).add(BlockTags.SAPLINGS.getAllElements()).add(BlockTags.LOGS.getAllElements()).add(Blocks.MOSSY_COBBLESTONE, Blocks.VINE);
        points += (int) BlockPos.getAllInBox(basePos.add(1, 1, 1), basePos.add(-1, -1, -1))
                .filter(pos -> matcher.test(egg.world.getBlockState(pos).getBlock()))
                .count();
        return points;
    }
}
