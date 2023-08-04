package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.client.*;
import com.github.kay9.dragonmounts.data.model.DragonModelPropertiesListener;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.breed.BreedRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    // ========================
    //          Events
    // ========================

    //    private static void setupEvents()
    //    {
    //        var bus = FMLJavaModLoadingContext.get().getModEventBus();
    //
    //        MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::attemptVanillaEggReplacement);
    //
    //        bus.addListener((EntityAttributeCreationEvent e) -> e.put(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes().build()));
    //        bus.addListener(BreedRegistry::hookRegistry);
    //
    //        if (FMLLoader.getDist() == Dist.CLIENT) // Client Events
    //        {
    //            MinecraftForge.EVENT_BUS.addListener(DragonMountsLegacy::cameraAngles);
    //            MinecraftForge.EVENT_BUS.addListener(Keybinds::handleKeyPress);
    //            MinecraftForge.EVENT_BUS.addListener(MountControlsMessenger::tick);
    //
    //            bus.addListener(DragonMountsLegacy::registerEggModelLoader);
    //            bus.addListener(DragonMountsLegacy::addToCreativeTab);
    //            bus.addListener((RegisterColorHandlersEvent.Item e) -> e.getItemColors().register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get()));
    //            bus.addListener(DragonMountsLegacy::rendererRegistry);
    //            bus.addListener(Keybinds::registerKeybinds);
    //            bus.addListener((FMLConstructModEvent e) -> e.enqueueWork(() -> ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(DragonModelPropertiesListener.INSTANCE)));
    //        }
    //    }

    static boolean overrideVanillaDragonEgg(Level level, BlockPos pos, Player player)
    {
        if (DMLConfig.allowEggOverride() && level.getBlockState(pos).is(Blocks.DRAGON_EGG))
        {
            var end = BreedRegistry.registry(level.registryAccess()).getOptional(DragonMountsLegacy.id("end"));
            if (end.isPresent())
            {
                level.removeBlock(pos, false);
                if (level.isClientSide) player.swing(InteractionHand.MAIN_HAND);
                DMLEggBlock.startHatching(end.get(), level, pos);
                return true;
            }
        }
        return false;
    }

    static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> registrar)
    {
        registrar.accept(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes().build());
    }

    static void clientTick(boolean pre)
    {
        if (!pre) MountControlsMessenger.tick();
    }

    @SuppressWarnings("ConstantConditions") // player should never be null at time of calling
    static void modifyMountCameraAngles(Camera camera)
    {
        if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon)
        {
            var distance = 0;
            var vertical = 0;
            switch (Minecraft.getInstance().options.getCameraType())
            {
                case THIRD_PERSON_FRONT -> distance = 6;
                case THIRD_PERSON_BACK -> {
                    distance = 6;
                    vertical = 4;
                }
            }
            camera.move(-camera.getMaxZoom(distance), vertical, 0);
        }
    }

    static void onKeyPress(int key, int action, int modifiers)
    {
        Keybinds.handleKeyPress(key, action);
    }

    static void populateSearchTrees()
    {
        var tree = Minecraft.getInstance().getSearchTree(SearchRegistry.CREATIVE_NAMES);
        var oldContents = tree.search(DragonMountsLegacy.MOD_ID + ":");
        NonNullList<ItemStack> newContents = NonNullList.create();
        DMLRegistry.EGG_BLOCK_ITEM.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, newContents);
        DMLRegistry.SPAWN_EGG.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, newContents);
        newItems: // this is a sanity check if resources are reloaded
        for (var newItem : newContents)
        {
            for (var oldItem : oldContents)
                if (ItemStack.matches(newItem, oldItem)) continue newItems;
            tree.add(newItem);
        }
        tree.refresh();
    }

    static void registerRenderers()
    {
        EntityRenderers.register(DMLRegistry.DRAGON.get(), DragonRenderer::new);
        ForgeHooksClient.registerLayerDefinition(DragonRenderer.MODEL_LOCATION, () -> DragonModel.createBodyLayer(DragonModel.Properties.STANDARD));
    }

    private static void registerEggModelLoader(ModelEvent.RegisterGeometryLoaders evt)
    {
        evt.register("dragon_egg", DragonEggModel.Loader.INSTANCE);
    }

    static void registerItemColors(ItemColors colors)
    {
        colors.register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get());
    }

    @SuppressWarnings("ConstantConditions") // client instance is null on data gen
    static void registerReloadListenersEarly()
    {
        if (Minecraft.getInstance() != null)
        {
            ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(DragonModelPropertiesListener.INSTANCE); // Dragon Model Properties need to be reloaded before Entity Models are!
        }
    }
}