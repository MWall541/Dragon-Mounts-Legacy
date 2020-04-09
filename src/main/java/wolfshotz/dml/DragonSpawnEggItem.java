package wolfshotz.dml;

import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;

import java.util.Objects;
import java.util.stream.Stream;

public class DragonSpawnEggItem extends Item
{
    public final EnumEggTypes dragonType;

    public DragonSpawnEggItem(EnumEggTypes type)
    {
        super(new Item.Properties().group(ItemGroup.MISC));
        this.dragonType = type;
    }

    public static Stream<DragonSpawnEggItem> getEggs()
    {
        return DragonMountsLegacy.ITEMS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(DragonSpawnEggItem.class::isInstance)
                .map(DragonSpawnEggItem.class::cast);
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

            if (dragonType.getType().spawn(world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null)
                itemstack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    public int getColor(int tintIndex)
    {
        Pair<Integer, Integer> colors = dragonType.getColors();
        return tintIndex == 0? colors.getLeft() : colors.getRight();
    }
}
