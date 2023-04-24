package com.github.kay9.dragonmounts.entity.dragon.ai;

import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class DragonMoveController extends MoveControl
{
    private final TameableDragon dragon;

    public DragonMoveController(TameableDragon dragon)
    {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void tick()
    {
        // original movement behavior if the entity isn't flying
        if (!dragon.isFlying())
        {
            super.tick();
            return;
        }

        if (operation == MoveControl.Operation.MOVE_TO)
        {
            operation = MoveControl.Operation.WAIT;
            double xDif = wantedX - mob.getX();
            double yDif = wantedY - mob.getY();
            double zDif = wantedZ - mob.getZ();
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
                mob.setYya(yDif > 0d? speed : -speed);

            float yaw = (float) (Mth.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
            mob.setYRot(rotlerp(mob.getYRot(), yaw, 6));
        }
        else
        {
            mob.setYya(0);
            mob.setZza(0);
        }
    }
}