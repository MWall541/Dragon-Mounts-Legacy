package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DMLRegistry.BLACK_FIRE_BREATH.get(), BlackFireBreathRenderer::new);
        event.registerEntityRenderer(DMLRegistry.BLUE_FIRE_BREATH.get(), BlueFireBreathRenderer::new);
        event.registerEntityRenderer(DMLRegistry.STORM_BREATH.get(), StormBreathRenderer::new);
        event.registerEntityRenderer(DMLRegistry.ICE_BREATH.get(), IceBreathRenderer::new);
        event.registerEntityRenderer(DMLRegistry.SCULK_BREATH.get(), SculkBreathRenderer::new);
    }
}