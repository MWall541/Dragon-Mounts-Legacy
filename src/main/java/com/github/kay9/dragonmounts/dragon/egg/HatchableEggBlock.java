package com.github.kay9.dragonmounts.dragon.egg;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@SuppressWarnings("deprecation")
public class HatchableEggBlock extends DragonEggBlock implements EntityBlock, SimpleWaterloggedBlock
{
    public static final IntegerProperty HATCH_STAGE = IntegerProperty.create("hatch_stage", 0, 3);
    public static final BooleanProperty HATCHING = BooleanProperty.create("hatching");

    public static final float DEFAULT_HATCH_CHANCE = 0.1f;

    public static final String NBT_HATCH_STAGE = "hatch_stage";
    public static final String NBT_BREED = TameableDragon.NBT_BREED;
    public static final String NBT_NAME = "CustomName";

    public HatchableEggBlock()
    {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0f, 9f).lightLevel(s -> 1).noOcclusion());
        registerDefaultState(defaultBlockState()
                .setValue(HATCH_STAGE, 0)
                .setValue(HATCHING, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HATCH_STAGE, HATCHING, WATERLOGGED);
    }

    public static void populateTab(BuildCreativeModeTabContentsEvent evt)
    {
        if (Minecraft.getInstance().level != null)
        {
            var reg = Minecraft.getInstance().level.registryAccess();
            for (DragonBreed breed : BreedRegistry.registry(reg))
                evt.accept(Item.create(breed, reg));
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static HatchableEggBlockEntity place(ServerLevel level, BlockPos pos, BlockState state, DragonBreed breed)
    {
        level.setBlock(pos, state, Block.UPDATE_ALL);

        // Forcibly add new BlockEntity, so we can set the specific breed.
        var data = ((HatchableEggBlockEntity) ((HatchableEggBlock) state.getBlock()).newBlockEntity(pos, state));
        data.setBreed(() -> breed);
        level.setBlockEntity(data);
        return data;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new HatchableEggBlockEntity(pPos, pState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player)
    {
        var breed = level.getBlockEntity(pos) instanceof HatchableEggBlockEntity e?
                e.getBreed() :
                BreedRegistry.getRandom(player.level().registryAccess(), player.getRandom());
        return Item.create(breed, player.level().registryAccess());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> type)
    {
        return type != DMLRegistry.EGG_BLOCK_ENTITY.get()? null :
                cast(((level, pos, state, be) -> be.tick(level, pos, state)));
    }

    @SuppressWarnings("unchecked")
    private static <F extends BlockEntityTicker<HatchableEggBlockEntity>, T> T cast(F from)
    {
        return (T) from;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (!pState.getValue(HATCHING))
        {
            if (!pLevel.isClientSide)
            {
                pLevel.setBlock(pPos, pState.setValue(HATCHING, true), Block.UPDATE_ALL);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos at, Player pPlayer)
    {
        if (level.getBlockEntity(at) instanceof HatchableEggBlockEntity e
                && e.getBreed().id(level.registryAccess()).getPath().equals("end")
                && !state.getValue(HATCHING))
            teleport(state, level, at); // retain original dragon egg teleport behavior
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos at, Player player, boolean willHarvest, FluidState fluid)
    {
        if (!player.getAbilities().instabuild
                && level.getBlockEntity(at) instanceof HatchableEggBlockEntity e
                && e.getBreed().id(level.registryAccess()).getPath().equals("end")
                && !state.getValue(HATCHING))
            return false; // retain original dragon egg teleport behavior; DON'T destroy!

        return super.onDestroyedByPlayer(state, level, at, player, willHarvest, fluid);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltips, TooltipFlag pFlag)
    {
        super.appendHoverText(stack, level, tooltips, pFlag);

        var tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        var stage = tag != null? tag.getString(NBT_HATCH_STAGE) : "0";
        tooltips.add(Component.translatable(getDescriptionId() + ".hatch_stage." + stage)
                .withStyle(ChatFormatting.GRAY));

        var player = Minecraft.getInstance().player;
        if (player != null && player.getAbilities().instabuild)
        {
            tooltips.add(CommonComponents.EMPTY);
            tooltips.add(Component.translatable(getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
            tooltips.add(CommonComponents.space().append(Component.translatable(getDescriptionId() + ".desc2")).withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction pDirection, BlockState pNeighborState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pNeighborPos)
    {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

        return super.updateShape(state, pDirection, pNeighborState, level, pCurrentPos, pNeighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED)? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
    {
        // Original logic trashes BlockEntity data. We need it, so do it ourselves.
        if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight())
        {
            CompoundTag tag = null;
            if (pLevel.getBlockEntity(pPos) instanceof HatchableEggBlockEntity e)
                tag = e.saveWithoutMetadata();

            var entity = FallingBlockEntity.fall(pLevel, pPos, pState); // this deletes the block. We need to cache the data first and then apply it.
            if (tag != null) entity.blockData = tag;
            falling(entity);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState)
    {
        return pState.getValue(HATCHING);
    }

    @Override // will only tick when HATCHING, according to isRandomlyTicking
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (!(level.getBlockEntity(pos) instanceof HatchableEggBlockEntity data)) return;
        int hatchStage = state.getValue(HATCH_STAGE);
        var finalStage = hatchStage == 3;

        if (random.nextFloat() < data.getBreed().hatchChance())
        {
            if (finalStage)
                hatch(level, pos);
            else
            {
                crack(level, pos);
                level.setBlock(pos, state.setValue(HATCH_STAGE, hatchStage + 1), Block.UPDATE_ALL);
            }

            return;
        }

        if (finalStage) // too far gone to change habitats now!
            crack(level, pos); // being closer to hatching creates more struggles to escape
        else if (DMLConfig.updateHabitats() && !data.getTransition().isRunning())
            data.updateHabitat();
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource random)
    {
        if (pState.getValue(HATCHING) && pLevel.getBlockEntity(pPos) instanceof HatchableEggBlockEntity e)
            for (int i = 0; i < random.nextIntBetweenInclusive(4, 7); i++)
                addHatchingParticles(e.getBreed(), pLevel, pPos, random);
    }

    private void crack(ServerLevel level, BlockPos pos)
    {
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.85f, 0.95f + level.getRandom().nextFloat() * 0.2f);
    }

    @SuppressWarnings("ConstantConditions")
    private void hatch(ServerLevel level, BlockPos pos)
    {
        var data = (HatchableEggBlockEntity) level.getBlockEntity(pos);
        var baby = DMLRegistry.DRAGON.get().create(level);

        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.2f, 0.95f + level.getRandom().nextFloat() * 0.2f);
        level.removeBlock(pos, false); // remove block AFTER data is cached

        baby.setBreedAndUpdate(data.getBreed());
        baby.setBaby(true);
        baby.setPos(pos.getX(), pos.getY(), pos.getZ());
        baby.setCustomName(data.getCustomName());
        level.addFreshEntity(baby);
    }

    public void addHatchingParticles(DragonBreed breed, Level level, BlockPos pos, RandomSource random)
    {
        double px = pos.getX() + random.nextDouble();
        double py = pos.getY() + random.nextDouble();
        double pz = pos.getZ() + random.nextDouble();
        double ox = 0;
        double oy = 0;
        double oz = 0;

        var particle = getHatchingParticles(breed, random);
        if (particle.getType() == ParticleTypes.DUST) py = pos.getY() + (random.nextDouble() - 0.5) + 1;
        else if (particle.getType() == ParticleTypes.PORTAL)
        {
            ox = (random.nextDouble() - 0.5) * 2;
            oy = (random.nextDouble() - 0.5) * 2;
            oz = (random.nextDouble() - 0.5) * 2;
        }

        level.addParticle(particle, px, py, pz, ox, oy, oz);
    }

    public static ParticleOptions getHatchingParticles(DragonBreed breed, RandomSource random)
    {
        return breed.hatchParticles().orElseGet(() -> dustParticleFor(breed, random));
    }

    public static DustParticleOptions dustParticleFor(DragonBreed breed, RandomSource random)
    {
        return new DustParticleOptions(Vec3.fromRGB24(random.nextDouble() < 0.75? breed.primaryColor() : breed.secondaryColor()).toVector3f(), 1);
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean overrideVanillaDragonEgg(Level level, BlockPos pos, Player player)
    {
        if (DMLConfig.allowEggOverride() && level.getBlockState(pos).is(Blocks.DRAGON_EGG))
        {
            var end = BreedRegistry.registry(level.registryAccess()).getOptional(DragonMountsLegacy.id("end"));
            if (end.isPresent())
            {
                if (level.isClientSide) player.swing(InteractionHand.MAIN_HAND);
                else
                {
                    var state = DMLRegistry.EGG_BLOCK.get().defaultBlockState().setValue(HATCHING, true);
                    place((ServerLevel) level, pos, state, end.get());
                }
                return true;
            }
        }
        return false;
    }

    // taken from DragonEggBlock#teleport
    private static void teleport(BlockState state, Level level, BlockPos pos)
    {
        var worldBorder = level.getWorldBorder();

        for (int i = 0; i < 1000; ++i) // excessive?
        {
            var teleportPos = pos.offset(level.random.nextInt(16) - level.random.nextInt(16), level.random.nextInt(8) - level.random.nextInt(8), level.random.nextInt(16) - level.random.nextInt(16));
            if (level.getBlockState(teleportPos).isAir() && worldBorder.isWithinBounds(teleportPos))
            {
                if (level.isClientSide)
                {
                    for (int j = 0; j < 128; ++j)
                    {
                        double d0 = level.random.nextDouble();
                        float f = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float f1 = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float f2 = (level.random.nextFloat() - 0.5F) * 0.2F;
                        double d1 = Mth.lerp(d0, teleportPos.getX(), pos.getX()) + (level.random.nextDouble() - 0.5D) + 0.5D;
                        double d2 = Mth.lerp(d0, teleportPos.getY(), pos.getY()) + level.random.nextDouble() - 0.5D;
                        double d3 = Mth.lerp(d0, teleportPos.getZ(), pos.getZ()) + (level.random.nextDouble() - 0.5D) + 0.5D;
                        level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, f, f1, f2);
                    }
                }
                else
                {
                    // Original Dragon Egg does not have a BlockEntity to account for,
                    // so our own teleport will now restore the block data.

                    var data = level.getBlockEntity(pos).saveWithoutMetadata();
                    level.removeBlock(pos, false);
                    level.setBlock(teleportPos, state, Block.UPDATE_CLIENTS);
                    level.getBlockEntity(teleportPos).load(data);
                }

                return;
            }
        }
    }

    public static class Item extends BlockItem
    {
        // "BlockStateTag"
        //      -- "hatch_stage"
        // "BlockEntityTag"
        //      -- "Breed"
        //      -- "CustomName"
        //      -- "TransitionerTag"
        //              -- "TransitionBreed"
        //              -- "TransitionTime"

        public Item()
        {
            super(DMLRegistry.EGG_BLOCK.get(), new net.minecraft.world.item.Item.Properties().rarity(Rarity.EPIC));
        }

        @Override
        public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
        {
            ensureExistingBreedType(stack, BlockItem.BLOCK_ENTITY_TAG); // extremely hacky to be doing it here... but there doesn't seem to be any other options.
            return super.initCapabilities(stack, nbt);
        }

        @Override
        public Component getName(ItemStack stack)
        {
            var tag = BlockItem.getBlockEntityData(stack);
            if (tag != null)
                return Component.translatable(getDescriptionId(), Component.translatable(DragonBreed.getTranslationKey(tag.getString(NBT_BREED))));
            return super.getName(stack);
        }

        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand)
        {
            if (player.getAbilities().instabuild && target instanceof TameableDragon dragon)
            {
                var tag = BlockItem.getBlockEntityData(stack);
                if (tag != null)
                {
                    dragon.setBreedAndUpdate(BreedRegistry.get(tag.getString(TameableDragon.NBT_BREED), player.level().registryAccess()));
                    return InteractionResult.sidedSuccess(player.level().isClientSide);
                }
            }
            return super.interactLivingEntity(stack, player, target, hand);
        }

        @Override
        public InteractionResult place(BlockPlaceContext pContext)
        {
            var result = super.place(pContext);
            if (result.consumesAction()
                    && pContext.getItemInHand().hasCustomHoverName()
                    && pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof HatchableEggBlockEntity e
            )
                e.setCustomName(pContext.getItemInHand().getHoverName());
            return result;
        }

        private static void ensureExistingBreedType(ItemStack stack, String subTag)
        {
            if (ServerLifecycleHooks.getCurrentServer() == null) return;

            if (!stack.hasTag()) stack.setTag(new CompoundTag());
            var blockEntityData = stack.getOrCreateTagElement(subTag);
            var breed = blockEntityData.getString(NBT_BREED);
            var reg = BreedRegistry.registry(ServerLifecycleHooks.getCurrentServer().registryAccess());

            if (breed.isEmpty() || !reg.containsKey(new ResourceLocation(breed))) // this item doesn't contain a breed yet?
            {
                breed = reg.getRandom(RandomSource.create()).orElseThrow().key().location().toString();
                blockEntityData.putString(NBT_BREED, breed); // assign one ourselves then.
            }
        }

        public static ItemStack create(DragonBreed breed, RegistryAccess reg)
        {
            var tag = new CompoundTag();
            tag.putString(TameableDragon.NBT_BREED, breed.id(reg).toString());

            var stack = new ItemStack(DMLRegistry.EGG_BLOCK_ITEM.get());
            BlockItem.setBlockEntityData(stack, DMLRegistry.EGG_BLOCK_ENTITY.get(), tag);

            return stack;
        }
    }
}
