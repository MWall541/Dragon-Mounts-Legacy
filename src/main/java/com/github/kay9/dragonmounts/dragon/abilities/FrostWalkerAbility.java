package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;


public class FrostWalkerAbility implements Ability, Ability.Factory<FrostWalkerAbility>
{
    public static final MapCodec<FrostWalkerAbility> CODEC = Codec.FLOAT
            .xmap(FrostWalkerAbility::new, a -> a.radiusMultiplier)
            .fieldOf("radius_multiplier");

    private final float radiusMultiplier;

    protected FrostWalkerAbility(float radiusMultiplier)
    {
        this.radiusMultiplier = radiusMultiplier;
    }

    public static FrostWalkerAbility create(float radiusMultiplier)
    {
        return new FrostWalkerAbility(radiusMultiplier);
    }

    @Override
    public void initialize(TameableDragon dragon)
    {
        dragon.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    public void tick(TameableDragon dragon)
    {
        var level = dragon.level();

        if (dragon.tickCount % 3 != 0) return; // no need for expensive calcs EVERY tick
        if (level.isClientSide() || dragon.getAgeProgress() < 0.5)
            return; // only juveniles and older can frost walk

        // taken from and modified of FrostWalkerEnchantment#onEntityMoved
        var radius = (int) (Math.max(radiusMultiplier * dragon.getAgeScale(), 1) + 3);
        var pos = dragon.blockPosition();

        for (BlockPos carat : BlockPos.betweenClosed(pos.offset((-radius), -2, (-radius)), pos.offset(radius, -1, radius)))
        {
            if (!carat.closerToCenterThan(dragon.position(), radius))
                continue; // circle

            var currentState = level.getBlockState(carat);

            if (currentState != FrostedIceBlock.meltsInto())
                continue;
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

    @Override
    public FrostWalkerAbility create()
    {
        return this;
    }

    @Override
    public MapCodec<? extends Factory<? extends Ability>> codec()
    {
        return CODEC;
    }
}
