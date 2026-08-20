package net.joosemann.lockslot.event;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.joosemann.lockslot.command.CommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.NonNull;

public class RegisterClientCommandEvent extends CommandManager implements ClientCommandRegistrationCallback {

    // Creates the commands `/lockslot data reset`, `/lockslot data reset all`, and `/lockslot data sync`.
    // Also implements `/lockslot data reset` and `/lockslot data sync`.
    // Both of these commands are functions that are led by the client, so they only trigger on the client-side.
    // The other command does the opposite, only triggering on the server-side.
    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> commandDispatcher, @NonNull CommandBuildContext commandBuildContext) {
        commandDispatcher.register(ClientCommandManager.literal("lockslot")
            .then(ClientCommandManager.literal("data")
                .then(ClientCommandManager.literal("reset").executes(CommandManager::resetPlayerData) // "/lockslot data reset"
                    .then(ClientCommandManager.literal("all") // "/lockslot data reset all" (requires moderator permissions)
                        .requires(source -> source.getPlayer().permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                        .executes(CommandManager::dummyClient))) // Does nothing, handled server-side
                .then(ClientCommandManager.literal("sync").executes(CommandManager::forceSync))) // "/lockslot data sync"
        );
    }
}
