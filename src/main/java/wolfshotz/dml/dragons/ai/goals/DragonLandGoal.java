package wolfshotz.dml.dragons.ai.goals;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;
import wolfshotz.dml.dragons.TameableDragonEntity;

import java.util.EnumSet;
import java.util.Random;

public class DragonLandGoal extends Goal
{
    private final TameableDragonEntity dragon;
    private BlockPos landingPos;

    public DragonLandGoal(TameableDragonEntity dragon)
    {
        this.dragon = dragon;
        setMutexFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.TARGET));
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
    public void tick()
    {
        if (dragon.getNavigator().noPath()) startExecuting();
    }

    @Override
    public void startExecuting()
    {
        if (!dragon.getNavigator().tryMoveToXYZ(landingPos.getX(), landingPos.getY(), landingPos.getZ(), 1))
            // can't seem to get to it, just float down.
            dragon.getNavigator().tryMoveToXYZ(dragon.getPosX(), dragon.getPosY() - 4, dragon.getPosZ(), 1);
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
