package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.data.BreedManager;
import com.github.kay9.dragonmounts.dragon.DMLEggBlock;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonEgg;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class DragonEggLootMod extends LootModifier
{
    public static final Codec<DragonEggLootMod> CODEC = RecordCodecBuilder.create(i ->
            codecStart(i).and(BreedManager.CODEC.fieldOf("breed").forGetter(m -> m.breed)).apply(i, DragonEggLootMod::new));

    private final DragonBreed breed;

    public DragonEggLootMod(LootItemCondition[] conditions, DragonBreed breed)
    {
        super(conditions);
        this.breed = breed;
    }

    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        if (DMLConfig.useLootTables()) generatedLoot.add(DMLEggBlock.Item.create(breed, DragonEgg.DEFAULT_HATCH_TIME));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec()
    {
        return CODEC;
    }
}