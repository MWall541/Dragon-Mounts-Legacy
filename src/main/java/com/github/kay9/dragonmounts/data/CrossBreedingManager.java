package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CrossBreedingManager extends SimpleJsonResourceReloadListener
{
    public static final CrossBreedingManager INSTANCE = new CrossBreedingManager();
    private static final String PATH = "dragonmounts/cross_breeding"; // data/[pack_name]/dragonmounts/cross_breeds/whatever.json

    private final Map<Couple, ResourceKey<DragonBreed>> crosses = new HashMap<>();

    private CrossBreedingManager()
    {
        super(new GsonBuilder().create(), PATH);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        crosses.clear();

        for (var entry : entries.entrySet())
        {
            var id = entry.getKey();
            var json = entry.getValue();
            var cross = CrossBreedResult.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(s -> new IllegalStateException("Unable to parse Cross Breeding result for: " + id + ", " + s));
            crosses.put(new Couple(cross.parent1(), cross.parent2()), cross.child());
        }
    }

    @Nullable
    public Holder.Reference<DragonBreed> getCrossBreed(Holder.Reference<DragonBreed> parent, Holder.Reference<DragonBreed> mate, HolderLookup.Provider ra)
    {
        ResourceKey<DragonBreed> result = crosses.get(new Couple(parent.key(), mate.key()));

        return result == null? null : DragonBreed.get(result, ra);
    }

    public record CrossBreedResult(ResourceKey<DragonBreed> parent1, ResourceKey<DragonBreed> parent2, ResourceKey<DragonBreed> child)
    {
        public static final Codec<CrossBreedResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(DragonBreed.REGISTRY_KEY).fieldOf("parent1").forGetter(CrossBreedResult::parent1),
            ResourceKey.codec(DragonBreed.REGISTRY_KEY).fieldOf("parent2").forGetter(CrossBreedResult::parent2),
            ResourceKey.codec(DragonBreed.REGISTRY_KEY).fieldOf("child").forGetter(CrossBreedResult::child)
        ).apply(instance, CrossBreedResult::new));
    }

    private record Couple(ResourceKey<DragonBreed> parent1, ResourceKey<DragonBreed> parent2)
    {
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Couple couple = (Couple) o;
            return (parent1 == couple.parent1 && parent2 == couple.parent2) ||
                    (parent1 == couple.parent2 && parent2 == couple.parent1);
        }

        @Override
        public int hashCode()
        {
            return parent1.hashCode() + parent2.hashCode();
        }
    }
}
