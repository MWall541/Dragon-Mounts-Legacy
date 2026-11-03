package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.abilities.*;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import com.github.kay9.dragonmounts.dragon.egg.habitats.*;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import static com.github.kay9.dragonmounts.DragonMountsLegacy.id;

public class DMLRegistry
{
    // ========================
    //         Blocks
    // ========================

    public static final Block EGG_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            id("dragon_egg"),
            new HatchableEggBlock()
    );

    // ========================
    //       Sound Events
    // ========================

    public static final SoundEvent DRAGON_AMBIENT_SOUND = registerSound("entity.dragon.ambient");
    public static final SoundEvent DRAGON_STEP_SOUND = registerSound("entity.dragon.step");
    public static final SoundEvent DRAGON_DEATH_SOUND = registerSound("entity.dragon.death");
    public static final SoundEvent GHOST_DRAGON_AMBIENT = registerSound("entity.dragon.ambient.ghost");

    // ========================
    //       Entity Types
    // ========================

    // DRAGON must be registered before SPAWN_EGG so SpawnEggItem constructor can reference it.
    public static final EntityType<TameableDragon> DRAGON;

    static
    {
        var dragonId = id("dragon");
        DRAGON = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                dragonId,
                FabricEntityType.Builder.createMob(TameableDragon::new, MobCategory.CREATURE,
                                mob -> mob.defaultAttributes(TameableDragon::createAttributes))
                        .sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT)
                        .eyeHeight(3.375f)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, dragonId))
        );
    }

    // ========================
    //         Items
    // ========================

    public static final Item EGG_BLOCK_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            id("dragon_egg"),
            new HatchableEggBlock.Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id("dragon_egg"))))
    );

    public static final Item SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            id("spawn_egg"),
            new DragonSpawnEgg(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id("spawn_egg"))))
    );

    // ========================
    //     Block Entity Types
    // ========================

    public static final BlockEntityType<HatchableEggBlockEntity> EGG_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            id("dragon_egg"),
            new BlockEntityType<>(HatchableEggBlockEntity::new, java.util.Set.of(EGG_BLOCK))
    );

    // ========================
    //    Data Component Types
    // ========================

    public static final DataComponentType<Holder<DragonBreed>> DRAGON_BREED_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            id("dragon_breed"),
            DataComponentType.<Holder<DragonBreed>>builder()
                    .persistent(DragonBreed.CODEC)
                    .networkSynchronized(DragonBreed.STREAM_CODEC)
                    .build()
    );

    // ========================
    //   Loot Item Conditions
    // ========================

    public static final MapCodec<? extends LootItemCondition> RANDOM_CHANCE_CONFIG_CONDITION = Registry.register(
            BuiltInRegistries.LOOT_CONDITION_TYPE,
            id("random_chance_by_config"),
            RandomChanceByConfig.CODEC
    );

    // ========================
    //   Custom Registries
    // ========================

    // Ability and Habitat registries are built in DragonMountsLegacy.onInitialize()
    // and their contents are registered there as well.

    // We store references here for access by other classes.
    public static Registry<MapCodec<? extends Ability.Factory<? extends Ability>>> ABILITY_REGISTRY;
    public static Registry<MapCodec<? extends Habitat>> HABITAT_REGISTRY;

    // ========================
    //       Helpers
    // ========================

    private static SoundEvent registerSound(String name)
    {
        var rl = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, rl, SoundEvent.createVariableRangeEvent(rl));
    }

    /** Called from DragonMountsLegacy to force class loading and static init. */
    static void init()
    {
        // All static fields are initialized when this class is loaded.
    }
}
