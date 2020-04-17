package wolfshotz.dml.cmd;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.command.EnumArgument;
import wolfshotz.dml.entity.dragons.TameableDragonEntity;
import wolfshotz.dml.entity.dragons.ai.LifeStageController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DragonSetAgeCommand
{
    public static final int SEARCH_DIST = 10;
    public static final SimpleCommandExceptionType NO_NEARBY_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.dragonmounts.setage.nodragons"));

    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("setage")
                .requires(c -> c.hasPermissionLevel(2))
                .then(
                        Commands.argument("age", EnumArgument.enumArgument(LifeStageController.EnumLifeStage.class))
                                .executes(ctx -> executes(ctx.getSource(), ctx.getArgument("age", LifeStageController.EnumLifeStage.class)))
                );
    }

    public static int executes(CommandSource source, LifeStageController.EnumLifeStage stage) throws CommandSyntaxException
    {
        Entity executor = source.getEntity();
        // Get Dragons nearby
        List<TameableDragonEntity> nearby = executor.world.getEntitiesWithinAABB(TameableDragonEntity.class, executor.getBoundingBox().grow(SEARCH_DIST));
        // Get the closest to the executor
        Optional<TameableDragonEntity> closest = nearby.stream().min(Comparator.comparingDouble(executor::getDistance));
        // uhhhh...
        if (!closest.isPresent()) throw NO_NEARBY_EXCEPTION.create();

        closest.get().getLifeStageController().setLifeStage(stage);
        executor.sendMessage(new TranslationTextComponent("commands.dragonmounts.setage.success", closest.get().getName().getFormattedText(), stage));

        return 0;
    }
}
