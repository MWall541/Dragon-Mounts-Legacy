package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReaperStepAbility extends FootprintAbility implements Ability.Factory<ReaperStepAbility>
{
    public static final ReaperStepAbility INSTANCE = new ReaperStepAbility();
    public static final MapCodec<ReaperStepAbility> CODEC = MapCodec.unit(INSTANCE);

    public static final TagKey<Block> PLANT_DEATH_TAG = BlockTags.create(DragonMountsLegacy.id("reaper_plant_death"));
    public static final TagKey<Block> PLANT_DESTRUCTION_TAG = BlockTags.create(DragonMountsLegacy.id("reaper_plant_destruction"));
    public static final TagKey<Block> REAPER_TRANSFORM = BlockTags.create(DragonMountsLegacy.id("reaper_transform"));

    @Override
    protected void placeFootprint(TameableDragon dragon, BlockPos pos)
    {
        var level = dragon.level();
        var steppingOn = level.getBlockState(pos);
        if (steppingOn.is(PLANT_DEATH_TAG))
        {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.1f, 2f);
            ((ServerLevel) level).sendParticles(ParticleTypes.SOUL, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 1, 0, 0.05);

            var bs = (dragon.getRandom().nextDouble() < 0.05? Blocks.WITHER_ROSE : Blocks.DEAD_BUSH).defaultBlockState();
            level.setBlock(pos, bs, Block.UPDATE_ALL);
        }
        else if (steppingOn.is(PLANT_DESTRUCTION_TAG))
        {
            level.destroyBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.1f, 2f);
            ((ServerLevel) level).sendParticles(ParticleTypes.SOUL, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 1, 0, 0.05);

            var sticks = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.STICK));
            sticks.setPickUpDelay(40);
            level.addFreshEntity(sticks);
        }
        else if ((steppingOn = level.getBlockState(pos = pos.below())).is(REAPER_TRANSFORM)) // todo: this isn't very customizable...
        {
            if (steppingOn.is(Blocks.GRASS_BLOCK))
                destroyAndReplace(level, Blocks.DIRT.defaultBlockState(), pos);
            else if (steppingOn.is(BlockTags.SAND))
                destroyAndReplace(level, Blocks.SOUL_SAND.defaultBlockState(), pos);
            else if (steppingOn.is(BlockTags.DIRT))
                destroyAndReplace(level, Blocks.SOUL_SOIL.defaultBlockState(), pos);

        }
    }

    @Override
    protected float getFootprintChance(TameableDragon dragon)
    {
        return 0.025f;
    }

    private static void destroyAndReplace(Level level, BlockState state, BlockPos pos)
    {
        level.destroyBlock(pos, false);
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    @Override
    public ReaperStepAbility create()
    {
        return this;
    }

    @Override
    public ResourceLocation type()
    {
        return REAPER_STEP;
    }
}
