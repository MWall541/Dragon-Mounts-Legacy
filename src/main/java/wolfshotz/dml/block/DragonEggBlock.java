package wolfshotz.dml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ObjectHolder;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragonegg.EggBreedTypes;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

/**
 * OG Dragon Mounts used meta-data to differentiate between the different egg breed types
 * Here, we will use blockstates.
 */
public class DragonEggBlock extends net.minecraft.block.DragonEggBlock
{
    @ObjectHolder(DragonMountsLegacy.MOD_ID + ":dragon_egg")
    public static final Block DRAGON_EGG = null;

    public static final EnumProperty<EggBreedTypes> BREED = EnumProperty.create("breed", EggBreedTypes.class);

    public DragonEggBlock(Properties properties)
    {
        super(properties);
        setDefaultState(getStateContainer().getBaseState().with(BREED, EggBreedTypes.AETHER));
    }

    public static void register(IEventBus bus)
    {
        bus.addGenericListener(Block.class, (RegistryEvent.Register<Block> e) -> e.getRegistry().register(
                new DragonEggBlock(Block.Properties.from(Blocks.DRAGON_EGG)).setRegistryName("dragon_egg")
        ));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(BREED);
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {}

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_)
    {
        if (!worldIn.isRemote)
        {
            EntityType<TameableDragonEntity> type = EggBreedTypes.getTypeByBlockState(state);
            DragonEggEntity egg = new DragonEggEntity(type, worldIn);
            egg.setPosition(pos.getX() + 0.5d, pos.getY() + 0.1d, pos.getZ() + 0.5d);
            worldIn.addEntity(egg);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        return ActionResultType.SUCCESS;
    }
}
