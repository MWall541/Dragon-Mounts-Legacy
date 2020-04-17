package wolfshotz.dml.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;

/**
 * Used to store the EnumEggTypes for use with the DragonEggBlock
 * The type is grabbed when #getStateForPlacement is called
 */
public class DragonEggBlockItem extends BlockItem
{
    private final EnumEggTypes type;

    public DragonEggBlockItem(EnumEggTypes type)
    {
        super(DragonEggBlock.INSTANCE, new Item.Properties().group(ItemGroup.MISC));
        this.type = type;
    }

    @Override
    public String getTranslationKey()
    {
        return getDefaultTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        if (isInGroup(group)) items.add(new ItemStack(this));
    }

    public EnumEggTypes getType() { return type; }
}
