package net.joosemann.lockslot.networking.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetworkHandlers extends LockedValues {
    public static void registerServerReceivers() {
        // SaveLockData Packet
        // Received from the client after asking for its lock data.
        // Used here to save the data via a data attachment to the player server-side.
        ServerPlayNetworking.registerGlobalReceiver(LockDataPayload.ID, ((payload, context) -> {

            // Get the player, guaranteed to be non-null
            ServerPlayer player = context.player();

            // Check if we already have data attached.
            // If so, we want to overwrite it in favor of our new data.
            // Therefore, we remove the current data if it exists.
            if (player.hasAttached(LockInstance.LOCK_ATTACHMENT_TYPE)) {
                player.removeAttached(LockInstance.LOCK_ATTACHMENT_TYPE);
            }

            // Attach our current locked slots to the player, so they retain the information on the next session.
            player.setAttached(LockInstance.LOCK_ATTACHMENT_TYPE, payload.lockedList());

            LockSlot.LOGGER.info("Saved lock data");
        }));
    }
}
