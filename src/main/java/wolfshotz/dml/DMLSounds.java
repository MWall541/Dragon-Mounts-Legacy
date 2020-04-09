package wolfshotz.dml;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class DMLSounds
{
    public static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, DragonMountsLegacy.MOD_ID);

    public static final RegistryObject<SoundEvent> DRAGON_BREATHE = register("dragon.breathe");
    public static final RegistryObject<SoundEvent> DRAGON_STEP = register("dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH = register("dragon.death");

    private static RegistryObject<SoundEvent> register(String name)
    {
        String registryName = "entity." + name;
        return SOUNDS.register(registryName, () -> new SoundEvent(DragonMountsLegacy.rl(registryName)));
    }
}
