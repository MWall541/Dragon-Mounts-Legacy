package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.dragon.breed.DragonBreed;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.io.IOException;

public class ModelPropertiesProvider implements DataProvider
{
    private final DataGenerator gen;

    public ModelPropertiesProvider(DataGenerator gen)
    {
        this.gen = gen;
    }

    @Override
    public void run(CachedOutput pCache) throws IOException
    {
        save(pCache, DragonBreed.BuiltIn.FIRE.location().getPath(), new DragonModel.Properties(false, false, false));
        save(pCache, DragonBreed.BuiltIn.GHOST.location().getPath(), new DragonModel.Properties(true, false, true));
        save(pCache, DragonBreed.BuiltIn.WATER.location().getPath(), new DragonModel.Properties(true, true, false));
    }

    private void save(CachedOutput cache, String id, DragonModel.Properties instance) throws IOException
    {
        var jsonObj = DragonModel.Properties.CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow(false, err -> {});
        var path = gen.getOutputFolder().resolve("assets/" + DragonMountsLegacy.MOD_ID + "/models/entity/dragon/breed/properties/" + id + ".json");
        DataProvider.saveStable(cache, jsonObj, path);
    }

    @Override
    public String getName()
    {
        return DragonMountsLegacy.MOD_ID + ": Dragon Model Properties";
    }
}
