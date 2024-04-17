package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
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

    // todo: dragon body rotates too quickly, rotation speed hardcoded in super
    @Override
    public void tick()
    {

        if (!dragon.isFlying())
        {
            super.tick();
            return;
        }

        if (operation == MoveControl.Operation.MOVE_TO)
        {
            operation = MoveControl.Operation.WAIT;
            float xDif = (float) (getWantedX() - mob.getX());
            float yDif = (float) (getWantedY() - mob.getY());
            float zDif = (float) (getWantedZ() - mob.getZ());

            float xzDistance = Mth.sqrt(xDif * xDif + zDif * zDif);
            float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.FLYING_SPEED));

            // only travel forward if reasonably far enough. Helps reduce weird strafing and circling in midair.
            if (xzDistance > mob.getBbWidth() * 0.5)
            {
                mob.setSpeed(Math.min(xzDistance, 1f) * speed);

                float yaw = (float) (Mth.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
                mob.setYRot(rotlerp(mob.getYRot(), yaw, 6));
            }
            else
                mob.setSpeed(0);

            // only travel up/down if necessary. Helps reduce awkward up/down bobbing.
            if (Math.abs(yDif) > 0.0001)
            {
                // this is required because for some fucking reason setSpeed also sets zza, and yya needs a speed to move with.
                float forwardOld = mob.zza;
                mob.setSpeed(speed);
                mob.zza = forwardOld;

                // god this is so unintuitive
                mob.setYya(speed * Mth.clamp(yDif, -1f, 1f));
            }
        }
        else
        {
            mob.setYya(0);
            mob.setZza(0);
        }
    }
}