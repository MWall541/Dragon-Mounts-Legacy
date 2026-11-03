package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Item model tint source resolving the primary/secondary colors of the dragon breed
 * stored on the spawn egg item stack. Layer 0 is the primary color, layer 1 the secondary.
 */
public record DragonBreedTintSource(int layer) implements ItemTintSource
{
    public static final MapCodec<DragonBreedTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("layer", 0).forGetter(DragonBreedTintSource::layer)
    ).apply(i, DragonBreedTintSource::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner)
    {
        return ARGB.opaque(DragonSpawnEgg.getColor(stack, layer));
    }

    @Override
    public MapCodec<DragonBreedTintSource> type()
    {
        return MAP_CODEC;
    }
}
