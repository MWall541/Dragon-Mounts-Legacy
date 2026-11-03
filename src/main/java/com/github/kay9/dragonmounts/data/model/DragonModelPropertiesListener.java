package com.github.kay9.dragonmounts.data.model;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.client.DragonRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class DragonModelPropertiesListener extends SimpleJsonResourceReloadListener<DragonModel.Properties> implements IdentifiableResourceReloadListener
{
    public static final DragonModelPropertiesListener INSTANCE = new DragonModelPropertiesListener();

    private static final String FOLDER = "models/entity/dragon/breed/properties";

    private final Map<Identifier, ModelLayerLocation> definitions = new HashMap<>(3);

    public DragonModelPropertiesListener()
    {
        super(DragonModel.Properties.CODEC, FileToIdConverter.json(FOLDER));
    }

    @Override
    public Identifier getFabricId()
    {
        return DragonMountsLegacy.id("dragon_model_properties");
    }

    @Override
    protected void apply(Map<Identifier, DragonModel.Properties> map, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        definitions.clear();

        for (var entry : map.entrySet())
        {
            var breedId = entry.getKey();
            var properties = entry.getValue();
            var modelLoc = new ModelLayerLocation(DragonRenderer.MODEL_LOCATION.model(), breedId.toString());
            // In Fabric, we register model layers via ModelLayerRegistry
            ModelLayerRegistry.registerModelLayer(modelLoc, () -> DragonModel.createBodyLayer(properties));
            definitions.put(entry.getKey(), modelLoc);
        }
    }

    /**
     * Gets and clears this listener's model definitions.
     */
    public Map<Identifier, ModelLayerLocation> pollDefinitions()
    {
        var map = Map.copyOf(definitions);
        definitions.clear();
        return map;
    }
}
