package wolfshotz.dml.util.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.block.DragonEggBlock;

public class BlockModelGen extends BlockStateProvider
{
    public BlockModelGen(DataGenerator gen, ExistingFileHelper exFileHelper)
    {
        super(gen, DragonMountsLegacy.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        getVariantBuilder(DragonEggBlock.DRAGON_EGG)
                .forAllStates(blockState ->
                {
                    String name = blockState.get(DragonEggBlock.BREED).getName();
                    BlockModelBuilder model = models().withExistingParent(String.format("block/%s_egg", name), "dragon_egg");
                    if (!name.equals("ender")) model.texture("all", String.format("block/%s_egg", name));
                    return ConfiguredModel.builder().modelFile(model).build();
                });
    }
}
