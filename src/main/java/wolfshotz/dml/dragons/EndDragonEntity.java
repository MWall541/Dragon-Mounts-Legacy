package wolfshotz.dml.dragons;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.Tags;
import wolfshotz.dml.egg.DragonEggEntity;

public class EndDragonEntity extends TameableDragonEntity
{
    public EndDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        addImmunities(DamageSource.DRAGON_BREATH);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;
        BlockPos basePose = egg.getPosition();
        points += (int) BlockPos.getAllInBox(basePose.add(1, 1, 1), basePose.add(-1, -1, -1))
                .filter(pos -> egg.world.getBlockState(pos).getBlock().isIn(Tags.Blocks.END_STONES))
                .count();
        if (BiomeDictionary.hasType(egg.world.getBiome(basePose), BiomeDictionary.Type.END)) points += 2;
        return points;
    }

    public static Object2FloatMap<ResourceLocation> getEggLoot()
    {
        return Object2FloatMaps.singleton(LootTables.CHESTS_END_CITY_TREASURE, 0.7f);
    }
}
