package wolfshotz.dml.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class BetterBlockMatcher implements Predicate<Block>
{
    private final Collection<Block> blocks;

    public BetterBlockMatcher(Block... blocks)
    {
        this(Arrays.asList(blocks));
    }

    public BetterBlockMatcher(Collection<Block> blocks)
    {
        this.blocks = blocks;
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
