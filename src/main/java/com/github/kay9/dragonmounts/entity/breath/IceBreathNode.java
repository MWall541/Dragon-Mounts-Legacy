package com.github.kay9.dragonmounts.entity.breath;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class IceBreathNode extends BreathNode
{
    private static final BreathEnvironmentEffects EFFECTS = new BreathEnvironmentEffects();

    private static final int DEFAULT_MAX_AGE = 50;
    private static final float DEFAULT_MAX_SPEED = 0.5f;
    private static final float DEFAULT_MAX_SIZE = 4f;
    private static final int TICKS_PER_FREEZE = 2;
    private static final int MAX_ADDED_FREEZE_TICKS = 140; // 7 seconds

    public IceBreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    protected IceBreathNode(Entity shooter, Vec3 startPos, Vec3 direction)
    {
        super(DMLRegistry.ICE_BREATH.get(), shooter, startPos, direction);
    }

    public static IceBreathNode shoot(TameableDragon shooter)
    {
        var direction = getShootDirection(shooter.getRandom(), shooter.getXRot(), shooter.getYHeadRot(), 5);
        return new IceBreathNode(shooter, shooter.getMouthPos(), direction);
    }

    @Override
    public int variedMaxAge()
    {
        return DEFAULT_MAX_AGE + (random.nextInt(10) - 5);
    }

    @Override
    public float variedSpeed()
    {
        return DEFAULT_MAX_SPEED + (0.5f * random.nextFloat() - 0.25f);
    }

    @Override
    public float variedMaxSize()
    {
        return DEFAULT_MAX_SIZE + (0.5f * random.nextFloat() - 0.25f);
    }

    @Override
    protected float getCurrentSize()
    {
        return calculateLinearExpansion();
    }

    @Override
    public void contactMethod()
    {
        touch();
    }

    @Override
    public void tick()
    {
        if (isOnFire())
        {
            melt();
            return;
        }

        super.tick();

        ageBySpeed();

        if (getLevel().isClientSide())
        {
            var motion = getDeltaMovement();
            var x = getRandomX(0.2) + motion.x();
            var y = getRandomY() + motion.y();
            var z = getRandomZ(0.2) + motion.z();
            getLevel().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, getMoveDirection().x * 0.5/*(2 * random.nextDouble() - 1) * 0.1*/, getMoveDirection().y * 0.05, getMoveDirection().z * 0.5/*(2 * random.nextDouble() - 1) * 0.1*/);
        }
    }

    /**
     * Blizzard-like breath weapon.
     * Freezes entities using vanillas freezing mechanics.
     * Damage is calculated rather uniquely on the ice breath:
     * Damage is heavily influenced on the fraction of how frozen the entity currently is,
     * as well as what the entity is wearing. Leather armor incorporates full insulation,
     * and will half the incoming damage. However, no leather armor results in 1.5x the
     * damage.
     * It should be noted that due to the breath being colder than powder snow, leather insulants CANNOT prevent
     * freezing!
     * Should the entity be immune to frozen damage, we will just inflict physical damage at the quarter amount.
     * Being a blizzard, wind speeds are high, so entities are also slightly pushed in the direction of the node.
     */
    @Override
    protected void onHitEntity(EntityHitResult result)
    {
        super.onHitEntity(result);

        var entity = result.getEntity();
        entity.setDeltaMovement(entity.getDeltaMovement().add(getDeltaMovement().scale(0.01))); // blizzard wind pushes entities.

        if (getLevel().isClientSide()) return; // server calculations from this point on
        if (!(entity instanceof LivingEntity living)) return;

        var damage = getBaseDamage();

        if (entity.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES) || entity.getType() == EntityType.SKELETON) // need to hardcode this for... whatever reason. Skeletons aren't in the tag.
            damage *= getIntensityScale() * 0.25f; // physical damage only.
        else // freeze, deal freezing damage, etc.
        {
            var coldIntensity = getIntensityScale() * getInsulationFor(living);
            var frozenTicks = entity.getTicksFrozen() + Mth.ceil(TICKS_PER_FREEZE * coldIntensity);
            var max = entity.getTicksRequiredToFreeze() + MAX_ADDED_FREEZE_TICKS; // additional max ticks allows for longer fully frozen time.
            var dmgFraction = entity.getPercentFrozen();

            entity.setTicksFrozen(Math.min(max, frozenTicks));

            if (dmgFraction < 0.2f) damage = 0; // only damage after a bit of freezing
            else
            {
                damage *= coldIntensity * dmgFraction;
                if (entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) damage *= 2f;
            }
        }

        entity.clearFire();
        entity.setSharedFlagOnFire(false);

        if (damage != 0) hurtEntity(entity, getDamageSource(), damage);
    }

    /**
     * Blizzard-like breath weapon
     * Freezes blocks and the like, around it. (snow on ground, etc.)
     * already present snow layers are grown
     * hard blocks, such as stone, shatter after exposed for a long time.
     * leaves, tall grass, flowers, plants in general also shatter, at a faster rate.
     * Water is frozen to ice - longer exposure freezes deeper.
     * Lava is turned to obsidian
     * Water under ice is frozen as well.
     */
    @Override
    protected void onHitBlock(BlockHitResult result)
    {
        super.onHitBlock(result);

        var dir = result.getDirection();
        var entityDir = Direction.getNearest(getMoveDirection().x(), getMoveDirection().y(), getMoveDirection().z());
        var directContact = entityDir.getOpposite() == dir;
        if (directContact) age += 1;

        if (getLevel().isClientSide()) return;
        if (!(getOwner() instanceof TameableDragon dragon) || !DMLConfig.canGrief(dragon)) return;

        var pos = result.getBlockPos();
        var relative = pos.relative(dir);
        var state = getLevel().getBlockState(pos);
        var relativeState = getLevel().getBlockState(relative);
        var chance = random.nextDouble();
        if (directContact) chance *= 0.5;

        // this has to be done manually since it uses the relativeState instead of the affected pos.
        if (chance < 0.01 && relativeState.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, relative))
            getLevel().setBlockAndUpdate(relative, Blocks.SNOW.defaultBlockState());

        EFFECTS.affectEnvironment(getLevel(), pos, state, chance);
    }

    public void melt()
    {
        if (getLevel().isClientSide())
        {
            if (random.nextDouble() <= 0.25) playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        }
        else
            getLevel().setBlockAndUpdate(blockPosition().above((int) eyeHeight), Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 3));

        discard();
    }

    @Override
    public void expire()
    {
        if (getLevel().isClientSide())
            for (int i = 0; i < 3; i++)
                getLevel().addParticle(ParticleTypes.RAIN, getRandomX(0.5), getRandomY(), getRandomZ(0.5), 0, -0.1f, 0);
        super.expire();
    }

    public DamageSource getDamageSource()
    {
        var shooter = getOwner();
        var owned = shooter != null;
        var name = owned? "iceBreath" : "freeze";
        if (!owned) shooter = this;
        return new IndirectEntityDamageSource(name, this, shooter).setProjectile();
    }

    /**
     * A result of 0.25 means the living is fully insulated, all effects are reduced to a quarter.
     * A result of 1.5 means the living is wearing full conductive armor, all effects are 1.5x
     * No armor is unaffected.
     *
     * @return the scale of the cold/damage effects of living's armor.
     */
    private static float getInsulationFor(LivingEntity living)
    {
        var intensity = 1f;
        for (var item : living.getArmorSlots())
            if (item.is(ItemTags.FREEZE_IMMUNE_WEARABLES))
                intensity -= 0.1875;
            else if (item.getItem() instanceof ArmorItem)
                intensity += 0.125f;
        return intensity;
    }

    static
    {
        EFFECTS.register(Blocks.WATER, frostedIceBehavior());
        EFFECTS.registerBasicReplacer(Blocks.FROSTED_ICE, Blocks.ICE, 0.05);
        EFFECTS.registerBasicReplacer(Blocks.ICE, Blocks.PACKED_ICE, 0.005);
        EFFECTS.registerBasicReplacer(Blocks.PACKED_ICE, Blocks.BLUE_ICE, 0.0001);
        EFFECTS.register(Blocks.SNOW, snowLayerBehavior());
        EFFECTS.registerBasicReplacer(Blocks.LAVA, Blocks.OBSIDIAN, 0.005); // Direct contact melts into water and makes obsidian anyway, so let's cover close-by contact.
    }

    private static BreathEnvironmentEffects.BreathBehavior snowLayerBehavior()
    {
        return (level1, affectedPos, affectedState, chance) ->
        {
            if (chance < 0.01)
            {
                int layers = affectedState.getValue(SnowLayerBlock.LAYERS);
                if (layers < 8)
                    level1.setBlockAndUpdate(affectedPos, affectedState.setValue(SnowLayerBlock.LAYERS, layers + 1));
            }
        };
    }

    private static BreathEnvironmentEffects.BreathBehavior frostedIceBehavior()
    {
        return (level1, affectedPos, affectedState, chance) ->
        {
            if (chance < 0.05)
            {
                level1.setBlockAndUpdate(affectedPos, Blocks.FROSTED_ICE.defaultBlockState());
                level1.scheduleTick(affectedPos, Blocks.FROSTED_ICE, Mth.nextInt(level1.getRandom(), 60, 120));
            }
        };
    }
}
