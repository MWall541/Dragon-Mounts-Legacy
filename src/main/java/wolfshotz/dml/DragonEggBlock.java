package wolfshotz.dml;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ObjectHolder;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;

/**
 * OG Dragon Mounts used meta-data to differentiate between the different egg breed types
 * Here, we will use blockstates.
 */
@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID)
public class DragonEggBlock extends net.minecraft.block.DragonEggBlock
{
    @ObjectHolder(DragonMountsLegacy.MOD_ID + ":dragon_egg")
    public static final Block DRAGON_EGG = null;

    public static final EnumProperty<EnumEggTypes> BREED = EnumProperty.create("breed", EnumEggTypes.class);

    public DragonEggBlock(Properties properties)
    {
        super(properties);
        setDefaultState(getStateContainer().getBaseState().with(BREED, EnumEggTypes.AETHER));
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
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        if (state.get(BREED) == EnumEggTypes.ENDER) super.onBlockClicked(state, worldIn, pos, player);
    }

    public static void startHatching(BlockState state, World world, BlockPos pos)
    {
        if (!world.isRemote)
        {
            DragonEggEntity egg = new DragonEggEntity(state.get(BREED), world);
            egg.setPosition(pos.getX() + 0.5d, pos.getY() + 0.1d, pos.getZ() + 0.5d);
            world.addEntity(egg);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @SubscribeEvent
    public static void onVanillaEggActivate(PlayerInteractEvent.RightClickBlock evt)
    {
        World world = evt.getWorld();
        Block block = world.getBlockState(evt.getPos()).getBlock();
        if (block instanceof net.minecraft.block.DragonEggBlock)
        {
            evt.setCanceled(true);
            evt.setUseBlock(Event.Result.DENY);
            startHatching(DRAGON_EGG.getDefaultState().with(BREED, EnumEggTypes.ENDER), world, evt.getPos());
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_)
    {
        startHatching(state, worldIn, pos);
        return ActionResultType.SUCCESS;
    }
}
