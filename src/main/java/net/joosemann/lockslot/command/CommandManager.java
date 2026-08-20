package net.joosemann.lockslot.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.networking.packets.PlayerDataPayload;
import net.joosemann.lockslot.util.PacketData;
import net.joosemann.lockslot.util.PlayerLockData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CommandManager {
    // Client-side command that resets the lock data for whichever player ran the command.
    protected static int resetPlayerData(CommandContext<FabricClientCommandSource> context) {
        LocalPlayer player = context.getSource().getPlayer();

        // Reset client-side lock data
        LockedValues.lockedArrayInit(null); // Resets to default (all false)
        LockedValues.lockedListInit(null); // Resets to default (empty list)

        // Tell the server to update its data with empty (default) data
        PlayerLockData data = new PlayerLockData(player.getStringUUID(), new LinkedList<>());
        PlayerDataPayload packet = new PlayerDataPayload(data, PacketData.PacketUse.UPDATE_DATA);
        ClientPlayNetworking.send(packet);

        // Send success message
        context.getSource().sendFeedback(Component.literal("Reset " + player.nameAndId().name() + "'s data"));

        // Returning > 0 indicates success
        return Command.SINGLE_SUCCESS; // = 1
    }

    // Server-side command that resets all data for every
    protected static int resetAllPlayerData(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getServer().isDedicatedServer()) {
            List<ServerPlayer> allPlayers = context.getSource().getServer().getPlayerList().getPlayers();
            LockDataPayload resetDataPacket = new LockDataPayload(new LinkedList<>()); // Packet to tell each client to reset its data

            // Loop through every UUID and reset each one's data
            // Then send each player a packet to reset their data on the client-side
            for (ServerPlayer player : allPlayers) {
                UUID uuid = player.getUUID();

                ServerLockedValues.lockedMapInit(uuid, null); // Resets to all false
                ServerLockedValues.lockedListInit(uuid, null); // Resets to an empty list
                ServerPlayNetworking.send(player, resetDataPacket); // Tell the client to do the same
            }

            // Tell the moderator this
            context.getSource().sendSuccess(() -> Component.literal("Reset all player data"), true);
        }
        // Otherwise, we must be in an integrated server, so just reset our data directly
        else {
            LockedValues.lockedArrayInit(null); // Resets to all false
            LockedValues.lockedListInit(null); // Resets to an empty list

            // Tell the player that the data was reset
            context.getSource().sendSuccess(() -> Component.literal("Reset all player data"), true);
        }

        return Command.SINGLE_SUCCESS; // = 1
    }

    // Client-side command that forces the client to send its data to the server to resync.
    // Note that, if the client and server are already in sync, then nothing has to be synced to begin with.
    // We always have to check if there is a desync, but do not have to resync unless we detect one.
    // Therefore, use the PacketData.PacketUse.CHECK_DESYNC flag for the packet.
    protected static int forceSync(CommandContext<FabricClientCommandSource> context) {
        // Get the player that ran the command
        LocalPlayer player = context.getSource().getPlayer();

        // Send a packet to the server to check for (and correct if needed) a desync.
        PlayerLockData data = new PlayerLockData(player.getStringUUID(), LockedValues.getLockedList());
        PlayerDataPayload packet = new PlayerDataPayload(data, PacketData.PacketUse.CHECK_DESYNC);
        ClientPlayNetworking.send(packet);

        // Send feedback to the player that we are syncing data.
        context.getSource().sendFeedback(Component.literal("Forcing data sync"));

        return Command.SINGLE_SUCCESS; // = 1
    }

    // Dummy functions that do nothing.
    // These allow the server and client to have the same command structure without executing extra (unneeded) code.

    // Server-side dummy function
    public static int dummyServer(CommandContext<CommandSourceStack> context) {
        // Warn the server that this command should be run on the client,
        // assuming that the server itself ran this command and not a player.
        if (!context.getSource().isPlayer()) {
            String msg = "Command \"/" + context.getInput() + "\" should only be run by a player!";
            context.getSource().sendFailure(Component.literal(msg));
        }
        return 0; // Returns 0 to indicate that nothing changed
    }

    // Client-side dummy function
    public static int dummyClient(CommandContext<FabricClientCommandSource> context) {
        // Warn the player that this command is for the server only.
        String msg = "This command should only be run on and by a server!";
        context.getSource().sendError(Component.literal(msg));
        return 0; // Returns 0 to indicate that nothing changed
    }
}
