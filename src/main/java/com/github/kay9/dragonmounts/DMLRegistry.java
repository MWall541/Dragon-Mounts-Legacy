package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DMLRegistry
{
    public static void init(IEventBus bus)
    {
        REGISTRIES.values().forEach(r -> r.register(bus));
    }

    private static final Map<IForgeRegistry<?>, DeferredRegister<?>> REGISTRIES = new HashMap<>();

    public static final RegistryObject<Block> EGG_BLOCK = register("dragon_egg", ForgeRegistries.BLOCKS, DMLEggBlock::new);

    public static final RegistryObject<Item> EGG_BLOCK_ITEM = register(EGG_BLOCK.getId().getPath(), ForgeRegistries.ITEMS, DMLEggBlock.Item::new);
    public static final RegistryObject<Item> SPAWN_EGG = register("spawn_egg", ForgeRegistries.ITEMS, DragonSpawnEgg::new);

    public static final RegistryObject<SoundEvent> DRAGON_BREATHE_SOUND = sound("entity.dragon.breathe");
    public static final RegistryObject<SoundEvent> DRAGON_STEP_SOUND = sound("entity.dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH_SOUND = sound("entity.dragon.death");

    public static final RegistryObject<EntityType<TameableDragon>> DRAGON = entity("dragon", EntityType.Builder   .of(TameableDragon::new, MobCategory.CREATURE).sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT).clientTrackingRange(10).updateInterval(3));
    public static final RegistryObject<EntityType<DragonEgg>> DRAGON_EGG = entity("dragon_egg", EntityType.Builder.of(DragonEgg::new, MobCategory.MISC)         .sized(DragonEgg.WIDTH, DragonEgg.HEIGHT)                    .clientTrackingRange(5) .updateInterval(8));

    public static final RegistryObject<BlockEntityType<DMLEggBlock.Entity>> EGG_BLOCK_ENTITY = register("dragon_egg", ForgeRegistries.BLOCK_ENTITIES, () -> BlockEntityType.Builder.of(DMLEggBlock.Entity::new, EGG_BLOCK.get()).build(null));

    public static final Tag<Block> FIRE_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("fire_dragon_habitat_blocks"), ImmutableSet.of(() -> Blocks.FIRE, () -> Blocks.LAVA, () -> Blocks.MAGMA_BLOCK, () -> Blocks.CAMPFIRE));
    public static final Tags.IOptionalNamedTag<Block> FOREST_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("forest_dragon_habitat_blocks"));
    public static final Tags.IOptionalNamedTag<Block> ICE_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("ice_dragon_habitat_blocks"));
    public static final Tags.IOptionalNamedTag<Block> NETHER_DRAGON_HABITAT_BLOCKS = BlockTags.createOptional(DragonMountsLegacy.id("nether_dragon_habitat_blocks"));

    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.Builder<T> builder)
    {
        return register(name, ForgeRegistries.ENTITIES, () -> builder.build(DragonMountsLegacy.MOD_ID + ":" + name));
    }

    private static RegistryObject<SoundEvent> sound(String name)
    {
        return register(name, ForgeRegistries.SOUND_EVENTS, () -> new SoundEvent(DragonMountsLegacy.id(name)));
    }

    @SuppressWarnings("unchecked")
    private static <T extends IForgeRegistryEntry<T>, I extends T> RegistryObject<I> register(String name, IForgeRegistry<T> forType, Supplier<? extends I> sup)
    {
        var registry = (DeferredRegister<T>) REGISTRIES.computeIfAbsent(forType, t -> DeferredRegister.create(t, DragonMountsLegacy.MOD_ID));
        return registry.register(name, sup);
    }
}
