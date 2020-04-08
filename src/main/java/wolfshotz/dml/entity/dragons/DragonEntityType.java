package wolfshotz.dml.entity.dragons;

import com.google.common.collect.Maps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import org.apache.commons.lang3.tuple.Pair;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;

import java.util.Map;
import java.util.function.Predicate;

public class DragonEntityType extends EntityType<TameableDragonEntity>
{
    private static final Map<Predicate<DragonEggEntity>, EnumEggTypes> HABITATS = Maps.newHashMap();
    private final EnumEggTypes breedType;
    private boolean thinLegs, tailScales, tailHorns;
    private int color1, color2;

    public DragonEntityType(IFactory<TameableDragonEntity> factoryIn, EnumEggTypes type)
    {
        super(factoryIn, EntityClassification.CREATURE, true, true, type == EnumEggTypes.FIRE || type == EnumEggTypes.NETHER, true, EntitySize.flexible(TameableDragonEntity.BASE_WIDTH, TameableDragonEntity.BASE_HEIGHT), e -> true, e -> 80, e -> 3, null);
        this.breedType = type;
    }

    /**
     * Used for the egg to check surrounding environment
     */
    public static DragonEntityType getByHabitat(DragonEggEntity egg)
    {
        for (Predicate<DragonEggEntity> test : HABITATS.keySet())
        {
            if (test.test(egg)) return HABITATS.get(test).getType();
        }
        return null;
    }

    /**
     * Used for the egg (Blockstates take enums)
     */
    public EnumEggTypes getBreedType() { return breedType; }

    /**
     * Used in the model to determine whether this dragon has thin legs or not
     */
    public boolean thinLegs() { return thinLegs;}

    /**
     * Used in the model to determine whether this dragon has additional tail scales
     */
    public boolean tailScales() { return tailScales; }

    /**
     * Used in the model to determine whether this dragon has a horned tail
     *
     * @return
     */
    public boolean tailHorns() { return tailHorns; }

    /**
     * Dragon colors
     */
    public Pair<Integer, Integer> getColors()
    {
        return Pair.of(color1, color2);
    }

    public DragonEntityType setHabitat(Predicate<DragonEggEntity> checkHabitat)
    {
        HABITATS.put(checkHabitat, breedType);
        return this;
    }

    public DragonEntityType setThinLegs()
    {
        this.thinLegs = true;
        return this;
    }

    public DragonEntityType setTailScales()
    {
        this.tailScales = true;
        return this;
    }

    public DragonEntityType setTailHorns()
    {
        this.tailHorns = true;
        return this;
    }

    public DragonEntityType setColors(int color1, int color2)
    {
        this.color1 = color1;
        this.color2 = color2;
        return this;
    }

    public float getRColor(boolean primary)
    {
        int color = primary ? color1 : color2;
        return ((color >> 16) & 0xFF) / 255f;
    }

    public float getGColor(boolean primary)
    {
        int color = primary ? color1 : color2;
        return ((color >> 8) & 0xFF) / 255f;
    }

    public float getBColor(boolean primary)
    {
        int color = primary ? color1 : color2;
        return (color & 0xFF) / 255f;
    }

}
