package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.client.DragonAnimator;
import com.github.kay9.dragonmounts.client.KeyMappings;
import com.github.kay9.dragonmounts.client.MountCameraManager;
import com.github.kay9.dragonmounts.client.MountControlsMessenger;
import com.github.kay9.dragonmounts.data.CrossBreedingManager;
import com.github.kay9.dragonmounts.dragon.ai.*;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;

/**
 * Here be dragons.
 * <p>
 * Let the legacy live on.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @author Kay9 and Xvareon
 */
@SuppressWarnings({"deprecation", "SameReturnValue"})
public class TameableDragon extends TamableAnimal implements Saddleable, FlyingAnimal, PlayerRideable, KeybindUsingMount, MenuProvider
{
    // base attributes
    public static final double BASE_SPEED_GROUND = 0.3; // actual speed varies from ground friction
    public static final double BASE_SPEED_FLYING = 0.32;
    public static final double BASE_DAMAGE = 8;
    public static final double BASE_HEALTH = 100;
    public static final double BASE_FOLLOW_RANGE = 24;
    public static final int BASE_KB_RESISTANCE = 1;
    public static final float BASE_WIDTH = 2.75f; // adult sizes
    public static final float BASE_HEIGHT = 2.75f;
    public static final int BASE_REPRO_LIMIT = 2;
    public static final int BASE_GROWTH_TIME = 72000;
    public static final float BASE_SIZE_MODIFIER = 1.0f;

