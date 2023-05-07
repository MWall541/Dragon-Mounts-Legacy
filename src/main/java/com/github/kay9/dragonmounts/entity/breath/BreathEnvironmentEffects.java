package com.github.kay9.dragonmounts.entity.breath;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class BreathEnvironmentEffects
{
    private final Map<Block, BreathBehavior> behaviors = new HashMap<>();

    public void affectEnvironment(Level level, BlockPos affectingPos, BlockState affecting, double chance)
    {
        var behavior = getBehaviorFor(affecting.getBlock());
        if (behavior != null)
            behavior.affect(level, affectingPos, affecting, chance);
    }

    public BreathBehavior getBehaviorFor(Block affecting)
    {
        return behaviors.get(affecting);
    }

    public void register(Block affected, BreathBehavior behavior)
    {
        behaviors.put(affected, behavior);
    }

    public void registerBasicReplacer(Block affecting, Block replacer, double chance)
    {
        register(affecting, (level, pos, state, c) ->
        {
            if (c < chance) level.setBlockAndUpdate(pos, replacer.defaultBlockState());
        });
    }

    public void registerDestructiveReplacer(Block affecting, double chance, boolean drops)
    {
        register(affecting, ((level, affectedPos, affectedState, c) ->
        {
            if (c < chance) level.destroyBlock(affectedPos, drops);
        }));
    }

    public interface BreathBehavior
    {
        void affect(Level level, BlockPos affectedPos, BlockState affectedState, double chance);
    }
}
