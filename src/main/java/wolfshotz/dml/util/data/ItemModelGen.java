package wolfshotz.dml.util.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.DragonSpawnEggItem;

public class ItemModelGen extends ItemModelProvider
{
    public ItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, DragonMountsLegacy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        DragonSpawnEggItem.getEggs()
                .forEach(i -> getBuilder(i.getRegistryName().getPath()).parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg")));
    }

    @Override
    public String getName()
    {
        return "Dragon Mounts Item Models";
    }
}
