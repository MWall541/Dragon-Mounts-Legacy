package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataProvider
{
    @SubscribeEvent
    public static void gather(GatherDataEvent event)
    {
        var gen = event.getGenerator();
        var fileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new BlockTagProvider(gen, DragonMountsLegacy.MOD_ID, fileHelper));
        gen.addProvider(event.includeServer(), new LootModifierProvider(gen, DragonMountsLegacy.MOD_ID));
        gen.addProvider(event.includeServer(), new DragonBreedProvider(gen));

        gen.addProvider(event.includeClient(), new ModelPropertiesProvider(gen));
    }
}
