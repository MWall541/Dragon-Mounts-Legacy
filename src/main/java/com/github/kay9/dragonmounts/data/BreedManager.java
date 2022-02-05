package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BreedManager extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON = new GsonBuilder().create();
    private static final Marker MARKER = MarkerManager.getMarker("DragonBreeds");
    private static Map<ResourceLocation, DragonBreed> registry = Collections.emptyMap();

    public static final BreedManager INSTANCE = new BreedManager();

    protected BreedManager()
    {
        super(GSON, "dragon_breeds");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        ImmutableMap.Builder<ResourceLocation, DragonBreed> builder = ImmutableMap.builder();

        for (var entry : elements.entrySet())
        {
            DragonBreed.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(m -> DragonMountsLegacy.LOG.warn(MARKER, "Failed to load '{}' dragon breed: {}", entry.getKey(), m))
                    .ifPresent(b -> builder.put(b.id(), b));
        }

        // Ensure one entry exists in the registry, for safety purposes.
        builder.put(DragonBreed.FIRE.id(), DragonBreed.FIRE);
        registry = builder.build();

        DragonMountsLegacy.LOG.info(MARKER, "Loaded {} Dragon Breeds", registry.size());
    }

    /**
     * Ensure at least ONE dragon is present in the registry, and builds it.
     * The default fallback will be the DML fire dragon default.
     * If someone really broke something or is just being a douche, we have our hardcoded version ready.
     */
//    private static ImmutableMap<ResourceLocation, DragonBreed> build(Map<ResourceLocation, DragonBreed> map)
//    {
//        if (map.isEmpty()) // not a single entry could be parsed. Use the built-in fallback...
//        {
//            DragonMountsLegacy.LOG.error(MARKER, "No DragonBreed's have been found. Using FIRE as a single fallback.");
//
//            var fire = DragonBreed.INTERNAL_FIRE.get();
//            fallback = fire;
//            return ImmutableMap.of(fire.id(), fire);
//        }
//        else if ((fallback = getNullable(DragonMountsLegacy.id("fire"))) == null) // attempt fire for fallback
//        {
//            fallback = map.values().iterator().next(); // fire doesn't seem to be present, so use the first available entry.
//        }
//
//        return ImmutableMap.copyOf(map);
//    }
    // todo: issues arose with syncing to client... we will continue to use a built-in entry for now.

    @Nullable
    public static DragonBreed getNullable(ResourceLocation breed)
    {
        return registry.get(breed);
    }

    public static DragonBreed get(ResourceLocation breed)
    {
        var result = getNullable(breed);
        return result != null? result : DragonBreed.FIRE; // fallback to fire if a breed ends up missing (due to removal of a type, or other extravagent error I can't think of)
    }

    public static DragonBreed read(String breed)
    {
        return get(ResourceLocation.tryParse(breed));
    }

    public static Collection<DragonBreed> getBreeds()
    {
        return registry.values();
    }
}
