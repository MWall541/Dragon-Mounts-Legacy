package wolfshotz.dml.entity.dragons.ai.goals;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

import java.util.Random;

public class DragonLandGoal extends Goal
{
    private final TameableDragonEntity dragon;
    private BlockPos landingPos;

    public DragonLandGoal(TameableDragonEntity dragon)
    {
        this.dragon = dragon;
    }

    @Override
    public boolean shouldExecute()
    {
        return dragon.isFlying() && dragon.getRidingPlayer() == null && findLandingBlock();
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        return dragon.isFlying() && dragon.getRidingPlayer() == null && !dragon.getNavigator().noPath();
    }

    @Override
    public void startExecuting()
    {
        if (!dragon.getNavigator().tryMoveToXYZ(landingPos.getX(), landingPos.getY(), landingPos.getZ(), 1))
            dragon.getNavigator().tryMoveToXYZ(landingPos.getX(), landingPos.getY() - 4, landingPos.getZ(), 1);
    }

    private boolean findLandingBlock()
    {
        Random rand = dragon.getRNG();
        // get current entity position
        landingPos = dragon.getPosition();

        // add some variance
        int followRange = MathHelper.floor(dragon.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getValue());
        int ox = followRange - rand.nextInt(followRange) * 2;
        int oz = followRange - rand.nextInt(followRange) * 2;
        landingPos = landingPos.add(ox, 0, oz);

        // get ground block
        landingPos = dragon.world.getHeight(Heightmap.Type.WORLD_SURFACE, landingPos);

        // make sure the block below is solid
        return dragon.world.getBlockState(landingPos.down()).getMaterial().isSolid();
    }
}
