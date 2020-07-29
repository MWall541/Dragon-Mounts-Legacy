package wolfshotz.dml.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GhostDragonEntity extends TameableDragonEntity
{
    public GhostDragonEntity(EntityType<? extends TameableDragonEntity> type, World world)
    {
        super(type, world);
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return getRNG().nextBoolean()? super.getAmbientSound() : SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    @Override
    public float getSoundPitch(SoundEvent sound)
    { // THICC BONE SOUNDS
        return sound == SoundEvents.ENTITY_SKELETON_AMBIENT? 2 : super.getSoundPitch(sound);
    }

    public static int getHabitatPoints(DragonEggEntity egg)
    {
        int points = 0;

        // woah dude, too high!
        if (egg.getPosY() > egg.world.getHeight() * 0.25) return 0;

        BlockPos pos = egg.getPosition();

        // sun is shining!
        if (egg.world.canBlockSeeSky(pos)) return 0;

        // too bright!
        return egg.world.getLight(pos) <= 4? 0 : 3;
    }
}
