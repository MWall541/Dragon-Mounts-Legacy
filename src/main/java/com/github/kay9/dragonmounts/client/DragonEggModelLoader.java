package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

/**
 * Loads the breed-specific dragon egg block models ({@code models/block/dragon_eggs/<breed>_dragon_egg.json})
 * and swaps the egg's block-state model at render time based on the breed stored in the block entity.
 * <p>
 * Replaces the old Forge-era custom {@code BakedModel} loader; in 26.x this goes through
 * Fabric's model loading API since block models can no longer resolve block entity data themselves.
 */
public class DragonEggModelLoader implements PreparableModelLoadingPlugin<Map<String, Identifier>>
{
    private static final DragonEggModelLoader INSTANCE = new DragonEggModelLoader();

    /** breed name ("namespace:breed") -> model key; rebuilt on every resource reload. */
    private static volatile Map<String, ExtraModelKey<BlockStateModel>> MODEL_KEYS = Map.of();

    public static void register()
    {
        PreparableModelLoadingPlugin.register(DragonEggModelLoader::loadEggModels, INSTANCE);
    }

    /**
     * Scans resource packs for breed egg models.
     * {@code assets/<ns>/models/block/dragon_eggs/<breed>_dragon_egg.json} maps to breed {@code <ns>:<breed>}.
     */
    private static CompletableFuture<Map<String, Identifier>> loadEggModels(PreparableReloadListener.SharedState state, Executor executor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            var models = new HashMap<String, Identifier>();
            var dir = "models/block/dragon_eggs";
            var prefixLength = "models/".length();
            var suffixLength = ".json".length();
            for (var rl : state.resourceManager().listResources(dir, f -> f.getPath().endsWith("_dragon_egg.json")).keySet())
            {
                var path = rl.getPath();
                path = path.substring(prefixLength, path.length() - suffixLength); // block/dragon_eggs/<breed>_dragon_egg
                var breed = String.format("%s:%s", rl.getNamespace(), path.substring("block/dragon_eggs/".length(), path.length() - "_dragon_egg".length()));
                models.put(breed, Identifier.tryBuild(rl.getNamespace(), path));
            }
            return models;
        }, executor);
    }

    @Override
    public void initialize(Map<String, Identifier> data, ModelLoadingPlugin.Context ctx)
    {
        var keys = new HashMap<String, ExtraModelKey<BlockStateModel>>(data.size());
        for (var entry : data.entrySet())
        {
            ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(entry.getKey()::toString);
            ctx.addModel(key, SimpleUnbakedExtraModel.blockStateModel(entry.getValue()));
            keys.put(entry.getKey(), key);
        }
        MODEL_KEYS = Map.copyOf(keys);

        ctx.modifyBlockModelAfterBake().register((model, bakeCtx) ->
                bakeCtx.state().getBlock() instanceof HatchableEggBlock? new BreedEggModel(model) : model);
    }

    @Nullable
    private static BlockStateModel modelFor(BlockAndTintGetter view, BlockPos pos)
    {
        if (!(view.getBlockEntity(pos) instanceof HatchableEggBlockEntity egg) || !egg.hasBreed()) return null;

        var key = MODEL_KEYS.get(egg.getBreedHolder().getRegisteredName());
        if (key == null) return null;

        return ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(key);
    }

    private static class BreedEggModel extends WrapperBlockStateModel
    {
        BreedEggModel(BlockStateModel wrapped)
        {
            super(wrapped);
        }

        @Override
        public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest)
        {
            var model = modelFor(view, pos);
            if (model != null) model.emitQuads(emitter, view, pos, state, random, cullTest);
            else super.emitQuads(emitter, view, pos, state, random, cullTest);
        }

        @Override
        public Material.Baked particleMaterial(BlockAndTintGetter view, BlockPos pos, BlockState state)
        {
            var model = modelFor(view, pos);
            return model != null? model.particleMaterial(view, pos, state) : super.particleMaterial(view, pos, state);
        }

        @Override
        public Object createGeometryKey(BlockAndTintGetter view, BlockPos pos, BlockState state, RandomSource random)
        {
            var model = modelFor(view, pos);
            return model != null? model.createGeometryKey(view, pos, state, random) : super.createGeometryKey(view, pos, state, random);
        }
    }
}
