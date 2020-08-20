package wolfshotz.dml.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.*;
import java.util.function.Predicate;

public class BetterBlockMatcher implements Predicate<Block>
{
    private final Set<Block> blocks = new HashSet<>();

    public BetterBlockMatcher(Block... blocks)
    {
        add(blocks);
    }

    public BetterBlockMatcher(Collection<Block> blocks)
    {
        add(blocks);
    }

    public BetterBlockMatcher add(Block... additional)
    {
        Collections.addAll(blocks, additional);
        return this;
    }

    public BetterBlockMatcher add(Collection<Block> additional)
    {
        blocks.addAll(additional);
        return this;
    }

    @Override
    public boolean test(Block block)
    {
        return blocks.contains(block);
    }

    public boolean test(BlockState state)
    {
        return test(state.getBlock());
    }
}
