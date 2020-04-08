package wolfshotz.dml.entity.dragons;

import com.google.common.base.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.util.BetterBlockMatcher;

public class IceDragonEntity extends TameableDragonEntity
{
    public IceDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!this.world.isRemote && isAlive()) freezeWaterBelow();
    }

    public void freezeWaterBelow()
    {
        BlockPos pos = getPosition();
        if (!Objects.equal(new BlockPos(prevPosX, prevPosY, prevPosZ), pos))
        {
            BlockState blockstate = Blocks.FROSTED_ICE.getDefaultState();
            float scale = (float) Math.min(16, 2 + (int) (2 * getScale()));
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();

            for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-scale, -1.0D, -scale), pos.add(scale, -1.0D, scale)))
            {
                mutablePos.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                if (!world.getBlockState(mutablePos).isAir(world, pos)) continue;
                if (!blockpos.withinDistance(getPositionVec(), scale)) continue;

                BlockState blockstate2 = world.getBlockState(blockpos);
                boolean isFull = blockstate2.getBlock() == Blocks.WATER && blockstate2.get(FlowingFluidBlock.LEVEL) == 0;
                if (isFull && world.func_226663_a_(blockstate, blockpos, ISelectionContext.dummy()))
                {
                    world.setBlockState(blockpos, blockstate);
                    world.getPendingBlockTicks().scheduleTick(blockpos, Blocks.FROSTED_ICE, MathHelper.nextInt(getRNG(), 60, 120));
                }
            }
        }
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
