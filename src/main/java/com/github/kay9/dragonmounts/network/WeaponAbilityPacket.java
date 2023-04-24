package com.github.kay9.dragonmounts.network;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.abilities.Ability;
import com.github.kay9.dragonmounts.abilities.WeaponAbility;
import com.github.kay9.dragonmounts.entity.dragon.TameableDragon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// todo: rename?
public class WeaponAbilityPacket
{
    private final int dragonId;
    private final int abilityId;
    private final boolean attacking;

    public WeaponAbilityPacket(TameableDragon dragon, Ability ability, boolean attacking)
    {
        this.dragonId = dragon.getId();
        this.abilityId = dragon.getBreed().abilities().indexOf(ability);
        this.attacking = attacking;
    }

    public WeaponAbilityPacket(FriendlyByteBuf buf)
    {
        this.dragonId = buf.readInt();
        this.abilityId = buf.readInt();
        this.attacking = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(dragonId);
        buf.writeInt(abilityId);
        buf.writeBoolean(attacking);
    }

    public void prepare(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> handle(ctx.get()));
        ctx.get().setPacketHandled(true);
    }

    public void handle(NetworkEvent.Context ctx)
    {
        var dragon = (TameableDragon) ctx.getSender().getLevel().getEntity(dragonId);
        var ability = ((WeaponAbility) dragon.getBreed().abilities().get(abilityId));
        ability.setAttacking(dragon, attacking);
        if (attacking) ability.attack(dragon);
    }

    public static void send(TameableDragon dragon, Ability ability, boolean breathing)
    {
        DragonMountsLegacy.NETWORK.sendToServer(new WeaponAbilityPacket(dragon, ability, breathing));
    }
}
