package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class DragonEggLootMod extends LootModifier
{
    public static final Codec<DragonEggLootMod> CODEC = RecordCodecBuilder.create(i -> codecStart(i)
            .and(ResourceLocation.CODEC.fieldOf("breed").forGetter(m -> m.id))
            .apply(i, DragonEggLootMod::new));

    private final ResourceLocation id;

    public DragonEggLootMod(LootItemCondition[] conditions, ResourceLocation breed)
    {
        super(conditions);
        this.id = breed;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        if (DMLConfig.useLootTables())
        {
            var reg = context.getLevel().registryAccess();
            var breed = BreedRegistry.registry(reg).get(id);
            if (breed != null)
                generatedLoot.add(HatchableEggBlock.Item.create(breed, reg));
            else
                DragonMountsLegacy.LOG.error("Attempted to add a dragon egg to loot with unknown breed id: \"{}\"", id);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec()
    {
        return CODEC;
    }
}
