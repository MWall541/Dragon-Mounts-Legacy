package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry extends SoundEvent
{
    public static final RegistryObject<SoundEvent> DRAGON_BREATHE_SOUND = sound("entity.dragon.breathe");
    public static final RegistryObject<SoundEvent> DRAGON_STEP_SOUND = sound("entity.dragon.step");
    public static final RegistryObject<SoundEvent> DRAGON_DEATH_SOUND = sound("entity.dragon.death");

    public static final RegistryObject<SoundRegistry> FIRE_BREATH_START = breath("fire.start", 2);
    public static final RegistryObject<SoundRegistry> FIRE_BREATH_LOOP = breath("fire.loop", 5.336);
    public static final RegistryObject<SoundRegistry> FIRE_BREATH_STOP = breath("fire.stop", 1);

    public static final RegistryObject<SoundRegistry> ICE_BREATH_START = breath("ice.start", 2);
    public static final RegistryObject<SoundRegistry> ICE_BREATH_LOOP = breath("ice.loop", 5.336);
    public static final RegistryObject<SoundRegistry> ICE_BREATH_STOP = breath("ice.stop", 1);

    private static RegistryObject<SoundEvent> sound(String name)
    {
        return DMLRegistry.register(name, ForgeRegistries.Keys.SOUND_EVENTS, () -> new SoundEvent(DragonMountsLegacy.id(name)));
    }

    private static RegistryObject<SoundRegistry> breath(String name, double durationInSeconds)
    {
        var id = "entity.dragon.weapon.breath." + name;
        return DMLRegistry.register(id, ForgeRegistries.Keys.SOUND_EVENTS, () -> new SoundRegistry(DragonMountsLegacy.id(id), durationInSeconds));
    }

    public static void bootstrap() {} // class isn't loaded in time...

    private final double durationInSeconds;
    public SoundRegistry(ResourceLocation id, double durationInSeconds)
    {
        super(id);
        this.durationInSeconds = durationInSeconds;
    }

    public int getDurationInTicks()
    {
        return (int) (durationInSeconds * 20); // cast to int, should be close enough.
    }
}
