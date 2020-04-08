package wolfshotz.dml.util;

import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;

import java.util.Random;

/**
 * @deprecated - might remove - not in use.
 */
public class ParticleHelper
{
    public static void spawnBodyParticle(TameableDragonEntity dragon, IParticleData type)
    {
        double ox = 0, oy = 0, oz = 0;
        float s = dragon.getScale() * 1.2f;
        Random rand = dragon.getRNG();

        if (type == ParticleTypes.EXPLOSION)
        {
            ox = rand.nextGaussian() * s;
            oy = rand.nextGaussian() * s;
            oz = rand.nextGaussian() * s;
        }
        else if (type == ParticleTypes.CLOUD)
        {
            ox = (rand.nextDouble() - 0.5) * 0.1;
            oy = rand.nextDouble() * 0.2;
            oz = (rand.nextDouble() - 0.5) * 0.1;
        }
        else if (type instanceof RedstoneParticleData)
        {
            ox = 0.8;
            oz = 0.8;
        }

        // use generic random box spawning
        double x = dragon.getPosX() + (rand.nextDouble() - 0.5) * dragon.getWidth() * s;
        double y = dragon.getPosY() + (rand.nextDouble() - 0.5) * dragon.getHeight() * s;
        double z = dragon.getPosZ() + (rand.nextDouble() - 0.5) * dragon.getWidth() * s;

        dragon.world.addParticle(type, x, y, z, ox, oy, oz);
    }

    public static void spawnBodyParticles(TameableDragonEntity dragon, IParticleData type, int baseAmount)
    {
        int amount = (int) (baseAmount * dragon.getScale());
        for (int i = 0; i < amount; i++) spawnBodyParticle(dragon, type);
    }

    public static void spawnBodyParticles(TameableDragonEntity dragon, IParticleData type)
    {
        spawnBodyParticles(dragon, type, 32);
    }
}
