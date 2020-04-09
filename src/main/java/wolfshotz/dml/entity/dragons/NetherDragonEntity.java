package wolfshotz.dml.entity.dragons;

import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;

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
}
