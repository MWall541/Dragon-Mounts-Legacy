package com.github.kay9.dragonmounts.dragon;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Consumer;

public class DragonSpawnEgg extends ForgeSpawnEggItem
{
    public DragonSpawnEgg()
    {
        super(DMLRegistry.DRAGON, 0, 0, new Item.Properties());
    }

    public static ItemStack create(Holder<DragonBreed> breed)
    {
        ItemStack stack = new ItemStack(DMLRegistry.SPAWN_EGG.get());
        setBreed(stack, breed);
        return stack;
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
        if (breed == null)
            breed = DragonBreed.getRandom(server.registryAccess(), server.overworld().getRandom());

        setBreed(stack, breed);
    }

    private static void setBreed(ItemStack stack, Holder<DragonBreed> breed)
    {
        // add breed data
        CompoundTag entityTag = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag();
        entityTag.putString(TameableDragon.NBT_BREED, breed.getRegisteredName());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityTag));

        // visual colors of spawn egg
        stack.set(DMLRegistry.DRAGON_BREED_COMPONENT.get(), breed);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        String breedId = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag().getString(TameableDragon.NBT_BREED);
        return Component.translatable(String.join(".", stack.getDescriptionId(), breedId.replace(':', '.')));
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
        Holder<DragonBreed> breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
        if (breed == null || !breed.isBound()) return 0xff;
        return (tintIndex == 0? breed.get().primaryColor() : breed.get().secondaryColor()) | (255 << 24);
    }
}