    // data value IDs
    private static final EntityDataAccessor<String> DATA_BREED = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ARMOR_TYPE = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.INT);
    private static final DragonArmorType[] ARMOR_VALUES = DragonArmorType.values();
    private static final EntityDataAccessor<Boolean> DATA_HAS_CHEST = SynchedEntityData.defineId(TameableDragon.class, EntityDataSerializers.BOOLEAN);
    private SimpleContainer chestInventory;
    private ItemStack chestItem = ItemStack.EMPTY;

    // data NBT IDs
    public static final String NBT_BREED = "Breed";
    private static final String NBT_SADDLED = "Saddle";
    private static final String NBT_REPRO_COUNT = "ReproCount";

    // other constants
    public static final int AGE_UPDATE_INTERVAL = 100; // every 5 seconds
    public static final UUID SCALE_MODIFIER_UUID = UUID.fromString("856d4ba4-9ffe-4a52-8606-890bb9be538b"); // just a random uuid I took online
    public static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("2c7a6c2e-6f4c-4d4e-9f19-9c1d2bb6a111"); // just a random uuid I took online (1)
    public static final int GROUND_CLEARENCE_THRESHOLD = 3; // height in blocks (multiplied by scale of dragon)

    // server/client delegates
    private final DragonAnimator animator;
    private final List<Ability> abilities = new ArrayList<>();
    private DragonBreed breed;
    private int reproCount;
    private float ageProgress = 1; // default to adult
    private boolean flying;
    private boolean nearGround;

    private final GroundPathNavigation groundNavigation;
    private final FlyingPathNavigation flyingNavigation;

    public TameableDragon(EntityType<? extends TameableDragon> type, Level level)
    {
        super(type, level);

        noCulling = true;

        moveControl = new DragonMoveController(this);
        animator = level.isClientSide? new DragonAnimator(this) : null;

        flyingNavigation = new FlyingPathNavigation(this, level);
        groundNavigation = new GroundPathNavigation(this, level);

        flyingNavigation.setCanFloat(true);
        groundNavigation.setCanFloat(true);

        navigation = groundNavigation;
    }

    @Override
    @NotNull
    public BodyRotationControl createBodyControl()
    {
        return new DragonBodyController(this);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(MOVEMENT_SPEED, BASE_SPEED_GROUND)
                .add(MAX_HEALTH, BASE_HEALTH)
                .add(FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(KNOCKBACK_RESISTANCE, BASE_KB_RESISTANCE)
                .add(ATTACK_DAMAGE, BASE_DAMAGE)
                .add(FLYING_SPEED, BASE_SPEED_FLYING)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void registerGoals()
    {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));

        if (DMLConfig.isBreathEnabled()) {
            this.goalSelector.addGoal(3, new DragonFireballAttackGoal(this));
        }

        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(5, new DragonFollowOwnerGoal(this, 1f, 24f, 3.5f, 32f));
        goalSelector.addGoal(6, new DragonBreedGoal(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.85f));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, LivingEntity.class, 16f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NonTameRandomTargetGoal<>(this, Animal.class, false, e -> !(e instanceof TameableDragon)));
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();

        entityData.define(DATA_BREED,"");
        entityData.define(DATA_SADDLED, false);
        entityData.define(DATA_AGE, 0); // default to adult stage
        entityData.define(DATA_ARMOR_TYPE, DragonArmorType.NONE.ordinal());
        entityData.define(DATA_HAS_CHEST, false);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> data)
    {
        if (DATA_BREED.equals(data))
        {
            setBreed(BreedRegistry.get(entityData.get(DATA_BREED), level().registryAccess()));
            updateAgeProperties();
        }
        else if (DATA_FLAGS_ID.equals(data)) refreshDimensions();
        else if (DATA_AGE.equals(data)) updateAgeProperties();
        else if (DATA_ARMOR_TYPE.equals(data)) updateArmorAttributes();
        else super.onSyncedDataUpdated(data);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(NBT_SADDLED, isSaddled());
        compound.putInt(NBT_REPRO_COUNT, reproCount);

        if (getBreed() != null) // breed is not read by the time the packet is being sent...
        {
            if (hasArmor())
                compound.putString("DragonArmor", getArmorType().name());

            compound.putString(NBT_BREED, getBreed().id(level().registryAccess()).toString());
            for (var ability : getAbilities()) ability.write(this, compound);
        }

        // Chest given
        if (!this.chestItem.isEmpty()) {
            compound.put("ChestItemType", this.chestItem.save(new CompoundTag()));
        }

        // Chest inventory
        if (hasChest() && chestInventory != null) {
            CompoundTag invTag = new CompoundTag();

            for (int i = 0; i < chestInventory.getContainerSize(); i++) {
                ItemStack stack = chestInventory.getItem(i);
                if (!stack.isEmpty()) {
                    invTag.put("Slot" + i, stack.save(new CompoundTag()));
                }
            }

            compound.put("DragonChest", invTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        // read and set breed first before reading everything else so things can override correctly,
        // e.g. attributes.
        var breed = BreedRegistry.get(compound.getString(NBT_BREED), level().registryAccess());
        if (breed != null) setBreed(breed);

        super.readAdditionalSaveData(compound);

        setSaddled(compound.getBoolean(NBT_SADDLED));
        this.reproCount = compound.getInt(NBT_REPRO_COUNT);

        for (var ability : getAbilities()) ability.read(this, compound);

        // set sync age data after we read it in AgeableMob
        entityData.set(DATA_AGE, getAge());

        if (compound.contains("DragonArmor")) {
            setArmorType(DragonArmorType.valueOf(compound.getString("DragonArmor")));
        }

        if (compound.contains("ChestItemType", 10)) {
            this.chestItem = ItemStack.of(compound.getCompound("ChestItemType"));
        }

        if (!this.chestItem.isEmpty()) {
            setHasChest(true);
        }

        if (compound.contains("DragonChest")) {
            if (chestInventory == null) chestInventory = new SimpleContainer(27);

            CompoundTag invTag = compound.getCompound("DragonChest");
            for (int i = 0; i < chestInventory.getContainerSize(); i++) {
                if (invTag.contains("Slot" + i)) {
                    chestInventory.setItem(i, ItemStack.of(invTag.getCompound("Slot" + i)));
                }
            }
        }
    }

    public void setBreed(DragonBreed dragonBreed)
    {
        if (breed != dragonBreed) // prevent loops, unnecessary work, etc.
        {
            if (breed != null) breed.close(this);
            this.breed = dragonBreed;
            breed.initialize(this);
            getEntityData().set(DATA_BREED, breed.id(level().registryAccess()).toString());
        }
    }

    /**
     * Since a breed type cannot be passed into the constructor (due to the dynamic nature of breeds)
     * and sometimes a breed type cannot be deserialized in time, there's always the possibility of
     * a nullable breed.
     */
    @Nullable
    public DragonBreed getBreed()
    {
        return breed;
    }

    /**
     * For ease of use when we aren't guaranteed on the breed
     */
    public Optional<DragonBreed> getBreedOptionally()
    {
        return Optional.ofNullable(breed);
    }

    public List<Ability> getAbilities()
    {
        return abilities;
    }

    /**
     * Returns true if the dragon is saddled.
     */
    public boolean isSaddled()
    {
        return entityData.get(DATA_SADDLED);
    }

    @Override
    public boolean isSaddleable()
    {
        return isAlive() && !isHatchling() && isTame();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource source)
    {
        setSaddled(true);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, getSoundSource(), 1, 1);
    }

    /**
     * Set or remove the saddle of the dragon.
     */
    public void setSaddled(boolean saddled)
    {
        entityData.set(DATA_SADDLED, saddled);
    }

    public void addReproCount()
    {
        reproCount++;
    }

    public boolean canFly()
    {
        // hatchling's can't fly
        return !isHatchling();
    }

    public boolean shouldFly()
    {
        if (isFlying()) return !onGround(); // more natural landings
        return canFly() && !isInWater() && !isNearGround();
    }

    /**
     * Returns true if the entity is flying.
     */
    public boolean isFlying()
    {
        return flying;
    }

    /**
     * Set the flying flag of the entity.
     */
    public void setFlying(boolean flying)
    {
        this.flying = flying;
    }

    public boolean isNearGround()
    {
        return nearGround;
    }

    public void setNavigation(boolean flying)
    {
            navigation = flying ?
                    flyingNavigation :
                    groundNavigation;
    }

    @Override
    public void tick()
    {
        if (isServer() && breed == null) // if we don't have a breed at this point, we should assume we aren't getting one, so assign a random one.
            setBreed(BreedRegistry.getRandom(level().registryAccess(), getRandom()));

        super.tick();

        // Ensure entity has an owner
        if (this.getOwner() instanceof Player owner) {
            // Check if the owner is still riding
            if (!this.getPassengers().contains(owner)) {
                dismountAllPassengers();
            }

            // Check if the owner is alive
            if (!owner.isAlive()) {
                dismountAllPassengers();
            }
        }

        if (isServer())
        {
            // periodically sync age data back to client
            if (!isAdult() && tickCount % AGE_UPDATE_INTERVAL == 0) entityData.set(DATA_AGE, age);

            // heal randomly
            if (isAlive() && getRandom().nextFloat() < 0.001) heal(1f);
        }
        else
        {
            // update animations on the client
            animator.tick();

            // because vanilla age does not increment on client...
            int age = getAge();
            if (age < 0) setAge(++age);
            else if (age > 0) setAge(--age);
        }

        // update nearGround state when moving for flight and animation logic
        nearGround = onGround() || !level().noCollision(this, new AABB(getX(), getY(), getZ(), getX(), getY() - (GROUND_CLEARENCE_THRESHOLD * getScale()), getZ()));

        // update flying state based on the distance to the ground
        boolean flying = shouldFly();
        if (flying != isFlying())
        {
            setFlying(flying);
            if (isServer()) setNavigation(flying);
        }

        // ----------------------------
        // FORCE LOOK AT TARGET START
        // ----------------------------
        LivingEntity target = getTarget();
        if (target != null && canAttack(target)) {
            double dx = target.getX() - getX();
            double dy = target.getY(0.3) - getEyeY(); // aim roughly at center
            double dz = target.getZ() - getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
            float pitch = (float) (-(Math.atan2(dy, distXZ) * (180.0 / Math.PI)));

            setYRot(yaw);       // rotate body
            setXRot(pitch);     // rotate pitch
            yHeadRot = yBodyRot = getYRot(); // sync head with body
        }
        // ----------------------------
        // FORCE LOOK AT TARGET END
        // ----------------------------

        updateAgeProgress();
        for (var ability : getAbilities()) ability.tick(this);
    }

    public void dismountAllPassengers() {
        for (Entity passenger : this.getPassengers()) {
            passenger.stopRiding();
        }
    }

    @Override
    public void travel(@NotNull Vec3 vec3)
    {
        if (isFlying())
        {
            if (isControlledByLocalInstance())
            {
                // Move relative to yaw - handled in the move controller or by driver
                moveRelative(getSpeed(), vec3);
                move(MoverType.SELF, getDeltaMovement());
                if (getDeltaMovement().lengthSqr() < 0.1) // we're not actually going anywhere, bob up and down.
                    setDeltaMovement(getDeltaMovement().add(0, Math.sin(tickCount / 4f) * 0.03, 0));
                setDeltaMovement(getDeltaMovement().scale(0.9f)); // smoothly slow down
            }

            calculateEntityAnimation(true);
        }
        else super.travel(vec3);
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(Player driver, Vec3 move)
    {
        double moveSideways = move.x;
        double moveY = move.y;
        double moveForward = Math.min(Math.abs(driver.zza) + Math.abs(driver.xxa), 1);

        if (isFlying() && hasLocalDriver())
        {
            moveForward = moveForward > 0? moveForward : 0;
            if (driver.jumping) moveY = 1;
            else if (KeyMappings.FLIGHT_DESCENT_KEY.isDown()) moveY = -1;
            else if (moveForward > 0 && DMLConfig.cameraDrivenFlight()) moveY = -driver.getXRot() / 90; // normalize from -1 to 1
        }

        // mimic dogshit implementation of AI movement vectors
        // the way this works is that it will mimic how setSpeed in Mob works:
        // it sets the normal speed variable,
        // and then sets the walk forward variable to the same value.
        // so if speed is 0.3, walk forward will also be 0.3 instead of 1.0.
        // so when moveRelative calculates movespeed, (walkforward * speed) we get 0.15.
        // so I guess we should do it to.
        var speed = getRiddenSpeed(driver);
        return new Vec3(moveSideways * speed, moveY * speed, moveForward * speed);
    }

    @Override
    protected void tickRidden(Player driver, Vec3 move)
    {
        // rotate head to match driver.
        float yaw = driver.yHeadRot;
        if (move.z > 0) // rotate in the direction of the drivers controls
            yaw += (float) Mth.atan2(driver.zza, driver.xxa) * (180f / (float) Math.PI) - 90;
        yHeadRot = yaw;
        setXRot(driver.getXRot() * 0.68f);

        // rotate body towards the head
        setYRot(Mth.rotateIfNecessary(yHeadRot, getYRot(), 4));

        if (isControlledByLocalInstance())
        {
            if (!isFlying() && canFly() && driver.jumping) liftOff();
        }
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player driver)
    {
        return (float) getAttributeValue(isFlying()? FLYING_SPEED : MOVEMENT_SPEED);
    }

    @Override
    @SuppressWarnings("ConstantConditions") // I bet the breed exists at this point...
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand)
    {
        var stack = player.getItemInHand(hand);

        var stackResult = stack.interactLivingEntity(player, this, hand);
        if (stackResult.consumesAction()) return stackResult;

        // tame
        if (!isTame())
        {
            if (isServer() && getBreed().tamingItems().contains(stack.getItem().builtInRegistryHolder()))
            {
                stack.shrink(1);
                tamedFor(player, getRandom().nextInt(5) == 0);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS; // pass regardless. We don't want to perform breeding, age ups, etc. on untamed.
        }

        // heal
        if (getHealthFraction() < 1 && isFoodItem(stack))
        {
            //noinspection ConstantConditions
            heal(stack.getItem().getFoodProperties(stack, this).getNutrition());
            playSound(getEatingSound(stack), 0.7f, 1);
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // saddle up!
        if (isTamedFor(player) && isSaddleable() && !isSaddled() && stack.getItem() instanceof SaddleItem)
        {
            stack.shrink(1);
            equipSaddle(getSoundSource());
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // give the saddle back!
        if (isTamedFor(player) && isSaddled() && stack.is(Tags.Items.SHEARS))
        {
            spawnAtLocation(Items.SADDLE);
            player.playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f);
            setSaddled(false);
            gameEvent(GameEvent.SHEAR, player);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // sit!
        if (isTamedFor(player) && (player.isSecondaryUseActive() || stack.is(Items.BONE))) // "bone sitting" for legacy reasons
        {
            if (isServer())
            {
                navigation.stop();
                setOrderedToSit(!isOrderedToSit());
                if (isOrderedToSit()) setTarget(null);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // equip dragon armor
        DragonArmorType armorType = getArmorFromItem(stack);

        if (isTamedFor(player) && armorType != DragonArmorType.NONE) {

            if (isServer()) {

                // store old armor BEFORE replacing
                DragonArmorType oldArmor = getArmorType();

                // set new armor
                setArmorType(armorType);

                // consume held item
                stack.shrink(1);

                // give back old armor if present
                if (oldArmor != DragonArmorType.NONE) {
                    ItemStack oldStack = getItemFromArmor(oldArmor);

                    if (!oldStack.isEmpty()) {
                        if (!player.addItem(oldStack)) {
                            player.drop(oldStack, false);
                        }
                    }
                }

                playSound(SoundEvents.HORSE_ARMOR, 1f, 1f);
            }

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // give that armor back
        if (isTamedFor(player) && hasArmor() && stack.is(Tags.Items.SHEARS)) {

            if (isServer()) {

                DragonArmorType oldArmor = getArmorType();

                setArmorType(DragonArmorType.NONE);

                ItemStack armorStack = getItemFromArmor(oldArmor);

                if (!armorStack.isEmpty()) {
                    if (!player.addItem(armorStack)) {
                        player.drop(armorStack, false);
                    }
                }
            }

            player.playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // put chest on dragon
        if (isTamedFor(player) && !hasChest() && stack.is(Tags.Items.CHESTS)) {

            if (isServer()) {
                this.chestItem = stack.copy();
                this.chestItem.setCount(1);
                setHasChest(true);
                stack.shrink(1);
                playSound(SoundEvents.DONKEY_CHEST, 1f, 1f);
            }

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // remove chest
        if (isTamedFor(player) && hasChest() && stack.is(Tags.Items.SHEARS)) {

            if (isServer()) {
                // drop contents first
                if (chestInventory != null) {
                    for (int i = 0; i < chestInventory.getContainerSize(); i++) {
                        ItemStack stackInSlot = chestInventory.getItem(i);
                        if (!stackInSlot.isEmpty()) {
                            spawnAtLocation(stackInSlot);
                            // chestInventory.setItem(i, ItemStack.EMPTY); // clear after dropping
                        }
                    }
                    chestInventory.clearContent();
                }

                // Drop chest used and fallback to a regular chest if something goes wrong
                if (!this.chestItem.isEmpty()) {
                    spawnAtLocation(this.chestItem);
                } else {
                    spawnAtLocation(Items.CHEST);
                }
                setHasChest(false);
            }

            player.playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // ride on
        if (isSaddled() && !isHatchling() && !isFood(stack) && this.canAddPassenger(player))
        {
            if (isServer())
            {
                // Ensure only the owner or others while the owner is riding can mount
                if (getOwner() != null) {
                    boolean isOwnerRiding = this.getPassengers().contains(getOwner());

                    if (player.getUUID().equals(getOwner().getUUID()) || isOwnerRiding) {
                        player.startRiding(this);
                        navigation.stop();
                        setTarget(null);
                        setOrderedToSit(false);
                        setInSittingPose(false);
                        return InteractionResult.sidedSuccess(level().isClientSide);
                    }
                }
            }
        }

        return super.mobInteract(player, hand);
    }

    public void liftOff()
    {
        if (canFly()) jumpFromGround();
    }

    @Override
    protected float getJumpPower()
    {
        // stronger jumps for easier lift-offs
        return super.getJumpPower() * (canFly()? 3 : 1);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource)
    {
        return !canFly() && super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    @Override
    protected void tickDeath()
    {
        // unmount any riding entities
        ejectPassengers();

        // freeze at place
        setDeltaMovement(Vec3.ZERO);
        setYRot(yRotO);
        setYHeadRot(yHeadRotO);

        if (deathTime >= getMaxDeathTime()) remove(RemovalReason.KILLED); // actually delete entity after the time is up

        deathTime++;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return getBreedOptionally().flatMap(DragonBreed::ambientSound).map(Holder::get).orElse(DMLRegistry.DRAGON_AMBIENT_SOUND.get());
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn)
    {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    public SoundEvent getStepSound()
    {
        return DMLRegistry.DRAGON_STEP_SOUND.get();
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected SoundEvent getDeathSound()
    {
        return DMLRegistry.DRAGON_DEATH_SOUND.get();
    }

    @Override
    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStackIn)
    {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getAttackSound()
    {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getWingsSound()
    {
        return SoundEvents.ENDER_DRAGON_FLAP;
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void playStepSound(@NotNull BlockPos entityPos, @NotNull BlockState state)
    {
        if (isInWater()) return;

        if (isHatchling())
        {
            super.playStepSound(entityPos, state);
            return;
        }

        // override sound type if the top block is snowy
        var soundType = state.getSoundType();
        if (level().getBlockState(entityPos.above()).getBlock() == Blocks.SNOW)
            soundType = Blocks.SNOW.getSoundType(state, level(), entityPos, this);

        // play stomping for bigger dragons
        playSound(getStepSound(), soundType.getVolume(), soundType.getPitch() * getVoicePitch());
    }

    @Override
    public void playAmbientSound()
    {
        if (getBreed() != null) // EntityType likes to invoke this before deserializing, so let's guard it.
            super.playAmbientSound();
    }

    @Override
    public int getAmbientSoundInterval()
    {
        return 240;
    }

    @Override
    protected float getSoundVolume()
    {
        return getScale();
    }

    @Override
    public float getVoicePitch()
    {
        return 2 - getScale();
    }

    @Override
    public ItemStack getPickedResult(HitResult target)
    {
        if (getBreed() == null) return ItemStack.EMPTY;
        return DragonSpawnEgg.create(getBreed(), level().registryAccess());
    }

    @Override
    protected @NotNull Component getTypeName()
    {
        if (getBreed() != null)
            return Component.translatable(DragonBreed.getTranslationKey(getBreed().id(level().registryAccess()).toString()));

        return super.getTypeName();
    }

    public boolean isFoodItem(ItemStack stack) {
        var food = stack.getItem().getFoodProperties(stack, this);

        // Check if it has food properties and is meat
        boolean isMeat = food != null && food.isMeat();

        // Fish items only count if they have food properties
        boolean isEdibleFish = stack.is(net.minecraft.tags.ItemTags.FISHES) && food != null;

        return isMeat || isEdibleFish;
    }

    // the "food" that enables breeding mode
    @Override
    @SuppressWarnings("ConstantConditions") // I bet the breed exists at this point...
    public boolean isFood(ItemStack stack)
    {
        return getBreed().breedingItems().contains(stack.getItem().builtInRegistryHolder());
    }

    public void tamedFor(Player player, boolean successful)
    {
        if (successful)
        {
            setTame(true);
            navigation.stop();
            setTarget(null);
            setOwnerUUID(player.getUUID());
            level().broadcastEntityEvent(this, (byte) 7);
        }
        else
        {
            level().broadcastEntityEvent(this, (byte) 6);
        }
    }

    public boolean isTamedFor(Player player)
    {
        return isTame() && isOwnedBy(player);
    }

    /**
     * Returns the height of the eyes. Used for looking at other entities.
     */
    @Override
    protected float getStandingEyeHeight(@NotNull Pose poseIn, EntityDimensions sizeIn)
    {
        return sizeIn.height * 1.2f;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
    public double getPassengersRidingOffset()
    {
        return getBbHeight() - 0.175;
    }

    /**
     * Returns render size modifier
     * <p>
     * 0.33 is the value representing the size for baby dragons.
     * 1.0 is the value representing the size for adult dragons.
     * We are essentially scaling linearly from baby size to adult size, base on ageProgress
     * This value can be manipulated using the breed's size modifier
     */
    @Override
    public float getScale()
    {
        var mod = getBreed() == null? 1f : getBreed().sizeModifier();
        return (0.33f + (0.67f * getAgeProgress())) * mod;
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean onClimbable()
    {
        // this better doesn't happen...
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHitIn)
    {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);

        // Drop saddle
        if (isSaddled()) spawnAtLocation(Items.SADDLE);

        // Drop block used for the armor
        if (hasArmor()) {
            ItemStack armorStack = getItemFromArmor(getArmorType());

            if (!armorStack.isEmpty()) {
                spawnAtLocation(armorStack);
            }
        }

        // Drop chest + contents
        if (hasChest()) {
            if (!this.chestItem.isEmpty()) {
                spawnAtLocation(this.chestItem);
            } else {
                spawnAtLocation(Items.CHEST);
            }
            this.chestItem = ItemStack.EMPTY;

            if (chestInventory != null) {
                for (int i = 0; i < chestInventory.getContainerSize(); i++) {
                    ItemStack stack = chestInventory.getItem(i);
                    if (!stack.isEmpty()) {
                        spawnAtLocation(stack);
                    }
                }
            }
        }
    }

    @Override
    protected @NotNull ResourceLocation getDefaultLootTable()
    {
        if (getBreed() == null) return BuiltInLootTables.EMPTY;
        return getBreed().deathLoot();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean doHurtTarget(Entity entityIn)
    {
        boolean attacked = entityIn.hurt(damageSources().mobAttack(this), (float) getAttribute(ATTACK_DAMAGE).getValue());

        if (attacked) doEnchantDamageEffects(this, entityIn);

        return attacked;
    }

    public void onWingsDown(float speed)
    {
        if (!isInWater())
        {
            // play wing sounds
            float pitch = (1 - speed);
            float volume = 0.3f + (1 - speed) * 0.2f;
            pitch *= getVoicePitch();
            volume *= getSoundVolume();
            level().playLocalSound(getX(), getY(), getZ(), getWingsSound(), SoundSource.VOICE, volume, pitch, true);
        }
    }

    @Override
    public void swing(@NotNull InteractionHand hand)
    {
        // play eating sound
        playSound(getAttackSound(), 1, 0.7f);
        super.swing(hand);
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(@NotNull DamageSource src, float par2)
    {
        if (isInvulnerableTo(src)) return false;

        // don't just sit there!
        setOrderedToSit(false);

        return super.hurt(src, par2);
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMate(@NotNull Animal mate)
    {
        if (mate == this) return false; // No. Just... no.
        if (!(mate instanceof TameableDragon dragonMate)) return false;
        if (!canReproduce()) return false;

        if (!dragonMate.canReproduce()) return false;

        return isInLove() && mate.isInLove();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canReproduce()
    {
        if (!isTame() || getBreed() == null) return false;

        var limit = getBreed().getReproductionLimit();
        return reproCount < limit || limit == -1;
    }

    @Override
    @SuppressWarnings("ConstantConditions") // breed nullability is checked in canReproduce
    public void spawnChildFromBreeding(@NotNull ServerLevel level, @NotNull Animal animal)
    {
        if (!(animal instanceof TameableDragon mate))
        {
            DragonMountsLegacy.LOG.warn("Tried to mate with non-dragon? Hello? {}", animal);
            return;
        }

        // pick a breed to inherit from, and place hatching.
        var state = DMLRegistry.EGG_BLOCK.get().defaultBlockState().setValue(HatchableEggBlock.HATCHING, true);
        var offSpringBreed = CrossBreedingManager.INSTANCE.getCrossBreed(getBreed(), mate.getBreed(), level.registryAccess());
        if (offSpringBreed == null) offSpringBreed = getRandom().nextBoolean()? getBreed() : mate.getBreed();

        var egg = HatchableEggBlock.place(level, blockPosition(), state, offSpringBreed);

        // mix the custom names in case both parents have one
        if (hasCustomName() && animal.hasCustomName())
        {
            String p1Name = getCustomName().getString();
            String p2Name = animal.getCustomName().getString();
            String babyName;

            if (p1Name.contains(" ") || p2Name.contains(" "))
            {
                // combine two words with space
                // "Tempor Invidunt Dolore" + "Magna"
                // = "Tempor Magna" or "Magna Tempor"
                String[] p1Names = p1Name.split(" ");
                String[] p2Names = p2Name.split(" ");

                p1Name = StringUtils.capitalize(p1Names[getRandom().nextInt(p1Names.length)]);
                p2Name = StringUtils.capitalize(p2Names[getRandom().nextInt(p2Names.length)]);

                babyName = getRandom().nextBoolean()? p1Name + " " + p2Name : p2Name + " " + p1Name;
            }
            else
            {
                // scramble two words
                // "Eirmod" + "Voluptua"
                // = "Eirvolu" or "Volueir" or "Modptua" or "Ptuamod" or ...
                if (getRandom().nextBoolean()) p1Name = p1Name.substring(0, (p1Name.length() - 1) / 2);
                else p1Name = p1Name.substring((p1Name.length() - 1) / 2);

                if (getRandom().nextBoolean()) p2Name = p2Name.substring(0, (p2Name.length() - 1) / 2);
                else p2Name = p2Name.substring((p2Name.length() - 1) / 2);

                p2Name = StringUtils.capitalize(p2Name);

                babyName = getRandom().nextBoolean()? p1Name + p2Name : p2Name + p1Name;
            }

            egg.setCustomName(Component.literal(babyName));
        }

        // increase reproduction counter
        addReproCount();
        mate.addReproCount();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob)
    {
        var offspring = DMLRegistry.DRAGON.get().create(level);
        if (getBreed() != null) offspring.setBreed(getBreed());
        return offspring;
    }

    @Override
    public boolean wantsToAttack(@NotNull LivingEntity target, @NotNull LivingEntity owner) {
        // Never attack the owner or anyone the owner is allied with
        if (target == owner || (owner != null && owner.isAlliedTo(target))) return false;

        // If the target is another Tameable entity, check the owner
        if (target instanceof OwnableEntity ownable) {
            // If the other pet has the same owner, don't attack
            if (Objects.equals(ownable.getOwnerUUID(), owner.getUUID())) {
                return false;
            }
        }

        // Check if the Dragon itself considers the target an ally
        if (this.isAlliedTo(target)) return false;

        return super.wantsToAttack(target, owner);
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        // Prevent attacking hatchlings or while being ridden
        if (isHatchling() || hasControllingPassenger()) return false;

        // Check if the target is an ally
        if (this.isAlliedTo(target)) return false;

        // If the target is another Tameable entity, check the owner
        if (this.isTame()) {
            LivingEntity owner = this.getOwner();

            // Never attack the owner or anyone the owner is allied with
            if (target == owner || (owner != null && owner.isAlliedTo(target))) return false;

            if (target instanceof OwnableEntity ownable) {
                if (this.getOwnerUUID() != null && this.getOwnerUUID().equals(ownable.getOwnerUUID())) {
                    return false;
                }
            }
        }

        return super.canAttack(target);
    }

    public boolean canAddPassenger(@NotNull Entity passenger) {
        return this.getPassengers().size() <= 2;
    }

    /**
     * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
     * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
     */
    @Override
    public LivingEntity getControllingPassenger()
    {
        return getFirstPassenger() instanceof LivingEntity driver && isOwnedBy(driver)? driver : null;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger)
    {
        super.addPassenger(passenger);

        if (passenger instanceof Player)
        {
            passenger.setYRot(getYRot());
            passenger.setXRot(getXRot());
        }

        if (hasLocalDriver())
        {
            MountControlsMessenger.sendControlsMessage();
            MountCameraManager.onDragonMount();
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger)
    {
        if (hasLocalDriver()) MountCameraManager.onDragonDismount();
        super.removePassenger(passenger);
    }

    @Override
    protected void positionRider(@NotNull Entity ridden, @NotNull MoveFunction pCallback)
    {
        int i = this.getPassengers().indexOf(ridden);
        if (i >= 0) {
            boolean flag = i == 0;
            float f = 0.9F;
            float f1 = (float)(this.isRemoved() ? (double)0.01F : (double)0.01F + ridden.getMyRidingOffset()) + 2.5F;
            if (this.getPassengers().size() > 1) {
                if (!flag) {
                    f = 0.0F;
                }

                if (ridden instanceof Animal) {
                    f += 0.2F;
                }
            }

            Vec3 vec3 = (new Vec3(0.0D, 0.0D, f)).yRot(-this.yBodyRot * ((float)Math.PI / 180F));
            pCallback.accept(ridden, this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
            this.clampRotation(ridden);
        }
    }

    private void clampRotation(Entity pEntity) {
        pEntity.setYBodyRot(this.getYRot());
        float f = pEntity.getYRot();
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        float f2 = Mth.clamp(f1, -160.0F, 160.0F);
        pEntity.yRotO += f2 - f1;
        float f3 = f + f2 - f1;
        pEntity.setYRot(f3);
        pEntity.setYHeadRot(f3);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src)
    {
        Entity srcEnt = src.getEntity();
        if (srcEnt != null && (srcEnt == this || hasPassenger(srcEnt))) return true;

        if (getBreed() != null) return getBreed().immunities().contains(src.typeHolder());

        return super.isInvulnerableTo(src);
    }

    /**
     * Returns the entity's health relative to the maximum health.
     *
     * @return health normalized between 0 and 1
     */
    public float getHealthFraction()
    {
        return getHealth() / getMaxHealth();
    }

    public int getMaxDeathTime()
    {
        return 120;
    }

    /**
     * Public wrapper for protected final setScale(), used by DragonLifeStageHelper.
     */
    @Override
    public void refreshDimensions()
    {
        double posXTmp = getX();
        double posYTmp = getY();
        double posZTmp = getZ();
        boolean onGroundTmp = onGround();

        super.refreshDimensions();

        // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client positioning
        setPos(posXTmp, posYTmp, posZTmp);

        // otherwise, setScale stops the dragon from landing while it is growing
        setOnGround(onGroundTmp);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose poseIn)
    {
        var height = isInSittingPose()? 2.15f : BASE_HEIGHT;
        var scale = getScale();
        return new EntityDimensions(BASE_WIDTH * scale, height * scale, false);
    }

    @Override
    public int getAge()
    {
        return age;
    }

    public void updateAgeProgress()
    {
        // no reason to recalculate this value several times per tick/frame...
        float growth = -BASE_GROWTH_TIME;
        if (getBreed() != null) growth = -getBreed().growthTime();
        float min = Math.min(getAge(), 0);
        ageProgress = 1 - (min / growth);
    }

    public float getAgeProgress()
    {
        return ageProgress;
    }

    /**
     * Updates properties/attributes/traits of dragons based on the current age scale.
     * Also syncs the current age to the client.
     * Called at an interval (of ticks) described by {@link TameableDragon#AGE_UPDATE_INTERVAL}
     */
    @SuppressWarnings("ConstantConditions")
    private void updateAgeProperties()
    {
        setAge(entityData.get(DATA_AGE));
        updateAgeProgress();
        refreshDimensions();

        setMaxUpStep(Math.max(2 * getAgeProgress(), 1));

        // update attributes and health only on the server
        if (isServer())
        {
            // health does not update on modifier application, so have to store the health frac first
            var healthFrac = getHealthFraction();

            // negate modifier value since the operation is as follows: base_value += modifier * base_value
            double modValue = -(1d - Math.max(getAgeProgress(), 0.1));
            var mod = new AttributeModifier(SCALE_MODIFIER_UUID, "Dragon size modifier", modValue, AttributeModifier.Operation.MULTIPLY_BASE);
            for (var attribute : new Attribute[]{MAX_HEALTH, ATTACK_DAMAGE, }) // avoid duped code
            {
                AttributeInstance instance = getAttribute(attribute);
                instance.removeModifier(mod);
                instance.addTransientModifier(mod);
            }

            // restore health fraction
            setHealth(healthFrac * getMaxHealth());
        }
    }

    public boolean isHatchling()
    {
        return getAgeProgress() < 0.5f;
    }

    public boolean isJuvenile()
    {
        return getAgeProgress() >= 0.5f && getAgeProgress() < 1f;
    }

    public boolean isAdult()
    {
        return getAgeProgress() >= 1f;
    }

    @Override
    public boolean isBaby()
    {
        return !isAdult();
    }

    @Override
    public void setBaby(boolean baby)
    {
        var growth = -BASE_GROWTH_TIME;
        if (getBreed() != null) growth = -getBreed().growthTime();
        setAge(baby? growth : 0);
        entityData.set(DATA_AGE, age);
    }

    @Override
    public void ageUp(int p_146741_, boolean p_146742_)
    {
        super.ageUp(p_146741_, p_146742_);
        entityData.set(DATA_AGE, getAge());
    }

    // simple helper method to determine if we're on the server thread.
    public boolean isServer()
    {
        return !level().isClientSide;
    }

    public DragonAnimator getAnimator()
    {
        return animator;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        if (getBreed() == null) return super.canBreatheUnderwater();
        return getBreed().immunities().contains(damageSources().drown().typeHolder());
    }

    @Override
    public boolean fireImmune()
    {
//        if (super.fireImmune()) return true;
//        if (getBreed() == null) return false;
//        return getBreed().immunities().contains(damageSources().onFire().typeHolder());

        // Regardless of breed, to avoid the dragon damaging itself with its fire breath
        return true;
    }

    @Override
    protected void onChangedBlock(@NotNull BlockPos pos)
    {
        super.onChangedBlock(pos);
        for (var ability : getAbilities()) ability.onMove(this);
    }

    @Override
    public boolean isInWall()
    {
        if (noPhysics) return false;
        else
        {
            // Reduce suffocation risks. They're fat and clusmy.
            var collider = getBoundingBox().deflate(getBbWidth() * 0.2f);
            return BlockPos.betweenClosedStream(collider).anyMatch((pos) ->
            {
                BlockState state = level().getBlockState(pos);
                return !state.isAir() && state.isSuffocating(level(), pos) && Shapes.joinIsNotEmpty(state.getCollisionShape(level(), pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(collider), BooleanOp.AND);
            });
        }
    }

    @Override
    public @NotNull Vec3 getLightProbePosition(float p_20309_)
    {
        return new Vec3(getX(), getY() + getBbHeight(), getZ());
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean hasLocalDriver()
    {
        return getControllingPassenger() instanceof Player p && p.isLocalPlayer();
    }

    public Vec3 getApproximateMouthPos() {
        float scale = this.getScale();

        // 1. Get the direction the head is pointing
        // Dampening the pitch (0.65f) keeps the projectile from spawning in the ground/sky
        Vec3 rotVector = this.calculateViewVector(this.getXRot() * 0.65f, this.getYHeadRot());

        // 2. Start at the eye position
        Vec3 position = this.getEyePosition(1.0F);

        // 3. SHIFT FORWARD FROM CENTER
        // If it's in the chest, we need to push the origin forward along the body's axis.
        // We use the body's yaw to move the "pivot point" toward the front of the dragon.
        float bodyYaw = (float) Math.toRadians(-this.yBodyRot);
        double bodyPush = 1.5D * scale; // Adjust this to move the "neck base" forward

        double forwardX = position.x + (Math.sin(bodyYaw) * bodyPush);
        double forwardZ = position.z + (Math.cos(bodyYaw) * bodyPush);
        double adjustedY = position.y - (0.5D * scale); // Drop it slightly from eyes to mouth

        position = new Vec3(forwardX, adjustedY, forwardZ);

        // 4. PROJECT TO SNOUT
        // We increase the 1.3D to 2.5D. This is the distance from the neck-base to the tip of the snout.
        double snoutReach = this.getBbWidth() + (2.5D * scale);
        position = position.add(rotVector.scale(snoutReach));

        return position;
    }

    public void performBreathAttack(Entity owner, Vec3 look) {
        if (!isServer()) return;

        Vec3 spawnPos = this.getApproximateMouthPos();

        if (isIceBreed()) {
            IceDragonBreathBall iceBall = new IceDragonBreathBall(level(), this);
            iceBall.setOwner(owner);
            iceBall.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            iceBall.shoot(look.x, look.y, look.z, 2.0F, 1.0F);
            level().addFreshEntity(iceBall);
        } else if (isWitherBreed()) {
            WitherBreathBall witherBall = new WitherBreathBall(level(), this, 0, 0, 0);
            witherBall.setOwner(owner);
            witherBall.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            witherBall.shoot(look.x, look.y, look.z, 2.0F, 1.0F);
            level().addFreshEntity(witherBall);
        } else if (isStormBreed()) {
            StormDragonBreathBall stormBall = new StormDragonBreathBall(level(), this);
            stormBall.setOwner(owner);
            stormBall.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            stormBall.shoot(look.x, look.y, look.z, 2.0F, 1.0F);
            level().addFreshEntity(stormBall);
        } else if (isEndBreed()) {
            EndDragonBreathBall endBall = new EndDragonBreathBall(level(), this, look.x, look.y, look.z);
            endBall.setOwner(owner);
            endBall.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level().addFreshEntity(endBall);
        } else {
            DragonBreathBall fireBall = new DragonBreathBall(level(), this, look.x, look.y, look.z, 1);
            fireBall.setOwner(owner);
            fireBall.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            level().addFreshEntity(fireBall);
        }
    }

    @Override
    public void onKeyPacket(Entity keyPresser) {
        if (DMLConfig.isBreathEnabled()) {
            if (keyPresser.isPassengerOfSameVehicle(this) && isServer()) {
                if (this.getOwner() != null && this.getOwner().equals(keyPresser)) {
                    Vec3 look = this.getLookAngle();
                    this.performBreathAttack(keyPresser, look);
                }
            }
        }
    }

    public class DragonFireballAttackGoal extends Goal {
        private final TameableDragon dragon;

        private int attackCooldown;
        private int burstShotsRemaining = 0;
        private int burstDelay = 0;

        private boolean startedFlying = false;

        private static final int BURST_SIZE = 15;
        private static final int BURST_INTERVAL = 0; // ticks between shots
        private static final int BURST_COOLDOWN = 2; // delay between volleys
        private static final double MAX_ATTACK_DISTANCE_SQR = 2500.0;
        private static final double MIN_FIREBALL_DISTANCE_SQR = 16.0;

        public DragonFireballAttackGoal(TameableDragon dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
            this.attackCooldown = 0; // Initial cooldown
        }

        @Override public boolean canUse() {
            LivingEntity target = dragon.getTarget();
            if (target == null || !target.isAlive() || isFriendly(target)) return false;
            double distance = dragon.distanceToSqr(target);
            return distance <= MAX_ATTACK_DISTANCE_SQR && distance >= MIN_FIREBALL_DISTANCE_SQR;
        }

        @Override public boolean canContinueToUse() {
            LivingEntity target = dragon.getTarget();
            if (target == null || !target.isAlive() || isFriendly(target)) return false;
            double distance = dragon.distanceToSqr(target);
            return distance <= MAX_ATTACK_DISTANCE_SQR && distance >= MIN_FIREBALL_DISTANCE_SQR;
        }

        @Override
        public void tick() {
            LivingEntity target = dragon.getTarget();
            if (target != null) {
                double distance = dragon.distanceToSqr(target);
                if (distance >= MIN_FIREBALL_DISTANCE_SQR) {
                    // ACTIVE BURST (shooting)
                    if (burstShotsRemaining > 0) {
                        if (--burstDelay <= 0) {
                            ensureFlying();
                            shootFireball(target);

                            burstShotsRemaining--;
                            burstDelay = BURST_INTERVAL;
                        }
                        return; // fireball cooldown handled
                    }

                    // COOLDOWN BETWEEN BURSTS
                    if (attackCooldown > 0) {
                        attackCooldown--;
                        return;
                    }

                    // START NEW BURST
                    burstShotsRemaining = BURST_SIZE;
                    burstDelay = 0;
                    attackCooldown = BURST_COOLDOWN;

                    // Ensure dragon lifts off at start of burst if not already flying
                    ensureFlying();
                }

                dragon.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }

        @Override
        public void stop() {
            burstShotsRemaining = 0;
            burstDelay = 0;

            // Only stop flight if this goal caused it
            if (startedFlying) {
                setFlying(false);
                startedFlying = false;
            }
        }

        private void ensureFlying() {
            // Only lift off if not already flying
            if (!dragon.isFlying()) {
                if (this.dragon.getRandom().nextFloat() < 0.025f) {
                    liftOff();
                    setFlying(true);
                    startedFlying = true;
                }
            }
        }

        private void shootFireball(LivingEntity target) {
            if (isServer()) {
                Vec3 mouthPos = dragon.getApproximateMouthPos();
                // Aim exactly at the target's center from the mouth
                Vec3 direction = target.getBoundingBox().getCenter().subtract(mouthPos).normalize();
                dragon.performBreathAttack(dragon, direction);
            }
        }

        private boolean isFriendly(LivingEntity target) {
            // Check if the target is the owner
            if (dragon.isOwnedBy(target)) return true;

            // Check if the target is another tamed entity owned by the same person
            if (target instanceof OwnableEntity ownableTarget) {
                if (dragon.getOwnerUUID() != null && dragon.getOwnerUUID().equals(ownableTarget.getOwnerUUID())) {
                    return true;
                }
            }

            // Check if they are on the same Team (Scoreboard teams)
            return dragon.isAlliedTo(target);
        }
    }

    public enum DragonArmorType {
        NONE(0, "none"),
        COPPER(4, "copper"),
        IRON(8, "iron"),
        GOLD(6, "gold"),
        EMERALD(10, "emerald"),
        DIAMOND(12, "diamond"),
        NETHERITE(16, "netherite");

        public final double armorValue;
        public final String textureName;

        DragonArmorType(double armorValue, String textureName) {
            this.armorValue = armorValue;
            this.textureName = textureName;
        }
    }

    public DragonArmorType getArmorType() {
        return ARMOR_VALUES[entityData.get(DATA_ARMOR_TYPE)];
    }

    public boolean hasArmor() {
        return getArmorType() != DragonArmorType.NONE;
    }

    public void setArmorType(DragonArmorType type) {
        entityData.set(DATA_ARMOR_TYPE, type.ordinal());
        updateArmorAttributes();
    }

    private DragonArmorType getArmorFromItem(ItemStack stack) {
        if (stack.is(Blocks.COPPER_BLOCK.asItem())) return DragonArmorType.COPPER;
        if (stack.is(Blocks.IRON_BLOCK.asItem())) return DragonArmorType.IRON;
        if (stack.is(Blocks.GOLD_BLOCK.asItem())) return DragonArmorType.GOLD;
        if (stack.is(Blocks.EMERALD_BLOCK.asItem())) return DragonArmorType.EMERALD;
        if (stack.is(Blocks.DIAMOND_BLOCK.asItem())) return DragonArmorType.DIAMOND;
        if (stack.is(Blocks.NETHERITE_BLOCK.asItem())) return DragonArmorType.NETHERITE;

        return DragonArmorType.NONE;
    }

    public ItemStack getItemFromArmor(DragonArmorType type) {
        return switch (type) {
            case COPPER -> new ItemStack(Items.COPPER_BLOCK);
            case IRON -> new ItemStack(Items.IRON_BLOCK);
            case GOLD -> new ItemStack(Items.GOLD_BLOCK);
            case DIAMOND -> new ItemStack(Items.DIAMOND_BLOCK);
            case EMERALD -> new ItemStack(Items.EMERALD_BLOCK);
            case NETHERITE -> new ItemStack(Items.NETHERITE_BLOCK);
            default -> ItemStack.EMPTY;
        };
    }

    private void updateArmorAttributes() {
        if (!isServer()) return;

        AttributeInstance armorAttr = getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        armorAttr.removeModifier(ARMOR_MODIFIER_UUID);

        DragonArmorType type = getArmorType();
        if (type != DragonArmorType.NONE) {
            armorAttr.addTransientModifier(
                    new AttributeModifier(
                            ARMOR_MODIFIER_UUID,
                            "Dragon armor",
                            type.armorValue,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }
    }

    public boolean hasChest() {
        return entityData.get(DATA_HAS_CHEST);
    }

    public void setHasChest(boolean value) {
        entityData.set(DATA_HAS_CHEST, value);

        if (value) {
            if (this.chestInventory == null) {
                this.chestInventory = new SimpleContainer(27);
            }
        } else {
            // Cleanup: Remove inventory and the stored chest item
            this.chestInventory = null;
            this.chestItem = ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getName();
    }

    @Override
    public ChestMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {

        if (!hasChest())
            return null;

        return new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, chestInventory, 3);
    }

    public void openChestInventory(Player player) {
        if (isServer() && hasChest()) {
            player.openMenu(this);
        }
    }

    public boolean isIceBreed() {
        if (getBreed() == null) return false;
        ResourceLocation breedId = getBreed().id(level().registryAccess());

        return  breedId.getPath().contains("ice") ||
                breedId.getPath().contains("water") ||
                breedId.getPath().contains("ocean") ||
                breedId.getPath().contains("aether") ||
                breedId.getPath().contains("ghost") ||
                breedId.getPath().contains("lunar") ||
                breedId.getPath().contains("aurora") ||
                breedId.getPath().contains("gale");
    }

    public boolean isStormBreed() {
        if (getBreed() == null) return false;
        ResourceLocation breedId = getBreed().id(level().registryAccess());

        return  breedId.getPath().contains("storm") ||
                breedId.getPath().contains("crystal") ||
                breedId.getPath().contains("bronze");
    }

    public boolean isEndBreed() {
        if (getBreed() == null) return false;
        ResourceLocation breedId = getBreed().id(level().registryAccess());

        return  breedId.getPath().contains("end") ||
                breedId.getPath().contains("magic") ||
                breedId.getPath().contains("sculk") ||
                breedId.getPath().contains("primal_end");
    }

    public boolean isWitherBreed() {
        if (getBreed() == null) return false;
        ResourceLocation breedId = getBreed().id(level().registryAccess());

        return  breedId.getPath().contains("wither") ||
                breedId.getPath().contains("dark");
    }
}