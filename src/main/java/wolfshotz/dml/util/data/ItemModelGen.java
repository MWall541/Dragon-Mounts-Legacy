package wolfshotz.dml.util.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.item.DragonEggBlockItem;
import wolfshotz.dml.item.DragonSpawnEggItem;

public class ItemModelGen extends ItemModelProvider
{
    public ItemModelGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, DragonMountsLegacy.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        DragonMountsLegacy.ITEMS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(DragonSpawnEggItem.class::isInstance)
                .map(DragonSpawnEggItem.class::cast)
                .forEach(i -> getBuilder(i.getRegistryName().getPath()).parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg")));

        DragonMountsLegacy.ITEMS
                .getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(DragonEggBlockItem.class::isInstance)
                .forEach(i ->
                {
                    String path = "dragonmounts:block/" + i.getRegistryName().getPath().replace("_dragon", "");
                    getBuilder(i.getRegistryName().getPath()).parent(new ModelFile.UncheckedModelFile(path));
                });
    }

    @Override
    public String getName() { return "Dragon Mounts Item Models"; }
}
