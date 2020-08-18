package wolfshotz.dml.entities;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.Tags;
import wolfshotz.dml.util.BetterBlockMatcher;

/**
 * TODO in 1.16: walk on lava!
 */
public class NetherDragonEntity extends TameableDragonEntity
{
    private static final ImmutableList<RegistryKey<Biome>> BIOMES = ImmutableList.of(Biomes.NETHER_WASTES,
            Biomes.CRIMSON_FOREST,
            Biomes.WARPED_FOREST,
            Biomes.BASALT_DELTAS,
            Biomes.SOUL_SAND_VALLEY);

    public NetherDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.ON_FIRE, DamageSource.IN_FIRE, DamageSource.LAVA);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePose = egg.getPosition();
        BetterBlockMatcher matcher = new BetterBlockMatcher(Tags.Blocks.NETHERRACK.getAllElements());

        for (BlockPos pos : BlockPos.getAllInBoxMutable(basePose.add(1, 1, 1), basePose.add(-1, -1, -1)))
            if (matcher.test(egg.world.getBlockState(pos))) ++points;
        points += egg.world.func_242406_i(egg.getPosition()).map(key -> BIOMES.contains(key)? 0 : 2).orElse(0);

        return points;
    }
}
