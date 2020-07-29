package wolfshotz.dml.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class LazySpawnEggItem extends Item
{
    private final int primColor, secColor;
    private final Supplier<EntityType<?>> type;

    public LazySpawnEggItem(Supplier<EntityType<?>> type, int primColor, int secColor)
    {
        super(new Item.Properties().group(ItemGroup.MISC));
        this.primColor = primColor;
        this.secColor = secColor;
        this.type = type;
    }

    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        if (!world.isRemote)
        {
            ItemStack itemstack = context.getItem();
            BlockPos blockpos = context.getPos();
            Direction direction = context.getFace();
            BlockState blockstate = world.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (block == Blocks.SPAWNER)
            {
                TileEntity tileentity = world.getTileEntity(blockpos);
                if (tileentity instanceof MobSpawnerTileEntity)
                {
                    AbstractSpawner abstractspawner = ((MobSpawnerTileEntity) tileentity).getSpawnerBaseLogic();
                    EntityType<?> entitytype1 = this.getType(itemstack.getTag());
                    abstractspawner.setEntityType(entitytype1);
                    tileentity.markDirty();
                    world.notifyBlockUpdate(blockpos, blockstate, blockstate, 3);
                    itemstack.shrink(1);
                    return ActionResultType.SUCCESS;
                }
            }

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(world, blockpos).isEmpty()) blockpos1 = blockpos;
            else blockpos1 = blockpos.offset(direction);

            EntityType<?> entitytype = this.getType(itemstack.getTag());
            if (entitytype.spawn(world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null)
                itemstack.shrink(1);

        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (raytraceresult.getType() != RayTraceResult.Type.BLOCK) return ActionResult.resultPass(itemstack);
        else if (worldIn.isRemote) return ActionResult.resultSuccess(itemstack);
        else
        {
            BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) raytraceresult;
            BlockPos blockpos = blockraytraceresult.getPos();
            if (!(worldIn.getBlockState(blockpos).getBlock() instanceof FlowingFluidBlock))
                return ActionResult.resultPass(itemstack);
            else if (worldIn.isBlockModifiable(playerIn, blockpos) && playerIn.canPlayerEdit(blockpos, blockraytraceresult.getFace(), itemstack))
            {
                EntityType<?> entitytype = this.getType(itemstack.getTag());
                if (entitytype.spawn(worldIn, itemstack, playerIn, blockpos, SpawnReason.SPAWN_EGG, false, false) == null)
                    return ActionResult.resultPass(itemstack);
                else
                {
                    if (!playerIn.abilities.isCreativeMode) itemstack.shrink(1);

                    playerIn.addStat(Stats.ITEM_USED.get(this));
                    return ActionResult.resultSuccess(itemstack);
                }
            }
            else return ActionResult.resultFail(itemstack);
        }
    }

    public int getColor(int tintIndex) { return tintIndex == 0? primColor : secColor; }

    public EntityType<?> getType(@Nullable CompoundNBT tag)
    {
        if (tag != null && tag.contains("EntityTag", 10))
        {
            CompoundNBT compoundnbt = tag.getCompound("EntityTag");
            if (compoundnbt.contains("id", 8))
                return EntityType.byKey(compoundnbt.getString("id")).orElse(type.get());
        }

        return type.get();
    }
}
