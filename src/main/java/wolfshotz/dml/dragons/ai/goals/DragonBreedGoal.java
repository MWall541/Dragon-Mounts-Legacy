package wolfshotz.dml.dragons.ai.goals;

import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import wolfshotz.dml.dragons.TameableDragonEntity;

import java.util.List;

public class DragonBreedGoal extends BreedGoal
{
    private final TameableDragonEntity dragon;

    public DragonBreedGoal(TameableDragonEntity animal)
    {
        super(animal, 1);
        this.dragon = animal;
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
    public boolean shouldExecute()
    {
        if (!dragon.isAdult()) return false;
        if (!dragon.isInLove()) return false;
        else return (targetMate = getNearbyMate()) != null;
    }

    public TameableDragonEntity getNearbyMate()
    {
        List<TameableDragonEntity> list = world.getEntitiesWithinAABB(TameableDragonEntity.class, dragon.getBoundingBox().grow(8d));
        double dist = Double.MAX_VALUE;
        TameableDragonEntity closest = null;

        for (TameableDragonEntity entity : list)
        {
            if (dragon.canMateWith(entity) && dragon.getDistanceSq(entity) < dist)
            {
                closest = entity;
                dist = dragon.getDistanceSq(entity);
            }
        }

        return closest;
    }

    @Override
    protected void spawnBaby()
    {
        // Respect Mod compatibility
        if (MinecraftForge.EVENT_BUS.post(new BabyEntitySpawnEvent(animal, targetMate, null)))
        {
            //Reset the "inLove" state for the animals
            animal.setGrowingAge(6000);
            targetMate.setGrowingAge(6000);
            return;
        }

        animal.resetInLove();
        targetMate.resetInLove();
        dragon.createChild(targetMate);
        world.setEntityState(this.animal, (byte) 18);
        if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
            world.addEntity(new ExperienceOrbEntity(world, animal.getPosX(), animal.getPosY(), animal.getPosZ(), animal.getRNG().nextInt(7) + 1));
    }
}
