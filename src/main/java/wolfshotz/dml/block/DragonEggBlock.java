package wolfshotz.dml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;

/**
 * OG Dragon Mounts used meta-data to differentiate between the different egg breed types
 * Here, we will use blockstates.
 */
public class DragonEggBlock extends net.minecraft.block.DragonEggBlock
{
    public static final EnumProperty<EggBreedTypes> BREED = EnumProperty.create("breed", EggBreedTypes.class);

    public DragonEggBlock(Properties properties)
    {
        super(properties);
        setDefaultState(getStateContainer().getBaseState().with(BREED, EggBreedTypes.ENDER));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(BREED);
    }

    public enum EggBreedTypes implements IStringSerializable
    {
        ENDER("ender"),
        FIRE("fire");

        private final String name;

        EggBreedTypes(String name) { this.name = name; }

        @Override
        public String getName() { return name; }
    }
}
