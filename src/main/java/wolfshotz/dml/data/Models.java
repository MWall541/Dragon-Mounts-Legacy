package wolfshotz.dml.data;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;
import wolfshotz.dml.DMLRegistry;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.misc.LazySpawnEggItem;

class Models
{
    static class Items extends ItemModelProvider
    {
        public Items(DataGenerator generator, ExistingFileHelper existingFileHelper)
        {
            super(generator, DragonMountsLegacy.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels()
        {
            DMLRegistry.ITEMS.getEntries().stream().map(RegistryObject::get).forEach(item ->
            {
                String path = item.getRegistryName().getPath();
                String parent = item instanceof LazySpawnEggItem? "item/template_spawn_egg" : "dragonmounts:block/" + path;
                getBuilder(path).parent(new ModelFile.UncheckedModelFile(new ResourceLocation(parent)));
            });
        }

        @Override
        public String getName() { return "Dragon Mounts Item Models"; }
    }

    static class Blocks extends BlockStateProvider
    {
        public Blocks(DataGenerator gen, ExistingFileHelper exFileHelper)
        {
            super(gen, DragonMountsLegacy.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels()
        {
            for (RegistryObject<Block> blockRegistryObject : DMLRegistry.BLOCKS.getEntries())
            {
                Block block = blockRegistryObject.get();
                String path = block.getRegistryName().getPath();
                BlockModelBuilder builder = models().withExistingParent(path, "dragon_egg");
                if (block != DMLRegistry.ENDER_EGG_BLOCK.get())
                    builder.texture("all", "block/" + path).texture("particle", "block/" + path);
                simpleBlock(block, ConfiguredModel.builder().modelFile(builder).build());
            }
        }
    }
}
