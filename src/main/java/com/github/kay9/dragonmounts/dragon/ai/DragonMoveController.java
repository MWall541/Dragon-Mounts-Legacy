package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.util.DMLUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;

public class DragonMoveController extends MoveControl
{
    private final TameableDragon dragon;

    public DragonMoveController(TameableDragon dragon)
    {
        super(dragon);
        this.dragon = dragon;
    }

    // todo: dragon body rotates too quickly, rotation speed hardcoded in super
    @Override
    public void tick()
    {
        double yDif = getWantedY() - mob.getY();

        if (!dragon.isFlying())
        {
            // better not done here, should be in the navigator.
//            if (yDif > dragon.getStepHeight() && canLiftOff(dragon))
//            {
//                dragon.liftOff();
//            }
//            else
//            {
                // ground movement behavior if we don't want to fly
                super.tick();
                return;
//            }
        }

        if (operation == MoveControl.Operation.MOVE_TO)
        {
            operation = MoveControl.Operation.WAIT;
            double xDif = getWantedX() - mob.getX();
            double zDif = getWantedZ() - mob.getZ();
            double sq = xDif * xDif + yDif * yDif + zDif * zDif;
            if (sq < (double) 2.5000003E-7F)
            {
                mob.setYya(0.0F);
                mob.setZza(0.0F);
                return;
            }

            float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.FLYING_SPEED));
            double distSq = Math.sqrt(xDif * xDif + zDif * zDif);
            mob.setSpeed(speed);
            if (Math.abs(yDif) > (double) 1.0E-5F || Math.abs(distSq) > (double) 1.0E-5F)
            {
                // using a normalized difference in pos and wantedY is essential to prevent awkward up/down bobbing in the air.
                // speed attributes are tuned using max values of 0..1  (-1 is just down, so that works.)
                float moveVertical = Mth.clamp((float) yDif, -1f, 1f);
                mob.setYya(moveVertical * speed);
            }

            float yaw = (float) (Mth.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
            mob.setYRot(rotlerp(mob.getYRot(), yaw, 6));
        }
        else
        {
            mob.setYya(0);
            mob.setZza(0);
        }
    }

    public static boolean canLiftOff(TameableDragon dragon)
    {
        return dragon.canFly()
                && !dragon.isLeashed()
                && dragon.level().noCollision(dragon, dragon.getBoundingBox().move(0, dragon.getJumpPower(), 0));
    }
}