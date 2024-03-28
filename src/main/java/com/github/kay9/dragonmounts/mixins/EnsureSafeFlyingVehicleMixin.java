package com.github.kay9.dragonmounts.mixins;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.FlyingAnimal;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class EnsureSafeFlyingVehicleMixin
{
    @Shadow
    private boolean clientVehicleIsFloating;

    /**
     * Purpose: to ensure players who ride flight-capable vehicles don't get kicked for flying
     * without needing to compromise server security by disabling "Kick for flying"
     */
    @Redirect(method = "handleMoveVehicle(Lnet/minecraft/network/protocol/game/ServerboundMoveVehiclePacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;clientVehicleIsFloating:Z", opcode = Opcodes.PUTFIELD))
    private void dragonmounts_ensureSafeFlyingVehicle(ServerGamePacketListenerImpl impl, boolean flag)
    {
        clientVehicleIsFloating = (!(impl.getPlayer().getRootVehicle() instanceof FlyingAnimal a) || !a.isFlying()) && flag;
    }
}
