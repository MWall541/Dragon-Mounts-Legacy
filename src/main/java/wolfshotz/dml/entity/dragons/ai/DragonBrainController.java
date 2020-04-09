package wolfshotz.dml.entity.dragons.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.AnimalEntity;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.entity.dragons.ai.goals.DragonLandGoal;

public class DragonBrainController
{
    private final TameableDragonEntity dragon;

    public DragonBrainController(TameableDragonEntity dragon)
    {
        this.dragon = dragon;
    }

    public void clearGoals()
    {
        dragon.goalSelector.goals.clear();
        dragon.targetSelector.goals.clear();
    }

    public void updateGoals()
    {
        dragon.getNavigator().getNodeProcessor().setCanEnterDoors(dragon.isHatchling());
        dragon.getNavigator().clearPath();

        clearGoals();

        dragon.goalSelector.addGoal(0, new SwimGoal(dragon));
        dragon.goalSelector.addGoal(5, new FollowOwnerGoal(dragon, 1, 14f, 4f, false));

        if (dragon.isFlying())
        {
            dragon.goalSelector.addGoal(1, new DragonLandGoal(dragon));
        }
        else
        {
            dragon.goalSelector.addGoal(2, dragon.getAISit());
            dragon.goalSelector.addGoal(3, new MeleeAttackGoal(dragon, 1, true));
            dragon.goalSelector.addGoal(5, new BreedGoal(dragon, 1));
            dragon.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(dragon, 1));
            dragon.goalSelector.addGoal(7, new LookAtGoal(dragon, LivingEntity.class, 10f));
            dragon.goalSelector.addGoal(8, new LookRandomlyGoal(dragon));
        }

        if (!dragon.isHatchling())
        {
            dragon.targetSelector.addGoal(0, new OwnerHurtByTargetGoal(dragon));
            dragon.targetSelector.addGoal(1, new OwnerHurtTargetGoal(dragon));
            dragon.targetSelector.addGoal(2, new HurtByTargetGoal(dragon));
            dragon.targetSelector.addGoal(3, new NonTamedTargetGoal<>(dragon, AnimalEntity.class, false, e -> !(e instanceof TameableDragonEntity) && !(e instanceof CreeperEntity)));
        }
    }
}
