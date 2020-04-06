package wolfshotz.dml.util.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import wolfshotz.dml.DragonMountsLegacy;

public class BlockModels extends BlockStateProvider
{
    public BlockModels(DataGenerator gen, ExistingFileHelper exFileHelper)
    {
        super(gen, DragonMountsLegacy.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
//        getVariantBuilder(DragonMountsLegacy.DRAGON_EGG)
//                .forAllStates(state -> {
//                    DragonEggBlock.EggBreedTypes breed = state.get(DragonEggBlock.BREED);
//                    return ConfiguredModel.builder().modelFile(models().withExistingParent("dragon_egg", "dragon_egg").texture(breed.getName(), DragonMountsLegacy.rl("textures/block/")))
//                })

        simpleBlock(DragonMountsLegacy.DRAGON_EGG, models().withExistingParent("dragon_egg", "dragon_egg").texture("all", "block/fire_egg"));
    }
}
