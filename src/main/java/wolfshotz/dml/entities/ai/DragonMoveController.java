package wolfshotz.dml.entities.ai;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.math.Vec3d;
import wolfshotz.dml.entities.TameableDragonEntity;

public class DragonMoveController extends MovementController
{
    private final float YAW_SPEED = 5;
    private final TameableDragonEntity dragon;

    public DragonMoveController(TameableDragonEntity dragon)
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

        Vec3d dragonPos = dragon.getPositionVector();
        Vec3d movePos = new Vec3d(posX, posY, posZ);

        // get direction vector by subtracting the current position from the
        // target position and normalizing the result
        Vec3d dir = movePos.subtract(dragonPos).normalize();

        // get euclidean distance to target
        double dist = dragonPos.distanceTo(movePos);

        // move towards target if it's far enough away
        if (dist > 1.5)
        {
            double flySpeed = dragon.getAttribute(SharedMonsterAttributes.FLYING_SPEED).getValue();

            // update velocity to approach target
            dragon.setMotion(dir.scale(flySpeed));
        }
        else
        {
            // just slow down and hover at current location
            dragon.setMotion(dragon.getMotion().scale(0.8d));
            dragon.setMotion(dragon.getMotion().add(0, Math.sin((dragon.ticksExisted / 5)) * 0.03, 0));
        }

        // face entity towards target
        if (dist > 2.5E-7)
        {
            float newYaw = (float) Math.toDegrees(Math.PI * 2 - Math.atan2(dir.x, dir.z));
            dragon.rotationYaw = limitAngle(dragon.rotationYaw, newYaw, YAW_SPEED);
            dragon.setAIMoveSpeed((float) (speed * dragon.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
        }

        // apply movement
        dragon.move(MoverType.SELF, dragon.getMotion());
    }
}
