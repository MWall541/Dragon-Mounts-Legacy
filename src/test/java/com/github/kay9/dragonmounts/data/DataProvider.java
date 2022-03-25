package com.github.kay9.dragonmounts.data;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataProvider
{
    @SubscribeEvent
    public static void gather(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        if (event.includeServer())
        {
            gen.addProvider(new BlockTagProvider(gen, fileHelper));
            gen.addProvider(new BiomeTagProvider(gen, fileHelper));
            gen.addProvider(new LootModifierProvider(gen));
            gen.addProvider(new DragonBreedProvider(gen));
        }
    }
}
