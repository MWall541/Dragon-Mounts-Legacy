package wolfshotz.dml.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.network.NetworkEvent;
import org.lwjgl.glfw.GLFW;
import wolfshotz.dml.DragonMountsLegacy;
import wolfshotz.dml.entities.TameableDragonEntity;

import java.util.function.Supplier;

public class BreathKeybind extends KeyBinding
{
    private boolean prevPressed;

    public BreathKeybind()
    {
        super("key.breathKey", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_R), "key.categories.gameplay");
    }

    @Override
    public void setPressed(boolean pressed)
    {
        super.setPressed(pressed);

        if (prevPressed == pressed || !(Minecraft.getInstance().player.getRidingEntity() instanceof TameableDragonEntity))
            return;
        prevPressed = pressed;
        DragonMountsLegacy.NETWORK.sendToServer(new Packet(pressed));
    }

    public static class Packet
    {
        private final boolean pressed;

        public Packet(boolean pressed) { this.pressed = pressed; }

        public Packet(PacketBuffer buf) { this.pressed = buf.readBoolean(); }

        public void encode(PacketBuffer buf) { buf.writeBoolean(pressed); }

        public void handle(Supplier<NetworkEvent.Context> context)
        {
            context.get().enqueueWork(() -> ((TameableDragonEntity) context.get().getSender().getRidingEntity()).setBreathing(pressed));
            context.get().setPacketHandled(true);
        }
    }
}
