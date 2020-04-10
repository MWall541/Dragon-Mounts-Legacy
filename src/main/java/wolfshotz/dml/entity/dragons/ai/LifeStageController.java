package wolfshotz.dml.entity.dragons.ai;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.MathHelper;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.util.MathX;

import java.util.UUID;

import static wolfshotz.dml.entity.dragons.ai.LifeStageController.EnumLifeStage.*;

public class LifeStageController
{
    private static final int TICKS_SINCE_CREATION_UPDATE_INTERVAL = 100;

    private final TameableDragonEntity dragon;
    private int ticksAlive;
    private EnumLifeStage prevStage;

    public LifeStageController(TameableDragonEntity dragon)
    {
        this.dragon = dragon;
    }

    public void tick()
    {
        if (dragon.isServer() && !isAdult() && ++ticksAlive % TICKS_SINCE_CREATION_UPDATE_INTERVAL == 0)
            dragon.setTicksAlive(ticksAlive);

        updateLifeStage();
        if (dragon.ticksExisted % TICKS_SINCE_CREATION_UPDATE_INTERVAL == 0) dragon.recalculateSize();
    }

    public void updateLifeStage()
    {
        EnumLifeStage currentStage = getLifeStage();
        if (prevStage != currentStage)
        {
            if (dragon.isServer())
            {
                applyStageAttributeModifiers();
                dragon.getNavigator().getNodeProcessor().setCanEnterDoors(isHatchling());
            }
            prevStage = currentStage;
        }
    }

    public void applyStageAttributeModifiers()
    {
        AttributeModifier scaleModifier = new AttributeModifier(UUID.fromString("856d4ba4-9ffe-4a52-8606-890bb9be538b"), "Dragon size modifier", getScale(), AttributeModifier.Operation.ADDITION);
        IAttributeInstance health = dragon.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance damage = dragon.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);

        health.removeModifier(scaleModifier);
        health.applyModifier(scaleModifier);
        damage.removeModifier(scaleModifier);
        damage.applyModifier(scaleModifier);
    }

    public EnumLifeStage getLifeStage()
    {
        return EnumLifeStage.fromTickCount(dragon.getTicksAlive());
    }

    public void setLifeStage(EnumLifeStage stage)
    {
        if (dragon.isServer())
        {
            ticksAlive = stage.startTicks();
            dragon.setTicksAlive(ticksAlive);
        }
        updateLifeStage();
    }

    public float getScale()
    {
        return EnumLifeStage.scaleFromTickCount(dragon.getTicksAlive());
    }

    public void setTicksAlive(int ticksAlive)
    {
        this.ticksAlive = ticksAlive;
    }

    public boolean isHatchling() { return getLifeStage() == HATCHLING; }

    public boolean isJuvenile() { return getLifeStage() == JUVENILE; }

    public boolean isAdult() { return getLifeStage() == ADULT; }

    public enum EnumLifeStage
    {
        HATCHLING(0.33f),
        JUVENILE(0.66f),
        ADULT(1);

        public static final int TICKS_PER_STAGE = 24000; // todo: configurable
        public static final EnumLifeStage[] VALUES = values(); // cached for speed

        private final float scale;

        EnumLifeStage(float scale)
        {
            this.scale = scale;
        }

        public static float scaleFromTickCount(int ticksSinceCreation)
        {
            EnumLifeStage lifeStage = fromTickCount(ticksSinceCreation);

            // constant size for egg and adult stage
            if (lifeStage == ADULT) return lifeStage.scale;

            // interpolated size between current and next stage
            return MathX.terpLinear(lifeStage.scale, lifeStage.next().scale,
                    progressFromTickCount(ticksSinceCreation));
        }

        public static float progressFromTickCount(int ticksSinceCreation)
        {
            EnumLifeStage lifeStage = fromTickCount(ticksSinceCreation);
            int lifeStageTicks = ticksSinceCreation - lifeStage.startTicks();
            return lifeStageTicks / (float) TICKS_PER_STAGE;
        }

        public static EnumLifeStage fromTickCount(int ticksSinceCreation)
        {
            return VALUES[clampTickCount(ticksSinceCreation) / TICKS_PER_STAGE];
        }

        public static int clampTickCount(int ticksSinceCreation)
        {
            return MathHelper.clamp(ticksSinceCreation, 0, VALUES.length * TICKS_PER_STAGE);
        }

        public int startTicks()
        {
            return ordinal() * TICKS_PER_STAGE;
        }

        public EnumLifeStage next()
        {
            return this == ADULT ? null : VALUES[ordinal() + 1];
        }
    }
}
