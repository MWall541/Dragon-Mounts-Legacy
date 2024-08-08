package com.github.kay9.dragonmounts.data.model;

import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.client.DragonRenderer;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.HashMap;
import java.util.Map;

public class DragonModelPropertiesListener extends SimpleJsonResourceReloadListener
{
    public static final DragonModelPropertiesListener INSTANCE = new DragonModelPropertiesListener();

    private static final String FOLDER = "models/entity/dragon/breed/properties";

    private final Map<ResourceLocation, ModelLayerLocation> definitions = new HashMap<>(3);

    public DragonModelPropertiesListener()
    {
        super(new GsonBuilder().create(), FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        definitions.clear();

        for (var entry : map.entrySet())
        {
            var breedId = entry.getKey();
            var properties = DragonModel.Properties.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(JsonParseException::new);
            var modelLoc = new ModelLayerLocation(DragonRenderer.MODEL_LOCATION.getModel(), breedId.toString());
            ForgeHooksClient.registerLayerDefinition(modelLoc, () -> DragonModel.createBodyLayer(properties));
            definitions.put(entry.getKey(), modelLoc);
        }
    }

    /**
     * Gets and clears this listener's model definitions.
     */
    public Map<ResourceLocation, ModelLayerLocation> pollDefinitions()
    {
        var map = Map.copyOf(definitions);
        definitions.clear();
        return map;
    }
}
