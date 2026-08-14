package net.joosemann.lockslot.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.util.ClientHelperMethods;
import net.joosemann.lockslot.util.LockInstance;
import net.joosemann.lockslot.util.ServerHelperMethods;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PlayerLoginEvent implements ServerPlayConnectionEvents.Join {

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, @NonNull PacketSender sender, @NonNull MinecraftServer server) {
        // When logging in, we want to restore any lock data, if it exists.
        // Check if we have any lock data to reference, and set it if so.

        // Player here is never null, so we don't need to check for that case
        ServerPlayer player = handler.player;

        // Check if we have data attached to the player.
        // If not, we do not have any data to reference, so we will just use our default values in LockedValues.
        if (!player.hasAttached(LockInstance.LOCK_ATTACHMENT_TYPE)) return;

        // Now that we know that we have data, grab it here.
        List<LockInstance> lockData = player.getAttached(LockInstance.LOCK_ATTACHMENT_TYPE);

        // Now set the lockedList from LockedValues to the data we grabbed from the player.
        if (lockData != null) {
            // Update lock data on both the client- and server-side.
            // This code will run on both sides, so just check which side we're running on.
            if (server.isDedicatedServer()) {
                // Server-side
                ServerHelperMethods.updateServerLockData(player.getUUID(), lockData);
            }
            else {
                // Client-side
                ClientHelperMethods.updateLockData(lockData);
            }

            // Now send this data to the client, so these values can also be updated client-side.
            LockDataPayload payload = new LockDataPayload(lockData);
            ServerPlayNetworking.send(player, payload);
        }
    }
}
