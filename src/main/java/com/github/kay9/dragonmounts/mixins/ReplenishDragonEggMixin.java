package com.github.kay9.dragonmounts.mixins;

import com.github.kay9.dragonmounts.DMLConfig;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndDragonFight.class)
public class ReplenishDragonEggMixin
{
    /**
     * Purpose: To implement a way to replenish dragon eggs after death of the ender dragon
     * <br>
     * This mixin redirects the 'if' statement in setDragonKilled that tests if the ender dragon was previously killed
     * essentially, instead of {@code if (!previouslyKilled) {...}},
     * we do {@code if (!dragonmounts_replenishDragonEgg) {...}}
     */
    @Redirect(method = "setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;previouslyKilled:Z", opcode = Opcodes.GETFIELD))
    private boolean dragonmounts_replenishDragonEgg(EndDragonFight instance)
    {
        return !(!instance.hasPreviouslyKilledDragon() || DMLConfig.replenishEggs()); // return the inverse of what we want because the target check inverts the result... yeah.
    }
}
