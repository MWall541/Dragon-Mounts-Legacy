package com.github.kay9.dragonmounts.entity.breath;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ideas:
 *  - Air oxidizes copper
 */
public class AirBreathNode extends BreathNode
{
    public AirBreathNode(EntityType<? extends BreathNode> type, Level level)
    {
        super(type, level);
    }

    public AirBreathNode(EntityType<? extends BreathNode> type, Entity shooter, Vec3 startPos, Vec3 direction)
    {
        super(type, shooter, startPos, direction);
    }
}
