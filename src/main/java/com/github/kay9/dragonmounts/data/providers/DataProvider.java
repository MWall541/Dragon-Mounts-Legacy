package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataProvider
{
    @SubscribeEvent
    public static void gather(GatherDataEvent evt)
    {
        var gen = evt.getGenerator();
        var output = evt.getGenerator().getPackOutput();
        var lookup = evt.getLookupProvider();
        var fileHelper = evt.getExistingFileHelper();

        gen.addProvider(evt.includeServer(), new BlockTagProvider(output, lookup, DragonMountsLegacy.MOD_ID, fileHelper));
        gen.addProvider(evt.includeServer(), new LootModifierProvider(output, DragonMountsLegacy.MOD_ID, evt.getLookupProvider()));
        gen.addProvider(evt.includeServer(), new DragonBreedProvider(output, lookup));

        gen.addProvider(evt.includeClient(), new ModelPropertiesProvider(gen));
    }
}
