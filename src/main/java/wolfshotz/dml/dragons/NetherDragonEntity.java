package wolfshotz.dml.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.Tags;
import wolfshotz.dml.egg.DragonEggEntity;
import wolfshotz.dml.util.BetterBlockMatcher;

/**
 * TODO in 1.16: walk on lava!
 */
public class NetherDragonEntity extends TameableDragonEntity
{
    public NetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.ON_FIRE, DamageSource.IN_FIRE, DamageSource.LAVA);
    }

    public static boolean isHabitat(DragonEggEntity egg)
    {
        return BiomeDictionary.hasType(egg.world.getBiome(egg.getPosition()), BiomeDictionary.Type.NETHER);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePose = egg.getPosition();
        BetterBlockMatcher matcher = new BetterBlockMatcher(Tags.Blocks.NETHERRACK.getAllElements());
        points += (int) BlockPos.getAllInBox(basePose.add(1, 1, 1), basePose.add(-1, -1, -1))
                .filter(pos -> matcher.test(egg.world.getBlockState(pos).getBlock()))
                .count();
        if (BiomeDictionary.hasType(egg.world.getBiome(basePose), BiomeDictionary.Type.NETHER)) points += 2;
        return points;
    }
}
