package wolfshotz.dml.entity.dragons.ai.goals;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

public class DragonBreedGoal extends BreedGoal
{
    public DragonBreedGoal(TameableDragonEntity animal)
    {
        super(animal, 1);
    }

    public static String fixChildName(String nameOld)
    {
        if (nameOld == null || nameOld.isEmpty()) return nameOld;

        // create all lower-case char array
        char[] chars = nameOld.toLowerCase().toCharArray();

        // convert first char to upper-case
        chars[0] = Character.toUpperCase(chars[0]);

        return new String(chars);
    }

    @Override
    protected void spawnBaby()
    {
        AgeableEntity ageableentity = animal.createChild(targetMate);

        // Respect Mod compatibility
        final BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(animal, targetMate, ageableentity);
        final boolean cancelled = MinecraftForge.EVENT_BUS.post(event);
        ageableentity = event.getChild();
        if (cancelled)
        {
            //Reset the "inLove" state for the animals
            animal.setGrowingAge(6000);
            targetMate.setGrowingAge(6000);
            animal.resetInLove();
            targetMate.resetInLove();
            return;
        }


    }
}
