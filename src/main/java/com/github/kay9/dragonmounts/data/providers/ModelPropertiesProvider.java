package com.github.kay9.dragonmounts.data.providers;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

import java.io.IOException;

public class ModelPropertiesProvider implements DataProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final DataGenerator gen;

    public ModelPropertiesProvider(DataGenerator gen)
    {
        this.gen = gen;
    }

    @Override
    public void run(HashCache pCache) throws IOException
    {
        save(pCache, "fire", new DragonModel.Properties(false, false, false));
        save(pCache, "ghost", new DragonModel.Properties(true, false, true));
        save(pCache, "water", new DragonModel.Properties(true, true, false));
    }

    private void save(HashCache cache, String id, DragonModel.Properties instance) throws IOException
    {
        var jsonObj = DragonModel.Properties.CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow(false, err -> {});
        var path = gen.getOutputFolder().resolve("assets/" + DragonMountsLegacy.MOD_ID + "/models/entity/dragon/breed/properties/" + id + ".json");
        DataProvider.save(GSON, cache, jsonObj, path);
    }

    @Override
    public String getName()
    {
        return DragonMountsLegacy.MOD_ID + ": Dragon Model Properties";
    }
}
