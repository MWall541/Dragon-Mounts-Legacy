package wolfshotz.dml.entity.dragons;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.BodyController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SaddleItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import wolfshotz.dml.DMLSounds;
import wolfshotz.dml.client.anim.DragonAnimator;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragonegg.DragonEggEntity;
import wolfshotz.dml.entity.dragonegg.EnumEggTypes;
import wolfshotz.dml.entity.dragons.ai.DragonBodyController;
import wolfshotz.dml.entity.dragons.ai.DragonMoveController;
import wolfshotz.dml.entity.dragons.ai.LifeStageController;
import wolfshotz.dml.entity.dragons.ai.goals.*;
import wolfshotz.dml.util.MathX;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.minecraft.entity.SharedMonsterAttributes.*;

/**
 * Here be dragons.
 * <p>
 * Recreated: 10:50PM, 4/3/2020
 * Let the legacy live on
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @author WolfShotz
 */
public class TameableDragonEntity extends TameableEntity
{
    // base attributes
    public static final double BASE_SPEED_GROUND = 0.3;
    public static final double BASE_SPEED_FLYING = 0.6;
    public static final double BASE_DAMAGE = 8;
    public static final double BASE_HEALTH = 60;
    public static final float BASE_WIDTH = 2.75f; // adult sizes
    public static final float BASE_HEIGHT = 2.75f;
    public static final double BASE_FOLLOW_RANGE = 16;
    public static final double BASE_FOLLOW_RANGE_FLYING = BASE_FOLLOW_RANGE * 2;
    public static final double ALTITUDE_FLYING_THRESHOLD = 2;
    public static final int REPRO_LIMIT = 2;
    // data value IDs
    private static final DataParameter<Boolean> DATA_FLYING = EntityDataManager.createKey(TameableDragonEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DATA_SADDLED = EntityDataManager.createKey(TameableDragonEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DATA_TICKS_ALIVE = EntityDataManager.createKey(TameableDragonEntity.class, DataSerializers.VARINT);

    // data NBT IDs
    private static final String NBT_SADDLED = "Saddle";
    private static final String NBT_TICKS_ALIVE = "TicksAlive";
    private static final String NBT_REPRO_COUNT = "ReproCount";


    // server/client delegates
    public LifeStageController lifeStageController;
    public final List<DamageSource> damageImmunities = Lists.newArrayList();

    public int reproCount;
    public DragonAnimator animator;

    public TameableDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);

        // enables walking over blocks
        stepHeight = 1;
        ignoreFrustumCheck = true;
        moveController = new DragonMoveController(this);
        if (isClient()) this.animator = new DragonAnimator(this);
    }

