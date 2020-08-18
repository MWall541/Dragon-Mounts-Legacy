package wolfshotz.dml.entities;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wolfshotz.dml.util.BetterBlockMatcher;

public class FireDragonEntity extends TameableDragonEntity
{
    public FireDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.ON_FIRE, DamageSource.IN_FIRE, DamageSource.LAVA);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (isInWater()) attackEntityFrom(DamageSource.DROWN, 2.0f);
    }

    public static int getHabitatPoints(DragonEggEntity egg) // todo: make a base method and have it take in a tag for basic habitat points
    {
        int points = 0;
        BlockPos basePos = egg.getPosition();
        BetterBlockMatcher matcher = new BetterBlockMatcher(Blocks.FIRE, Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(basePos.add(1, 1, 1), basePos.add(-1, -1, -1)))
            if (matcher.test(egg.world.getBlockState(pos))) ++points;
        if (egg.isInLava()) ++points;
        return points;
    }
}
