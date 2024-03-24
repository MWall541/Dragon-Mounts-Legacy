package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class MountCameraManager
{
    private static CameraType previousPerspective = CameraType.FIRST_PERSON;

    public static void onDragonMount()
    {
        if (DMLConfig.thirdPersonOnMount())
        {
            previousPerspective = Minecraft.getInstance().options.getCameraType();
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    public static void onDragonDismount()
    {
        if (DMLConfig.thirdPersonOnMount())
            Minecraft.getInstance().options.setCameraType(previousPerspective);
    }

    @SuppressWarnings("ConstantConditions") // player should never be null at time of calling
    public static void setMountCameraAngles(Camera camera)
    {
        if (Minecraft.getInstance().player.getVehicle() instanceof TameableDragon && !Minecraft.getInstance().options.getCameraType().isFirstPerson())
        {
            var offsets = DMLConfig.getCameraPerspectiveOffset(Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK);
            camera.move(0, offsets[1].get(), offsets[2].get());
            camera.move(-camera.getMaxZoom(offsets[0].get()), 0, 0); // do distance calcs AFTER our new position is set
        }
    }
}
