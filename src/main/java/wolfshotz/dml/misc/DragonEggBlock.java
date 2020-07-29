package wolfshotz.dml.misc;

import com.google.common.collect.ImmutableMap;
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
import org.apache.commons.lang3.tuple.Pair;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.entities.DragonEggEntity;
import wolfshotz.dml.entities.TameableDragonEntity;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * OG Dragon Mounts used meta-data to differentiate between the different egg breed types
 * Here, we will use blockstates.
 */
public class DragonEggBlock extends net.minecraft.block.DragonEggBlock
{
    private static ImmutableMap<EntityType<?>, Block> LOOK_UP;

    public final Supplier<EntityType<? extends TameableDragonEntity>> breed;
    private final ToIntFunction<DragonEggEntity> habitatFunc;
    private final int primColor;
    private final int secColor;

    public DragonEggBlock(Supplier<EntityType<? extends TameableDragonEntity>> breed, ToIntFunction<DragonEggEntity> habitatPoints, int primColor, int secColor)
    {
        super(Block.Properties.from(Blocks.DRAGON_EGG).hardnessAndResistance(1, 9));
        this.habitatFunc = habitatPoints;
        this.breed = breed;
        this.primColor = primColor;
        this.secColor = secColor;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        if (breed.get() == DMLRegistry.ENDER_DRAGON_ENTITY.get()) super.onBlockClicked(state, worldIn, pos, player);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_)
    {
        startHatching(this, worldIn, pos);
        return ActionResultType.SUCCESS;
    }

    public int getHabitatPoints(DragonEggEntity egg) { return habitatFunc.applyAsInt(egg); }

    public Pair<Integer, Integer> getColors() { return Pair.of(primColor, secColor); }

    public float getColorR(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 16) & 0xFF) / 255f;
    }

    public float getColorG(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 8) & 0xFF) / 255f;
    }

    public float getColorB(boolean primary)
    {
        int color = primary? primColor : secColor;
        return (color & 0xFF) / 255f;
    }

    public static void startHatching(Block block, World world, BlockPos pos)
    {
        if (!world.isRemote)
        {
            DragonEggEntity egg = DMLRegistry.EGG_ENTITY.get().create(world);
            egg.setEggType(block);
            egg.setPosition(pos.getX() + 0.5d, pos.getY() + 0.1d, pos.getZ() + 0.5d);
            world.addEntity(egg);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public static void onVanillaEggActivate(PlayerInteractEvent.RightClickBlock evt)
    {
        World world = evt.getWorld();
        Block block = world.getBlockState(evt.getPos()).getBlock();
        if (block == Blocks.DRAGON_EGG)
        {
            evt.setCanceled(true);
            evt.setUseBlock(Event.Result.DENY);
            startHatching(DMLRegistry.ENDER_EGG_BLOCK.get(), world, evt.getPos());
        }
    }

    // todo clean all this up
    public static DragonEggBlock lookUp(EntityType<?> dragonType)
    {
        // lazily instatiate, for registry reasons
        if (LOOK_UP == null) LOOK_UP = ImmutableMap.<EntityType<?>, Block>builder()
                .put(DMLRegistry.AETHER_DRAGON_ENTITY.get(), DMLRegistry.AETHER_EGG_BLOCK.get())
                .put(DMLRegistry.ENDER_DRAGON_ENTITY.get(), DMLRegistry.ENDER_EGG_BLOCK.get())
                .put(DMLRegistry.FIRE_DRAGON_ENTITY.get(), DMLRegistry.FIRE_EGG_BLOCK.get())
                .put(DMLRegistry.FOREST_DRAGON_ENTITY.get(), DMLRegistry.FOREST_EGG_BLOCK.get())
                .put(DMLRegistry.GHOST_DRAGON_ENTITY.get(), DMLRegistry.GHOST_EGG_BLOCK.get())
                .put(DMLRegistry.ICE_DRAGON_ENTITY.get(), DMLRegistry.ICE_EGG_BLOCK.get())
                .put(DMLRegistry.NETHER_DRAGON_ENTITY.get(), DMLRegistry.NETHER_EGG_BLOCK.get())
                .put(DMLRegistry.WATER_DRAGON_ENTITY.get(), DMLRegistry.WATER_EGG_BLOCK.get())
                .build();

        return (DragonEggBlock) LOOK_UP.getOrDefault(dragonType, DMLRegistry.AETHER_EGG_BLOCK.get());
    }
}
