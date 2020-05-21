package wolfshotz.dml.egg;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.dragons.TameableDragonEntity;

import java.util.function.Supplier;

/**
 * OG Dragon Mounts used meta-data to differentiate between the different egg breed types
 * Here, we will use blockstates.
 */
@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID)
public class DragonEggBlock extends net.minecraft.block.DragonEggBlock
{
    public final Supplier<EntityType<TameableDragonEntity>> breed;

    public DragonEggBlock(Supplier<EntityType<TameableDragonEntity>> breed)
    {
        super(Block.Properties.from(Blocks.DRAGON_EGG).hardnessAndResistance(0, 9));
        this.breed = breed;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        if (breed.get() == DMLRegistry.ENDER_DRAGON.get()) super.onBlockClicked(state, worldIn, pos, player);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_)
    {
        startHatching(breed.get(), worldIn, pos);
        return ActionResultType.SUCCESS;
    }

    public static void startHatching(EntityType<TameableDragonEntity> type, World world, BlockPos pos)
    {
        if (!world.isRemote)
        {
            DragonEggEntity egg = new DragonEggEntity(DragonEggType.lookUp(type), world);
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
        if (block == Blocks.DRAGON_EGG)
        {
            evt.setCanceled(true);
            evt.setUseBlock(Event.Result.DENY);
            startHatching(DMLRegistry.ENDER_DRAGON.get(), world, evt.getPos());
        }
    }
}
