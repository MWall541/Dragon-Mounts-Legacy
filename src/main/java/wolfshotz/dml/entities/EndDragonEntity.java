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

public class EndDragonEntity extends TameableDragonEntity
{
    private static final ImmutableList<RegistryKey<Biome>> BIOMES = ImmutableList.of(Biomes.END_BARRENS,
            Biomes.END_HIGHLANDS,
            Biomes.END_MIDLANDS,
            Biomes.END_MIDLANDS,
            Biomes.THE_END,
            Biomes.SMALL_END_ISLANDS);

    public EndDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.DRAGON_BREATH);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePose = egg.getPosition();

        for (BlockPos pos : BlockPos.getAllInBoxMutable(basePose.add(1, 1, 1), basePose.add(-1, -1, -1)))
            if (egg.world.getBlockState(pos).getBlock().isIn(Tags.Blocks.END_STONES)) ++points;
        points += egg.world.func_242406_i(egg.getPosition()).map(key -> BIOMES.contains(key)? 0 : 2).orElse(0);

        return points;
    }
}
