package com.github.kay9.dragonmounts.dragon.abilities;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.ForgeEventFactory;

public abstract class FootprintAbility implements Ability
{
    @Override
    public void onMove(TameableDragon dragon)
    {
        if (dragon.getAgeProgress() < 0.5 || !dragon.onGround()) return;
        if (!ForgeEventFactory.getMobGriefingEvent(dragon.level(), dragon)) return;

        var chance = getFootprintChance(dragon);
        if (chance == 0) return;

        for (int i = 0; i < 4; i++)
        {
            // place only if randomly selected
            if (dragon.getRandom().nextFloat() > chance) continue;

            // get footprint position
            int bx = (int) (dragon.getX() + (i % 2 * 2 - 1) * dragon.getAgeScale());
            int by = (int) dragon.getY();
            int bz = (int) (dragon.getZ() + (i / 2f % 2 * 2 - 1) * dragon.getAgeScale());
            var pos = new BlockPos(bx, by, bz);

            placeFootprint(dragon, pos);
        }
    }

    protected float getFootprintChance(TameableDragon dragon)
    {
        return 0.05f;
    }

    protected abstract void placeFootprint(TameableDragon dragon, BlockPos pos);
}
