package net.joosemann.lockslot.event;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.joosemann.lockslot.command.CommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.NonNull;

public class RegisterServerCommandEvent extends CommandManager implements CommandRegistrationCallback {
    // Defines commands `/lockslot data reset`, `/lockslot data reset all`, and `/lockslot data sync`
    // and implements `/lockslot data reset all` on the server-side.
    // The other two commands do nothing on the server-side (here), as we want all of them to run on the client.
    // Therefore, we define these two commands so the server is aware of them and does not error out, while the
    // implementation of those commands is handled on the client-side.
    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, @NonNull CommandBuildContext commandBuildContext, Commands.@NonNull CommandSelection commandSelection) {
        commandDispatcher.register(Commands.literal("lockslot")
            .then(Commands.literal("data")
                .then(Commands.literal("reset").executes(CommandManager::dummyServer) // "/lockslot data reset"
                    .then(Commands.literal("all") // "/lockslot data reset all"
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .executes(CommandManager::resetAllPlayerData))) // Requires moderator permissions
                .then(Commands.literal("sync").executes(CommandManager::dummyServer))) // "/lockslot data sync"
        );
    }
}