    @Override
    protected BodyController createBodyController()
    {
        return new DragonBodyController(this);
    }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(MOVEMENT_SPEED).setBaseValue(BASE_SPEED_GROUND);
        getAttribute(MAX_HEALTH).setBaseValue(BASE_HEALTH);
        getAttribute(FOLLOW_RANGE).setBaseValue(BASE_FOLLOW_RANGE);
        getAttribute(KNOCKBACK_RESISTANCE).setBaseValue(10);
        getAttributes().registerAttribute(ATTACK_DAMAGE).setBaseValue(BASE_DAMAGE);
        getAttributes().registerAttribute(FLYING_SPEED).setBaseValue(BASE_SPEED_FLYING);
    }

    @Override
    protected void registerGoals() // TODO: Much Smarter AI and features
    {
        goalSelector.addGoal(1, new DragonLandGoal(this));
        goalSelector.addGoal(2, sitGoal = new SitGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(4, new DragonBabuFollowParent(this, 10));
        goalSelector.addGoal(5, new DragonFollowOwnerGoal(this, 10f, 20f, 250f));
        goalSelector.addGoal(5, new DragonBreedGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1));
        goalSelector.addGoal(7, new DragonLookAtGoal(this));
        goalSelector.addGoal(8, new LookRandomlyGoal(this));

        if (getType() != DMLEntities.WATER_DRAGON.get())
        {
            goalSelector.addGoal(0, new SwimGoal(this)
            {
                @Override
                public boolean shouldExecute() { return getSubmergedHeight() > getScale() || isInLava(); }
            });
        }

        targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this)
        {
            @Override
            public boolean shouldExecute() { return !isHatchling() && super.shouldExecute(); }
        });
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this)
        {
            @Override
            public boolean shouldExecute() { return !isHatchling() && super.shouldExecute(); }
        });
        targetSelector.addGoal(2, new HurtByTargetGoal(this)
        {
            @Override
            public boolean shouldExecute() { return !isHatchling() && super.shouldExecute(); }
        });
        targetSelector.addGoal(3, new NonTamedTargetGoal<AnimalEntity>(this, AnimalEntity.class, false, e -> !(e instanceof TameableDragonEntity))
        {
            @Override
            public boolean shouldExecute() { return !isHatchling() && super.shouldExecute(); }
        });
    }

    @Override
    protected void registerData()
    {
        super.registerData();

        dataManager.register(DATA_FLYING, false);
        dataManager.register(DATA_SADDLED, false);
        dataManager.register(DATA_TICKS_ALIVE, LifeStageController.EnumLifeStage.ADULT.startTicks()); // default to adult stage
    }

    @Override
    public void writeAdditional(CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putBoolean(NBT_SADDLED, isSaddled());
        compound.putInt(NBT_TICKS_ALIVE, getTicksAlive());
        compound.putInt(NBT_REPRO_COUNT, reproCount);
    }

    @Override
    public void readAdditional(CompoundNBT compound)
    {
        super.readAdditional(compound);
        setSaddled(compound.getBoolean(NBT_SADDLED));
        if (compound.contains(NBT_TICKS_ALIVE)) setTicksAlive(compound.getInt(NBT_TICKS_ALIVE));
        this.reproCount = compound.getInt(NBT_REPRO_COUNT);
    }

    /**
     * Returns true if the dragon is saddled.
     */
    public boolean isSaddled() { return dataManager.get(DATA_SADDLED); }

    /**
     * Set or remove the saddle of the dragon.
     */
    public void setSaddled(boolean saddled) { dataManager.set(DATA_SADDLED, saddled); }

    public int getTicksAlive() { return dataManager.get(DATA_TICKS_ALIVE); }

    public void setTicksAlive(int ticksAlive)
    {
        dataManager.set(DATA_TICKS_ALIVE, ticksAlive);
        getLifeStageController().setTicksAlive(ticksAlive);
    }

    public void addReproCount() { reproCount++; }

    public boolean canFly()
    {
        // hatchling's can't fly
        return !isHatchling();
    }

    public boolean shouldFly() { return canFly() && !isInWater() && getAltitude() > ALTITUDE_FLYING_THRESHOLD; }

    /**
     * Returns true if the entity is flying.
     */
    public boolean isFlying()
    {
        return dataManager.get(DATA_FLYING);
    }

    /**
     * Set the flying flag of the entity.
     */
    public void setFlying(boolean flying) { dataManager.set(DATA_FLYING, flying); }

    public LifeStageController getLifeStageController()
    {
        if (lifeStageController == null) lifeStageController = new LifeStageController(this);
        return lifeStageController;
    }

    @Override
    public void livingTick()
    {
        getLifeStageController().tick();

        if (isServer())
        {
            // update flying state based on the distance to the ground
            boolean flying = shouldFly();
            if (flying != isFlying())
            {
                // notify client
                setFlying(flying);

                // update AI follow range (needs to be updated before creating
                // new PathNavigate!)
                getAttribute(FOLLOW_RANGE).setBaseValue(flying? BASE_FOLLOW_RANGE_FLYING : BASE_FOLLOW_RANGE);

                // update pathfinding method
                if (flying) navigator = new FlyingPathNavigator(this, world);
                else navigator = new GroundPathNavigator(this, world);
            }
        }
        else animator.tick();

        super.livingTick();
    }

    @Override
    public void travel(Vec3d vec3d)
    {
        if (!isFlying()) super.travel(vec3d);

        if (world.isRemote) return;
        PlayerEntity rider = getRidingPlayer();
        if (rider == null) return;

        double x = getPosX();
        double y = getPosY();
        double z = getPosZ();

        // control direction with movement keys
        if (rider.moveStrafing != 0 || rider.moveForward != 0)
        {
            Vec3d wp = rider.getLookVec();

            if (rider.moveForward < 0)
            {
                wp = wp.rotateYaw(MathX.PI_F);
            }
            else if (rider.moveStrafing > 0)
            {
                wp = wp.rotateYaw(MathX.PI_F * 0.5f);
            }
            else if (rider.moveStrafing < 0)
            {
                wp = wp.rotateYaw(MathX.PI_F * -0.5f);
            }

            x += wp.x * 10;
            y += wp.y * 10;
            z += wp.z * 10;
        }

        // lift off with a jump
        if (!isFlying() && rider.isJumping) liftOff();

        getMoveHelper().setMoveTo(x, y, z, 1);
    }

    /**
     * Returns the distance to the ground while the entity is flying.
     */
    public double getAltitude()
    {
        BlockPos.Mutable pos = new BlockPos.Mutable(getPosition());
        while (pos.getY() > 0 && !world.getBlockState(pos).isSolid()) pos.move(0, -1, 0);

        return getPosY() - pos.getY();
    }
    /**
     * Causes this entity to lift off if it can fly.
     */
    public void liftOff()
    {
        if (canFly()) jump();
    }

    @Override
    protected float getJumpUpwardsMotion()
    {
        // stronger jumps for easier lift-offs
        return canFly()? 1 : super.getJumpUpwardsMotion();
    }

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier)
    {
        if (canFly()) return false;
        return super.onLivingFall(distance, damageMultiplier);
    }

    /**
     * Handles entity death timer, experience orb and particle creation
     */
    @Override
    protected void onDeathUpdate()
    {
        // unmount any riding entities
        removePassengers();

        // freeze at place
        setMotion(Vec3d.ZERO);
        rotationYaw = prevRotationYaw;
        rotationYawHead = prevRotationYawHead;

        if (deathTime >= getMaxDeathTime()) remove(); // actually delete entity after the time is up

        deathTime++;
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        // heal
        if (getHealthRelative() < 1 && isFoodItem(stack))
        {
            heal(stack.getItem().getFood().getHealing());
            playSound(getEatSound(stack), 0.7f, 1);
            stack.shrink(1);
            return true;
        }

        // saddle up!
        if (isTamedFor(player) && !isChild() && !isSaddled() && stack.getItem() instanceof SaddleItem)
        {
            stack.shrink(1);
            setSaddled(true);
            playSound(SoundEvents.ENTITY_HORSE_SADDLE, 1, 1);
            return true;
        }

        // tame
        if (isBreedingItem(stack) && !isTamed())
        {
            stack.shrink(1);
            if (isServer()) tamedFor(player, getRNG().nextInt(5) == 0);
            return true;
        }

        // sit!
        if (isTamedFor(player) && player.isShiftKeyDown())
        {
            if (isServer())
            {
                navigator.clearPath();
                if (!isSitting()) setAttackTarget(null);
                sitGoal.setSitting(!isSitting());
            }
            return true;
        }

        // ride on
        if (isServer() && isTamed() && isSaddled() && !isChild() && (!isBreedingItem(stack) && canReproduce()))
        {
            setRidingPlayer(player);
            sitGoal.setSitting(false);
            navigator.clearPath();
            setAttackTarget(null);
            return true;
        }

        return super.processInteract(player, hand);
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected SoundEvent getAmbientSound()
    {
        if (getRNG().nextInt(5) == 0) return SoundEvents.ENTITY_ENDER_DRAGON_GROWL;
        return DMLSounds.DRAGON_BREATHE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return SoundEvents.ENTITY_ENDER_DRAGON_HURT; }

    public SoundEvent getStepSound() { return DMLSounds.DRAGON_STEP.get(); }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected SoundEvent getDeathSound() { return DMLSounds.DRAGON_DEATH.get(); }

    @Override
    public SoundEvent getEatSound(ItemStack itemStackIn) { return SoundEvents.ENTITY_GENERIC_EAT; }

    public SoundEvent getAttackSound() { return SoundEvents.ENTITY_GENERIC_EAT; }

    public SoundEvent getWingsSound() { return SoundEvents.ENTITY_ENDER_DRAGON_FLAP; }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void playStepSound(BlockPos entityPos, BlockState state)
    {
        if (isInWater()) return;

        // override sound type if the top block is snowy
        SoundType soundType = state.getSoundType();
        if (world.getBlockState(entityPos.up()).getBlock() == Blocks.SNOW)
            soundType = Blocks.SNOW.getSoundType(state, world, entityPos, this);

        // play stomping for bigger dragons
        SoundEvent stepSound = getStepSound();
        if (isHatchling()) stepSound = soundType.getStepSound();

        playSound(stepSound, soundType.getVolume(), -1f);
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    @Override
    public int getTalkInterval() { return 240; }

    @Override
    protected float getSoundVolume() { return getScale(); }

    @Override
    protected float getSoundPitch() { return getScale() - 2; }

    public float getSoundPitch(SoundEvent sound) { return getSoundPitch(); }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) { playSound(soundIn, volume, pitch, false); }

    public void playSound(SoundEvent sound, float volume, float pitch, boolean local)
    {
        if (isSilent()) return;

        volume *= getSoundVolume();
        pitch *= getSoundPitch(sound);

        if (local) world.playSound(getPosX(), getPosY(), getPosZ(), sound, getSoundCategory(), volume, pitch, false);
        else world.playSound(null, getPosX(), getPosY(), getPosZ(), sound, getSoundCategory(), volume, pitch);
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean isFoodItem(ItemStack stack)
    {
        return stack.getItem().isFood() && stack.getItem().getFood().isMeat();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack)
    {
        return ItemTags.FISHES.contains(stack.getItem());
    }

    public void tamedFor(PlayerEntity player, boolean successful)
    {
        if (successful)
        {
            setTamed(true);
            navigator.clearPath();
            setAttackTarget(null);
            setOwnerId(player.getUniqueID());
            if (world.isRemote) playTameEffect(true);
            world.setEntityState(this, (byte) 7);
        }
        else
        {
            if (world.isRemote) playTameEffect(false);
            world.setEntityState(this, (byte) 6);
        }
    }

    public boolean isTamedFor(PlayerEntity player) { return isTamed() && isOwner(player); }

    public void addImmunities(DamageSource... sources) { damageImmunities.addAll(Arrays.asList(sources)); }

    /**
     * Returns the height of the eyes. Used for looking at other entities.
     */
    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn)
    {
        float eyeHeight = super.getStandingEyeHeight(poseIn, sizeIn);

        if (isSitting()) eyeHeight *= 0.8f;

        return eyeHeight;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
    public double getMountedYOffset()
    {
        return (isSitting()? 1.7f : 2f) * getScale();
    }

    /**
     * Returns render size modifier
     */
    @Override
    public float getRenderScale()
    {
        return getScale();
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    public boolean canDespawn(double distanceToClosestPlayer) { return false; }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder()
    {
        // this better doesn't happen...
        return false;
    }

    @Override
    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn)
    {
        super.dropSpecialItems(source, looting, recentlyHitIn);

        if (isSaddled()) entityDropItem(Items.SADDLE);
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean attacked = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) getAttribute(ATTACK_DAMAGE).getValue());

        if (attacked) applyEnchantments(this, entityIn);

        return attacked;
    }

    public void onWingsDown(float speed)
    {
        if (!isInWater())
        {
            // play wing sounds
            float pitch = (1 - speed);
            float volume = 0.3f + (1 - speed) * 0.2f;
            playSound(getWingsSound(), volume, pitch, true);
        }
    }

    @Override
    public void swingArm(Hand hand)
    {
        // play eating sound
        playSound(getAttackSound(), 1, 0.7f);

        // play attack animation
        if (isServer())
            ((ServerWorld) world).getChunkProvider().sendToAllTracking(this, new SAnimateHandPacket(this, 0));
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource src, float par2)
    {
        if (isInvulnerableTo(src)) return false;

        // don't just sit there!
        if (isServer()) sitGoal.setSitting(false);

        return super.attackEntityFrom(src, par2);
    }

    /**
     * Return whether this entity should be rendered as on fire.
     */
    @Override
    public boolean canRenderOnFire()
    {
        return super.canRenderOnFire() && !isImmuneToFire() && !isInvulnerableTo(DamageSource.IN_FIRE);
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMateWith(AnimalEntity mate)
    {
        if (mate == this) return false; // No. Just... no.
        else if (!(mate instanceof TameableDragonEntity)) return false;
        else if (!canReproduce()) return false;

        TameableDragonEntity dragonMate = (TameableDragonEntity) mate;

        if (!dragonMate.isTamed()) return false;
        else if (!dragonMate.canReproduce()) return false;
        else return isInLove() && dragonMate.isInLove();
    }

    public boolean canReproduce() { return isTamed() && reproCount < REPRO_LIMIT; }

    /**
     * This function is used when two same-species animals in 'love mode' breed to generate the new baby animal.
     */
    @Override
    public AgeableEntity createChild(AgeableEntity mate)
    {
        if (!(mate instanceof TameableDragonEntity))
            throw new IllegalArgumentException("The mate isn't a dragon");

        DragonEggEntity egg;

        // pick a breed to inherit from
        if (getRNG().nextBoolean()) egg = new DragonEggEntity(EnumEggTypes.getByType(getType()), world);
        else egg = new DragonEggEntity(EnumEggTypes.getByType(mate.getType()), world);

        // mix the custom names in case both parents have one
        if (hasCustomName() && mate.hasCustomName())
        {
            String p1Name = getCustomName().getString();
            String p2Name = mate.getCustomName().getString();
            String babyName;

            if (p1Name.contains(" ") || p2Name.contains(" "))
            {
                // combine two words with space
                // "Tempor Invidunt Dolore" + "Magna"
                // = "Tempor Magna" or "Magna Tempor"
                String[] p1Names = p1Name.split(" ");
                String[] p2Names = p2Name.split(" ");

                p1Name = DragonBreedGoal.fixChildName(p1Names[rand.nextInt(p1Names.length)]);
                p2Name = DragonBreedGoal.fixChildName(p2Names[rand.nextInt(p2Names.length)]);

                babyName = rand.nextBoolean()? p1Name + " " + p2Name : p2Name + " " + p1Name;
            }
            else
            {
                // scramble two words
                // "Eirmod" + "Voluptua"
                // = "Eirvolu" or "Volueir" or "Modptua" or "Ptuamod" or ...
                if (rand.nextBoolean()) p1Name = p1Name.substring(0, (p1Name.length() - 1) / 2);
                else p1Name = p1Name.substring((p1Name.length() - 1) / 2);

                if (rand.nextBoolean()) p2Name = p2Name.substring(0, (p2Name.length() - 1) / 2);
                else p2Name = p2Name.substring((p2Name.length() - 1) / 2);

                p2Name = DragonBreedGoal.fixChildName(p2Name);

                babyName = rand.nextBoolean()? p1Name + p2Name : p2Name + p1Name;
            }

            egg.setCustomName(new StringTextComponent(babyName));
        }

        // increase reproduction counter
        addReproCount();
        ((TameableDragonEntity) mate).addReproCount();
        egg.setPosition(getPosX(), getPosY(), getPosZ());
        world.addEntity(egg);

        return null; // An egg isnt an ageable!
    }

    @Override
    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
    {
        if (target instanceof TameableEntity) return !Objects.equals(((TameableEntity) target).getOwner(), owner);
        return true;
    }

    /**
     * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
     * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
     */
    @Override
    public Entity getControllingPassenger()
    {
        List<Entity> list = getPassengers();
        return list.isEmpty()? null : list.get(0);
    }

    @Override
    public boolean canPassengerSteer()
    {
        // must always return false or the vanilla movement code interferes
        // with DragonMoveHelper
        return false;
    }

    public PlayerEntity getRidingPlayer()
    {
        Entity entity = getControllingPassenger();
        if (entity instanceof PlayerEntity) return (PlayerEntity) entity;
        else return null;
    }

    public void setRidingPlayer(PlayerEntity player)
    {
        player.rotationYaw = rotationYaw;
        player.rotationPitch = rotationPitch;
        player.startRiding(this);
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        Entity riddenByEntity = getControllingPassenger();
        if (riddenByEntity != null)
        {
            Vec3d pos = new Vec3d(0, getMountedYOffset() + riddenByEntity.getYOffset(), 0.8 * getScale())
                    .rotateYaw((float) Math.toRadians(-renderYawOffset))
                    .add(getPositionVec());
            passenger.setPosition(pos.x, pos.y, pos.z);

            // fix rider rotation
            if (getRidingEntity() instanceof LivingEntity)
            {
                LivingEntity rider = ((LivingEntity) riddenByEntity);
                rider.prevRotationPitch = rider.rotationPitch;
                rider.prevRotationYaw = rider.rotationYaw;
                rider.renderYawOffset = renderYawOffset;
            }
        }
    }

    public boolean isInvulnerableTo(DamageSource src)
    {
        Entity srcEnt = src.getTrueSource();
        if (srcEnt != null)
        {
            // ignore own damage
            if (srcEnt == this) return true;

            // ignore damage from riders
            if (isPassenger(srcEnt)) return true;
        }

        return damageImmunities.contains(src);
    }

    /**
     * Returns the entity's health relative to the maximum health.
     *
     * @return health normalized between 0 and 1
     */
    public double getHealthRelative() { return getHealth() / (double) getMaxHealth(); }

    public int getDeathTime() { return deathTime; }

    public int getMaxDeathTime() { return 120; }

    public void setAttackDamage(double damage)
    {
        getAttribute(ATTACK_DAMAGE).setBaseValue(damage);
    }

    /**
     * Public wrapper for protected final setScale(), used by DragonLifeStageHelper.
     */
    @Override
    public void recalculateSize()
    {
        double posXTmp = getPosX();
        double posYTmp = getPosY();
        double posZTmp = getPosZ();
        boolean onGroundTmp = onGround;

        super.recalculateSize();

        // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client positioning
        setPosition(posXTmp, posYTmp, posZTmp);

        // otherwise, setScale stops the dragon from landing while it is growing
        onGround = onGroundTmp;
    }

    /**
     * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
     * positive, it get's decremented each tick. Don't confuse this with EntityLiving.getAge. With a negative value the
     * Entity is considered a child.
     */
    @Override
    public int getGrowingAge()
    {
        // adapter for vanilla code to enable breeding interaction
        return isAdult()? 0 : -1;
    }

    /**
     * The age value may be negative or positive or zero. If it's negative, it get's incremented on each tick, if it's
     * positive, it get's decremented each tick. With a negative value the Entity is considered a child.
     */
    @Override
    public void setGrowingAge(int age) {/* managed by DragonLifeStageHelper, so this is a no-op*/}

    @Override
    public EntitySize getSize(Pose poseIn) { return new EntitySize(BASE_WIDTH * getScale(), BASE_HEIGHT * getScale(), false); }

    /**
     * Returns the size multiplier for the current age.
     *
     * @return scale
     */
    public float getScale() { return getLifeStageController().getScale(); }

    public boolean isHatchling() { return getLifeStageController().isHatchling(); }

    public boolean isJuvenile() { return getLifeStageController().isJuvenile(); }

    public boolean isAdult() { return getLifeStageController().isAdult(); }

    @Override
    public boolean isChild() { return !isAdult(); }

    /**
     * Checks if this entity is running on a client.
     * <p>
     * Required since MCP's isClientWorld returns the exact opposite...
     *
     * @return true if the entity runs on a client or false if it runs on a server
     */
    public final boolean isClient()
    {
        return world.isRemote;
    }

    /**
     * Checks if this entity is running on a server.
     *
     * @return true if the entity runs on a server or false if it runs on a client
     */
    public final boolean isServer()
    {
        return !world.isRemote;
    }
}
