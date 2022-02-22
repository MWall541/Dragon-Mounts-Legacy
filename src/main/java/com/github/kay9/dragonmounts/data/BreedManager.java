package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.network.UpdateBreedsPacket;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

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
        update(r -> elements.forEach((key, value) ->
                DragonBreed.CODEC.parse(JsonOps.INSTANCE, value)
                        .resultOrPartial(m -> DragonMountsLegacy.LOG.warn(MARKER, "Failed to load '{}' dragon breed: {}", key, m))
                        .ifPresent(r)));
        DragonMountsLegacy.LOG.info(MARKER, "Loaded {} Dragon Breeds", registry.size());
    }

    public static void update(Consumer<Consumer<DragonBreed>> consumer)
    {
        ImmutableMap.Builder<ResourceLocation, DragonBreed> builder = ImmutableMap.builder();
        consumer.accept(d -> builder.put(d.id(), d));
        builder.put(DragonBreed.FIRE.id(), DragonBreed.FIRE); // Ensure one entry exists in the registry, for safety purposes.
        registry = builder.build();
    }

    public static void syncClientBreeds(OnDatapackSyncEvent event)
    {
        var player = event.getPlayer();
        var target = player == null? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);
        DragonMountsLegacy.NETWORK.send(target, new UpdateBreedsPacket(getBreeds()));
    }

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
