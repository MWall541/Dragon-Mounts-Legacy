package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A dynamic BakedModel which returns quads based on the given breed of the tile entity.
 */
public class DragonEggModel implements IUnbakedGeometry<DragonEggModel>
{
    private final ImmutableMap<String, BlockModel> models;

    public DragonEggModel(ImmutableMap<String, BlockModel> models)
    {
        this.models = models;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        var baked = ImmutableMap.<String, BakedModel>builder();
        for (var entry : models.entrySet())
        {
            var unbaked = entry.getValue();
            unbaked.resolveParents(baker::getModel);
            baked.put(entry.getKey(), unbaked.bake(baker, unbaked, spriteGetter, modelState, modelLocation, true));
        }
        return new Baked(baked.build(), overrides);
    }

    private record Data(String breedId)
    {
        private static final ModelProperty<Data> PROPERTY = new ModelProperty<>();
    }

    public static class Baked implements IDynamicBakedModel
    {
        private static final Supplier<BakedModel> FALLBACK = Suppliers.memoize(() -> Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.DRAGON_EGG.defaultBlockState()));

        private final ImmutableMap<String, BakedModel> models;
        private final ItemOverrides overrides;

        public Baked(ImmutableMap<String, BakedModel> models, ItemOverrides overrides)
        {
            this.models = models;
            this.overrides = new ItemModelResolver(this, overrides);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType)
        {
            var data = extraData.get(Data.PROPERTY);
            if (data != null)
                return models.get(data.breedId()).getQuads(state, side, rand, extraData, renderType);
            return FALLBACK.get().getQuads(state, side, rand, extraData, renderType);
        }

        @Override
        public boolean useAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean isGui3d()
        {
            return true;
        }

        @Override
        public boolean usesBlockLight()
        {
            return true;
        }

        @Override
        public boolean isCustomRenderer()
        {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon()
        {
            return FALLBACK.get().getParticleIcon();
        }

        @Override
        public TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
        {
            var data = modelData.get(Data.PROPERTY);
            if (data != null)
                return models.get(data.breedId()).getParticleIcon(modelData);

            return getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides()
        {
            return overrides;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform)
        {
            return FALLBACK.get().applyTransform(transformType, poseStack, applyLeftHandTransform);
        }

        @Override
        public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData)
        {
            if (level.getBlockEntity(pos) instanceof HatchableEggBlockEntity e && e.isModelReady())
                return modelData.derive()
                        .with(Data.PROPERTY, new Data(e.getBreed().id(Minecraft.getInstance().level.m_9598_()).toString()))
                        .build();

            return modelData;
        }
    }

    public static class ItemModelResolver extends ItemOverrides
    {
        private final Baked owner;
        private final ItemOverrides nested;

        public ItemModelResolver(Baked owner, ItemOverrides nested)
        {
            this.owner = owner;
            this.nested = nested;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int pSeed)
        {
            var override = nested.resolve(original, stack, level, entity, pSeed);
            if (override != original) return override;

            var tag = BlockItem.getBlockEntityData(stack);
            if (tag != null)
            {
                var model = owner.models.get(tag.getString(HatchableEggBlock.NBT_BREED));
                if (model != null) return model;
            }

            return original;
        }
    }

    public static class Loader implements IGeometryLoader<DragonEggModel>
    {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public DragonEggModel read(JsonObject jsonObject, JsonDeserializationContext deserializer) throws JsonParseException
        {
            var models = ImmutableMap.<String, BlockModel>builder();
            var dir = "models/block/dragon_eggs";
            var length = "models/".length();
            var suffixLength = ".json".length();
            for (var entry : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.getPath().endsWith(".json")).entrySet())
            {
                var rl = entry.getKey();
                var path = rl.getPath();
                path = path.substring(length, path.length() - suffixLength);
                var id = String.format("%s:%s", rl.getNamespace(), path.substring("block/dragon_eggs/".length(), path.length() - "_dragon_egg".length()));

                try (var reader = entry.getValue().openAsReader())
                {
                    models.put(id, BlockModel.fromStream(reader));
                }
                catch (IOException e)
                {
                    throw new JsonParseException(e);
                }
            }

            return new DragonEggModel(models.build());
        }
    }
}
