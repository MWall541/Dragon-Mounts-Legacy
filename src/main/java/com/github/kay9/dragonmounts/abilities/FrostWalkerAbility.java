package com.github.kay9.dragonmounts.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Supplier;


public class FrostWalkerAbility implements Ability
{
    public static final Codec<Factory> CODEC = Codec.FLOAT
            .xmap(Factory::new, Factory::radiusMultiplier)
            .fieldOf("radius_multiplier")
            .codec();

    private final float radiusMultiplier;

    public FrostWalkerAbility(float radiusMultiplier)
    {
        this.radiusMultiplier = radiusMultiplier;
    }

    @Override
    public void initialize(TameableDragon dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, 0);
    }

    @Override
    public void close(TameableDragon dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, BlockPathTypes.WATER.getMalus());
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        var level = dragon.getLevel();

        if (dragon.tickCount % 3 != 0) return; // no need for expensive calcs EVERY tick
        if (level.isClientSide() || dragon.getAgeProgress() < 0.5)
            return; // only juveniles and older can frost walk

        // taken from and modified of FrostWalkerEnchantment#onEntityMoved
        var radius = Math.max(radiusMultiplier * dragon.getScale(), 1) + 3f;
        var pos = dragon.blockPosition();

        for (BlockPos carat : BlockPos.betweenClosed(pos.offset((-radius), -2.0D, (-radius)), pos.offset(radius, -1.0D, radius)))
        {
            if (!carat.closerToCenterThan(dragon.position(), radius))
                continue; // circle

            var currentState = level.getBlockState(carat);

            if (currentState.getMaterial() != Material.WATER || currentState.getValue(LiquidBlock.LEVEL) != 0)
                continue; // only full water blocks
            if (ForgeEventFactory.onBlockPlace(dragon, BlockSnapshot.create(level.dimension(), level, carat), Direction.UP))
                continue;

            var ice = Blocks.FROSTED_ICE.defaultBlockState();

            if (!ice.canSurvive(level, carat) || !level.isUnobstructed(ice, carat, CollisionContext.empty()))
                continue;

            var mPos = carat.mutable().move(0, 1, 0);

            if (!level.getBlockState(mPos).isAir())
                continue;

            level.setBlockAndUpdate(carat, ice);
            level.scheduleTick(carat, Blocks.FROSTED_ICE, Mth.nextInt(dragon.getRandom(), 60, 120));
        }
    }

    public record Factory(float radiusMultiplier) implements Ability.Factory<FrostWalkerAbility>
    {
        @Override
        public FrostWalkerAbility create()
        {
            return new FrostWalkerAbility(radiusMultiplier());
        }

        @Override
        public ResourceLocation type()
        {
            return FROST_WALKER;
        }
    }
}
