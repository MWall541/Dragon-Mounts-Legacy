package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

public class DragonEggLootMod extends LootModifier
{
    public static final MapCodec<DragonEggLootMod> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i)
            .and(ResourceKey.codec(DragonBreed.REGISTRY_KEY).fieldOf("egg_breed").forGetter(m -> m.id))
            .and(Codec.BOOL.optionalFieldOf("replace_first", false).forGetter(m -> m.replaceFirst))
            .apply(i, DragonEggLootMod::new));

    public record Target(ResourceKey<DragonBreed> forBreed, ResourceKey<LootTable> target, double chance) {}
    public static Target[] BUILT_IN_CHANCES = new Target[]{
            new Target(AETHER, BuiltInLootTables.SIMPLE_DUNGEON, 0.15),
            new Target(FIRE, BuiltInLootTables.DESERT_PYRAMID, 0.075),
            new Target(FOREST, BuiltInLootTables.JUNGLE_TEMPLE, 0.3),
            new Target(GHOST, BuiltInLootTables.WOODLAND_MANSION, 0.2),
            new Target(GHOST, BuiltInLootTables.ABANDONED_MINESHAFT, 0.095),
            new Target(ICE, BuiltInLootTables.IGLOO_CHEST, 0.2),
            new Target(NETHER, BuiltInLootTables.BASTION_TREASURE, 0.35),
            new Target(WATER, BuiltInLootTables.BURIED_TREASURE, 0.175)
    };

    private final ResourceKey<DragonBreed> id;
    private final boolean replaceFirst;

    public DragonEggLootMod(LootItemCondition[] conditions, ResourceKey<DragonBreed> breed, boolean replaceFirst)
    {
        super(conditions);
        this.id = breed;
        this.replaceFirst = replaceFirst;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        var egg = HatchableEggBlock.Item.create(id);

        if (replaceFirst) generatedLoot.set(0, egg);
        else generatedLoot.add(egg);

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec()
    {
        return CODEC;
    }
}
