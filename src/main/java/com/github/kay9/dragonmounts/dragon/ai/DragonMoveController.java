package com.github.kay9.dragonmounts.dragon.ai;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

//todo ditch this in favor of common-sided movement. Smoother on client
public class DragonMoveController extends MoveControl
{
    private final float YAW_SPEED = 5;
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

        Vec3 dragonPos = dragon.position();
        Vec3 movePos = new Vec3(wantedX, wantedY, wantedZ);

        // get direction vector by subtracting the current position from the
        // target position and normalizing the result
        Vec3 dir = movePos.subtract(dragonPos).normalize();

        // get euclidean distance to target
        double dist = dragonPos.distanceTo(movePos);

        // move towards target if it's far enough away
        if (dist > 1.5)
        {
            // update velocity to approach target
            dragon.setDeltaMovement(dir);
        }

        // face entity towards target
        if (dist > 2.5E-7)
        {
            float newYaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(dir.x, dir.z));
            dragon.setYRot(rotlerp(dragon.getYRot(), newYaw, 6));
            dragon.setSpeed((float) (speedModifier * dragon.getAttribute(Attributes.MOVEMENT_SPEED).getValue()));
        }

        // apply movement
        dragon.move(MoverType.SELF, dragon.getDeltaMovement());
    }
}