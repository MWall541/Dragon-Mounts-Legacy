package wolfshotz.dml.util.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import wolfshotz.dml.DragonMountsLegacy;

@EventBusSubscriber(modid = DragonMountsLegacy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void runDataGen(GatherDataEvent evt)
    {
        DataGenerator gen = evt.getGenerator();

        if (evt.includeClient())
        {
            gen.addProvider(new BlockModelGen(gen, evt.getExistingFileHelper()));
            gen.addProvider(new ItemModelGen(gen, evt.getExistingFileHelper()));
        }
    }
}
