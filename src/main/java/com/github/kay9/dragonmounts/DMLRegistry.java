package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
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

    public static final RegistryObject<Block> EGG_BLOCK = register("dragon_egg", Registry.BLOCK_REGISTRY, DMLEggBlock::new);

    public static final RegistryObject<Item> EGG_BLOCK_ITEM = register(EGG_BLOCK.getId().getPath(), Registry.ITEM_REGISTRY, DMLEggBlock.Item::new);
    public static final RegistryObject<Item> SPAWN_EGG = register("spawn_egg", Registry.ITEM_REGISTRY, DragonSpawnEgg::new);

    public static final RegistryObject<SoundEvent> DRAGON_AMBIENT_SOUND = sound("entity.dragon.ambient");
    public static final RegistryObject<SoundEvent> DRAGON_STEP_SOUND = sound("entity.dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH_SOUND = sound("entity.dragon.death");
    public static final RegistryObject<SoundEvent> GHOST_DRAGON_AMBIENT = sound("entity.dragon.ambient.ghost");

    public static final RegistryObject<EntityType<TameableDragon>> DRAGON = entity("dragon", EntityType.Builder.of(TameableDragon::new, MobCategory.CREATURE).sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT).clientTrackingRange(10).updateInterval(3));
    public static final RegistryObject<EntityType<DragonEgg>> DRAGON_EGG = entity("dragon_egg", EntityType.Builder.of(DragonEgg::new, MobCategory.MISC).sized(DragonEgg.WIDTH, DragonEgg.HEIGHT).clientTrackingRange(5).updateInterval(8));

    public static final RegistryObject<BlockEntityType<DMLEggBlock.Entity>> EGG_BLOCK_ENTITY = register("dragon_egg", Registry.BLOCK_ENTITY_TYPE_REGISTRY, () -> BlockEntityType.Builder.of(DMLEggBlock.Entity::new, EGG_BLOCK.get()).build(null));

    public static final RegistryObject<Codec<DragonEggLootMod>> EGG_LOOT_MODIFIER = register("dragon_egg_loot", Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, () -> DragonEggLootMod.CODEC);

    public static final RegistryObject<LootItemConditionType> RANDOM_CHANCE_CONFIG_CONDITION = register("random_chance_by_config", Registry.LOOT_ITEM_REGISTRY, () -> new LootItemConditionType(new RandomChanceByConfig.Serializer()));

    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.Builder<T> builder)
    {
        return register(name, Registry.ENTITY_TYPE_REGISTRY, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
    }

    private static RegistryObject<SoundEvent> sound(String name)
    {
        return register(name, Registry.SOUND_EVENT_REGISTRY, () -> new SoundEvent(DragonMountsLegacy.id(name)));
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
