package wolfshotz.dml.cmd;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.command.EnumArgument;
import wolfshotz.dml.entities.TameableDragonEntity;
import wolfshotz.dml.entities.ai.LifeStageController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DragonSetAgeCommand
{
    public static final int SEARCH_DIST = 10;
    public static final SimpleCommandExceptionType NO_NEARBY_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.dragonmounts.setage.nodragons"));

    public static ArgumentBuilder<CommandSource, ?> register()
    {
        return Commands.literal("dragon")
                .then(Commands.literal("setage")
                        .requires(c -> c.hasPermissionLevel(2))
                        .then(Commands.argument("age", EnumArgument.enumArgument(LifeStageController.EnumLifeStage.class)).executes(DragonSetAgeCommand::execute)));
    }

    private static int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException
    {
        Entity executor = ctx.getSource().getEntity();
        // Get Dragons nearby
        List<TameableDragonEntity> nearby = executor.world.getEntitiesWithinAABB(TameableDragonEntity.class, executor.getBoundingBox().grow(SEARCH_DIST));
        // Get the closest to the executor
        Optional<TameableDragonEntity> closest = nearby.stream().min(Comparator.comparingDouble(executor::getDistance));
        // uhhhh...
        if (!closest.isPresent()) throw NO_NEARBY_EXCEPTION.create();

        LifeStageController.EnumLifeStage stage = ctx.getArgument("age", LifeStageController.EnumLifeStage.class);

        closest.get().getLifeStageController().setLifeStage(stage);
        executor.sendMessage(new TranslationTextComponent("commands.dragonmounts.setage.success", closest.get().getName().toString(), stage), executor.getUniqueID());

        return 0;
    }
}
