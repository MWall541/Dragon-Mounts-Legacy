package wolfshotz.dml.egg;

import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class DragonSpawnEggItem extends Item
{
    private final DragonEggType egg;

    public DragonSpawnEggItem(DragonEggType egg)
    {
        super(new Item.Properties().group(ItemGroup.MISC));
        this.egg = egg;
    }

    /**
     * Called when this item is used when targetting a Block
     */
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        if (!world.isRemote)
        {
            ItemStack itemstack = context.getItem();
            BlockPos blockpos = context.getPos();
            Direction direction = context.getFace();

            BlockPos blockpos1 = blockpos.offset(direction);
            if (world.getBlockState(blockpos).getCollisionShape(world, blockpos).isEmpty())
                blockpos1 = blockpos;

            if (egg.getBreed().spawn(world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null)
                itemstack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    public int getColor(int tintIndex) { return tintIndex == 0? egg.primColor : egg.secColor; }
}
