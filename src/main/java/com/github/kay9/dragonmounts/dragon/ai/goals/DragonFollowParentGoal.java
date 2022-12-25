package com.github.kay9.dragonmounts.dragon.ai.goals;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;

/**
 * Goal for baby dragons to follow their parents.
 * Extends <code>FollowParentGoal</code> with support for sitting.
 *
 * @author AnimalsWritingCode
 */
public class DragonFollowParentGoal extends FollowParentGoal {
    private final TameableDragon dragon;

    public DragonFollowParentGoal(TameableDragon dragon, double speedMultiplier) {
        super(dragon, speedMultiplier);

        this.dragon = dragon;
    }

    public boolean canUse() {
        return this.dragon.isOrderedToSit() ? false : super.canUse();
    }

    public boolean canContinueToUse() {
        return this.dragon.isOrderedToSit() ? false : super.canContinueToUse();
    }
}
