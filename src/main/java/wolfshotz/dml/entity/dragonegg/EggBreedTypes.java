package wolfshotz.dml.entity.dragonegg;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.BiomeDictionary;
import wolfshotz.dml.block.DragonEggBlock;
import wolfshotz.dml.entity.DMLEntities;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.util.MathX;

import java.util.List;
import java.util.function.Supplier;

public enum EggBreedTypes implements IStringSerializable
{
    AETHER(DMLEntities.AETHER_DAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg) { return egg.getPosY() > egg.world.getHeight() * 0.66f; }
            },
    ENDER(DMLEntities.END_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg) { return BiomeDictionary.hasType(egg.world.getBiome(egg.getPosition()), BiomeDictionary.Type.END); }
            },
    FIRE(DMLEntities.FIRE_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    Block block = egg.world.getBlockState(egg.getPosition()).getBlock();
                    return block == Blocks.LAVA || block == Blocks.FIRE || egg.world.getBlockState(egg.getPosition().down(1)).getBlock() == Blocks.MAGMA_BLOCK;
                }
            },
    FOREST(DMLEntities.FOREST_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    return MathX.findBlockstateInArea(egg.world, egg.getBoundingBox().grow(2), BlockTags.LEAVES.getAllElements().toArray(new Block[0]));
                }
            },
    GHOST(DMLEntities.GHOST_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    // woah dude, too high!
                    if (egg.getPosY() > egg.world.getHeight() * 0.25) return false;

                    BlockPos pos = egg.getPosition();

                    // sun is shining!
                    if (egg.world.canBlockSeeSky(pos)) return false;

                    // too bright!
                    return egg.world.getLight(pos) <= 4;
                }
            },
    ICE(DMLEntities.ICE_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    List<Block> blocks = Lists.newArrayList(BlockTags.ICE.getAllElements().toArray(new Block[0]));
                    blocks.addAll(Lists.newArrayList(Blocks.SNOW, Blocks.SNOW_BLOCK));
                    return MathX.findBlockstateInArea(egg.world, egg.getBoundingBox().grow(2), blocks.toArray(new Block[0]));
                }
            },
    NETHER(DMLEntities.NETHER_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    return BiomeDictionary.hasType(egg.world.getBiome(egg.getPosition()), BiomeDictionary.Type.NETHER);
                }
            },
    WATER(DMLEntities.WATER_DRAGON)
            {
                @Override
                public boolean isHabitat(DragonEggEntity egg)
                {
                    return egg.world.getBlockState(egg.getPosition()).getBlock() == Blocks.WATER;
                }
            };

    public static final EggBreedTypes[] VALUES = values();

    public final Supplier<EntityType<TameableDragonEntity>> type;

    EggBreedTypes(Supplier<EntityType<TameableDragonEntity>> type)
    {
        this.type = type;
    }

    public static EntityType<TameableDragonEntity> getTypeByTest(DragonEggEntity egg)
    {
        for (EggBreedTypes breed : VALUES)
        {
            if (breed.isHabitat(egg)) return breed.type.get();
        }
        return null; // run a null test for this. if its not in a needed habitat, dw about it!
    }

    public static EntityType<TameableDragonEntity> getTypeByBlockState(BlockState state)
    {
        for (EggBreedTypes breed : VALUES)
        {
            if (breed == state.get(DragonEggBlock.BREED)) return breed.type.get();
        }
        return DMLEntities.AETHER_DAGON.get(); // how tho?
    }

    public static EggBreedTypes getByEntityType(EntityType<TameableDragonEntity> type)
    {
        for (EggBreedTypes breed : VALUES)
        {
            if (breed.type.get().equals(type)) return breed;
        }
        return AETHER; // fallback!!
    }

    public boolean isHabitat(DragonEggEntity egg) { return false; }

    @Override
    public String getName() { return toString().toLowerCase(); }
}
