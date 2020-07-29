package wolfshotz.dml.entities.ai;

import net.minecraft.entity.ai.controller.BodyController;
import wolfshotz.dml.entities.TameableDragonEntity;
import wolfshotz.dml.util.MathX;

public class DragonBodyController extends BodyController
{
    private final TameableDragonEntity dragon;
    private int turnTicks;
    private final int turnTicksLimit = 20;
    private float prevRotationYawHead;

    public DragonBodyController(TameableDragonEntity dragon)
    {
        super(dragon);
        this.dragon = dragon;
    }

    @Override
    public void updateRenderAngles()
    {
        double deltaX = dragon.getPosX() - dragon.prevPosX;
        double deltaY = dragon.getPosZ() - dragon.prevPosZ;
        double dist = deltaX * deltaX + deltaY * deltaY;

        float yawSpeed = 90;

        // rotate instantly if flying, sitting or moving
        if (dragon.isFlying() || dragon.isSitting() || dist > 0.0001)
        {
            dragon.renderYawOffset = dragon.rotationYaw;
            dragon.rotationYawHead = MathX.updateRotation(dragon.renderYawOffset, dragon.rotationYawHead, yawSpeed);
            prevRotationYawHead = dragon.rotationYawHead;
            turnTicks = 0;
            return;
        }

        double yawDiff = Math.abs(dragon.rotationYawHead - prevRotationYawHead);
        if (yawDiff > 15)
        {
            turnTicks = 0;
            prevRotationYawHead = dragon.rotationYawHead;
        }
        else
        {
            turnTicks++;

            if (turnTicks > turnTicksLimit)
            {
                yawSpeed = Math.max(1 - (float) (turnTicks - turnTicksLimit) / turnTicksLimit, 0) * 75;
            }
        }

        dragon.renderYawOffset = MathX.updateRotation(dragon.rotationYawHead, dragon.renderYawOffset, yawSpeed);
    }
}
