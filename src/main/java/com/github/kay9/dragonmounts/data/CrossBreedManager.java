package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CrossBreedManager extends SimpleJsonResourceReloadListener
{
    public static final CrossBreedManager INSTANCE = new CrossBreedManager();
    private static final String PATH = "dragonmounts/cross_breeds"; // data/[pack_name]/dragonmounts/cross_breeds/whatever.json

    private final Map<Couple, ResourceKey<DragonBreed>> crosses = new HashMap<>();

    private CrossBreedManager()
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
                    .getOrThrow(false, Util.prefix("Unable to parse Cross Breed result for: " + id, DragonMountsLegacy.LOG::error));
            crosses.put(new Couple(cross.parent1(), cross.parent2()), cross.child());
        }
    }

    @Nullable
    public DragonBreed getCrossBreed(DragonBreed parent, DragonBreed mate, RegistryAccess ra)
    {
        var reg = BreedRegistry.registry(ra);
        var parentKey = reg.getResourceKey(parent).orElseThrow();
        var mateKey = reg.getResourceKey(mate).orElseThrow();
        var result = crosses.get(new Couple(parentKey, mateKey));

        return result == null? null : reg.get(result);
    }

    public record CrossBreedResult(ResourceKey<DragonBreed> parent1, ResourceKey<DragonBreed> parent2, ResourceKey<DragonBreed> child)
    {
        public static final Codec<CrossBreedResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(BreedRegistry.REGISTRY_KEY).fieldOf("parent1").forGetter(CrossBreedResult::parent1),
            ResourceKey.codec(BreedRegistry.REGISTRY_KEY).fieldOf("parent2").forGetter(CrossBreedResult::parent2),
            ResourceKey.codec(BreedRegistry.REGISTRY_KEY).fieldOf("child").forGetter(CrossBreedResult::child)
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
