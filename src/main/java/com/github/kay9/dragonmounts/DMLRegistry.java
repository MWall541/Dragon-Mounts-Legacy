package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraftforge.registries.ForgeRegistries.Keys;

public class DMLRegistry
{
    protected static void init(IEventBus bus)
    {
        REGISTRIES.values().forEach(r -> r.register(bus));
        REGISTRIES.clear(); // Registration happens once, no need to stick around.
    }

    private static final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> REGISTRIES = new HashMap<>();

    public static final RegistryObject<Block> EGG_BLOCK = register("dragon_egg", Registries.BLOCK, HatchableEggBlock::new);

    public static final RegistryObject<Item> EGG_BLOCK_ITEM = register(EGG_BLOCK.getId().getPath(), Registries.ITEM, HatchableEggBlock.Item::new);
    public static final RegistryObject<Item> SPAWN_EGG = register("spawn_egg", Registries.ITEM, DragonSpawnEgg::new);

    public static final RegistryObject<SoundEvent> DRAGON_AMBIENT_SOUND = sound("entity.dragon.ambient");
    public static final RegistryObject<SoundEvent> DRAGON_STEP_SOUND = sound("entity.dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH_SOUND = sound("entity.dragon.death");
    public static final RegistryObject<SoundEvent> GHOST_DRAGON_AMBIENT = sound("entity.dragon.ambient.ghost");

    public static final RegistryObject<EntityType<TameableDragon>> DRAGON = entity("dragon", EntityType.Builder.of(TameableDragon::new, MobCategory.CREATURE).sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT).clientTrackingRange(10).updateInterval(3));

    public static final RegistryObject<BlockEntityType<HatchableEggBlockEntity>> EGG_BLOCK_ENTITY = register("dragon_egg", Registries.BLOCK_ENTITY_TYPE, () -> BlockEntityType.Builder.of(HatchableEggBlockEntity::new, EGG_BLOCK.get()).build(null));

    public static final RegistryObject<Codec<DragonEggLootMod>> EGG_LOOT_MODIFIER = register("dragon_egg_loot", Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, () -> DragonEggLootMod.CODEC);

    public static final RegistryObject<LootItemConditionType> RANDOM_CHANCE_CONFIG_CONDITION = register("random_chance_by_config", Registries.LOOT_CONDITION_TYPE, () -> new LootItemConditionType(new RandomChanceByConfig.Serializer()));

    public static final RegistryObject<MemoryModuleType<Unit>> SIT_MEMORY = brainMemory("is_sitting", Codec.unit(Unit.INSTANCE)); // must be serialized since TamableAnimal does not use getters/setters
    public static final RegistryObject<Activity> SIT_ACTIVITY = brainActivity("sit");

    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.Builder<T> builder)
    {
        return register(name, Registries.ENTITY_TYPE, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
    }

    private static RegistryObject<SoundEvent> sound(String name)
    {
        return register(name, Registries.SOUND_EVENT, () -> SoundEvent.createVariableRangeEvent(DragonMountsLegacy.id(name)));
    }

    private static <T> RegistryObject<MemoryModuleType<T>> brainMemory(String name, @Nullable Codec<T> serializeCodec)
    {
        return register(name, Registries.MEMORY_MODULE_TYPE, () -> new MemoryModuleType<>(Optional.ofNullable(serializeCodec)));
    }

    private static RegistryObject<Activity> brainActivity(String name)
    {
        return register(name, Registries.ACTIVITY, () -> new Activity(name));
    }

    @SuppressWarnings("unchecked")
    private static <T, I extends T> RegistryObject<I> register(String name, ResourceKey<Registry<T>> forType, Supplier<I> sup)
    {
        var registry = (DeferredRegister<T>) REGISTRIES.computeIfAbsent(forType, t ->
        {
            var fr = RegistryManager.ACTIVE.getRegistry(forType);
            if (fr == null) return DeferredRegister.create(forType, DragonMountsLegacy.MOD_ID);
            return DeferredRegister.create(fr, DragonMountsLegacy.MOD_ID);
        });
        return registry.register(name, sup);
    }
}
