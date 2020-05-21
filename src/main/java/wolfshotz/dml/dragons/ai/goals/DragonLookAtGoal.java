package wolfshotz.dml.dragons.ai.goals;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.util.EntityPredicates;
import wolfshotz.dml.dragons.TameableDragonEntity;

public class DragonLookAtGoal extends LookAtGoal
{
    private final EntityPredicate predicate;

    public DragonLookAtGoal(TameableDragonEntity entityIn)
    {
        super(entityIn, LivingEntity.class, 10f);
        this.predicate = new EntityPredicate()
                .setDistance(maxDistance)
                .allowFriendlyFire()
                .allowInvulnerable()
                .setSkipAttackChecks()
                .setCustomPredicate((e) -> EntityPredicates.notRiding(entityIn).test(e));
    }

    public boolean shouldExecute()
    {
        if (entity.getRNG().nextFloat() >= chance) return false;
        else
        {
            if (entity.getAttackTarget() != null) closestEntity = entity.getAttackTarget();
            else
                closestEntity = entity.world.func_225318_b(watchedClass, predicate, entity, entity.getPosX(), entity.getPosYEye(), entity.getPosZ(), entity.getBoundingBox().grow(maxDistance, 3.0D, maxDistance));
            return closestEntity != null;
        }
    }
}
