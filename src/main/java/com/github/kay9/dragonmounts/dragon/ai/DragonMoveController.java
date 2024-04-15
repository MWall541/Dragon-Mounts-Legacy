package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.util.DMLUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;

public class DragonMoveController extends MoveControl
{
    private static final float LIFT_OFF_HEIGHT = 5;

    private final TameableDragon dragon;

    public DragonMoveController(TameableDragon dragon)
    {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void tick()
    {
        double yDif = getWantedY() - mob.getY();

        if (!dragon.isFlying())
        {
            var liftOffScale = LIFT_OFF_HEIGHT * dragon.getScale();
            if (yDif > liftOffScale
                    && DMLUtil.noVerticalCollision(dragon.level(), getWantedX(), getWantedZ(), getWantedY(), getWantedY() - liftOffScale)
                    && canLiftOff(dragon))
            {
                dragon.liftOff();
            }
            else
            {
                // ground movement behavior if we don't want to fly
                super.tick();
                return;
            }
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
                mob.setYya((float) yDif * speed);

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