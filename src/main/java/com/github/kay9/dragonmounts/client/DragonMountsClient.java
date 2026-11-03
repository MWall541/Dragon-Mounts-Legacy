package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.data.model.DragonModelPropertiesListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.server.packs.PackType;

public class DragonMountsClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        // Register entity renderer and model layer
        EntityRendererRegistry.register(DMLRegistry.DRAGON, DragonRenderer::new);
        ModelLayerRegistry.registerModelLayer(DragonRenderer.MODEL_LOCATION, () -> DragonModel.createBodyLayer(DragonModel.Properties.STANDARD));

        // Register key mappings
        KeyMappingHelper.registerKeyMapping(KeyMappings.FLIGHT_DESCENT_KEY);
        KeyMappingHelper.registerKeyMapping(KeyMappings.CAMERA_CONTROLS);

        // Register client tick events
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            MountControlsMessenger.tick();
            KeyMappings.tick();
        });

        // Tint source used by the spawn egg item model to color it by breed
        ItemTintSources.ID_MAPPER.put(DragonMountsLegacy.id("dragon_breed"), DragonBreedTintSource.MAP_CODEC);

        // Select property used by the egg item model to pick the breed-specific model
        SelectItemModelProperties.ID_MAPPER.put(DragonMountsLegacy.id("dragon_breed"), DragonBreedProperty.TYPE);

        // Per-breed egg block models (swapped at render time based on the block entity)
        DragonEggModelLoader.register();

        // Register reload listener for dragon model properties
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(DragonModelPropertiesListener.INSTANCE);
    }
}
