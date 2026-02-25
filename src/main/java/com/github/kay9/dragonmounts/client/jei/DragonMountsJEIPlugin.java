package com.github.kay9.dragonmounts.client.jei;

import com.github.kay9.dragonmounts.DMLRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class DragonMountsJEIPlugin implements IModPlugin {

    private static final ResourceLocation UID =
            new ResourceLocation("dragonmounts", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {

        registration.registerSubtypeInterpreter(
                DMLRegistry.SPAWN_EGG.get(),
                new DragonEggSubtypeInterpreter()
        );

        registration.registerSubtypeInterpreter(
                DMLRegistry.EGG_BLOCK_ITEM.get(),
                new DragonEggBlockSubtypeInterpreter()
        );
    }
}