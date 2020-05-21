package wolfshotz.dml.egg;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import org.apache.commons.lang3.tuple.Pair;
import wolfshotz.dml.dragons.TameableDragonEntity;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class DragonEggType
{
    public static final List<DragonEggType> INSTANCES = Lists.newArrayList();
    public final ToIntFunction<DragonEggEntity> habitatFunc;
    public final int primColor, secColor;
    private final Supplier<EntityType<TameableDragonEntity>> breed;
    private final Supplier<Block> eggBlock;

    public DragonEggType(Supplier<EntityType<TameableDragonEntity>> breed, Supplier<Block> eggBlock, ToIntFunction<DragonEggEntity> habitatFunc, int primColor, int secColor)
    {
        this.breed = breed;
        this.eggBlock = eggBlock;
        this.habitatFunc = habitatFunc;
        this.primColor = primColor;
        this.secColor = secColor;

        INSTANCES.add(this);
    }

    public EntityType<? extends TameableDragonEntity> getBreed() { return breed.get(); }

    public Block getEggBlock() { return eggBlock.get(); }

    public int getHabitatPoints(DragonEggEntity egg) { return habitatFunc.applyAsInt(egg); }

    public Pair<Integer, Integer> getColors() { return Pair.of(primColor, secColor); }

    public float getColorR(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 16) & 0xFF) / 255f;
    }

    public float getColorG(boolean primary)
    {
        int color = primary? primColor : secColor;
        return ((color >> 8) & 0xFF) / 255f;
    }

    public float getColorB(boolean primary)
    {
        int color = primary? primColor : secColor;
        return (color & 0xFF) / 255f;
    }

    public static DragonEggType lookUp(EntityType<?> type)
    {
        return INSTANCES.stream().filter(t -> t.breed.get() == type).findFirst().get();
    }
}
