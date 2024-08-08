package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.function.Consumer;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties()
                .component(DMLRegistry.SPAWN_EGG_COLORS_COMPONENT.get(), new Colors(0xf, 0xf)));
    }

    public record Colors(int primaryColor, int secondaryColor) {}

    public static ItemStack create(Holder.Reference<DragonBreed> breed)
    {
        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG.get());

        // add breed data
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.getRegisteredName());
        CustomData.set(DataComponents.ENTITY_DATA, stack, entityTag);

        return stack;
    }

    public static void populateTab(Consumer<ItemStack> registrar)
    {
        if (Minecraft.getInstance().level != null)
        {
            var reg = Minecraft.getInstance().level.registryAccess();
            DragonBreed.registry(reg).holders().forEach(breed -> registrar.accept(create(breed)));
        }
    }

    @SuppressWarnings("ConstantConditions") // ensured by item properties.
    public static int getColor(ItemStack stack, int tintIndex)
    {
        Colors colors = stack.get(DMLRegistry.SPAWN_EGG_COLORS_COMPONENT.get());
        return tintIndex == 0? colors.primaryColor() : colors.secondaryColor();
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack)
    {
        super.verifyComponentsAfterLoad(stack);

        // ensure a breed exists for this egg. if not, assign a random one.
        // possible cause is through commands, or other unnatural means.
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        CompoundTag tag = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag();
        Holder.Reference<DragonBreed> breed = DragonBreed.parse(tag.getString(TameableDragon.NBT_BREED), server.registryAccess());
        if (server != null && breed == null)
        {
            breed = DragonBreed.getRandom(server.registryAccess(), server.overworld().getRandom());
            tag.putString(TameableDragon.NBT_BREED, breed.getRegisteredName());
            stack.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
        }

        // adjust name to breed
        ResourceLocation breedId = breed.key().location();
        stack.set(DataComponents.ITEM_NAME, Component.translatable(String.join(".", stack.getDescriptionId(), breedId.getNamespace(), breedId.getPath())));

        // visual colors of spawn egg
        stack.set(DMLRegistry.SPAWN_EGG_COLORS_COMPONENT.get(), new Colors(breed.get().primaryColor(), breed.get().secondaryColor()));
    }

    @Override
    public Optional<Mob> spawnOffspringFromSpawnEgg(Player pPlayer, Mob pMob, EntityType<? extends Mob> pEntityType, ServerLevel server, Vec3 pPos, ItemStack stack)
    {
        // don't spawn an offspring if the breed doesn't match!

        CompoundTag tag = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag();
        Holder.Reference<DragonBreed> breed = DragonBreed.parse(tag.getString(TameableDragon.NBT_BREED), server.registryAccess());
        if (breed == null || !breed.is(((TameableDragon) pMob).getBreedHolder()))
            return Optional.empty();

        return super.spawnOffspringFromSpawnEgg(pPlayer, pMob, pEntityType, server, pPos, stack);
    }
}
