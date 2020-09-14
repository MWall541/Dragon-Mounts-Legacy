package wolfshotz.dml.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class DataHandler
{
    public static void gather(GatherDataEvent evt)
    {
        DataGenerator gen = evt.getGenerator();
        if (evt.includeClient())
        {
            ExistingFileHelper helper = evt.getExistingFileHelper();
            gen.addProvider(new Models.Items(gen, helper));
            gen.addProvider(new Models.Blocks(gen, helper));
        }
        if (evt.includeServer())
        {
            gen.addProvider(new Loot(gen));
        }
    }
}
