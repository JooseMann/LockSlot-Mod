package net.joosemann.lockslot.networking.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.networking.packets.LockInstancePayload;
import net.joosemann.lockslot.util.LockInstance;

public class ServerNetworkHandlers extends LockedValues {
    public static void registerServerReceivers() {
        // AddLockInstance Packet
        // Received from the client when the list of locked slots is updated.
        // Used to keep the client and the server in sync with the list of locked slots.
        ServerPlayNetworking.registerGlobalReceiver(LockInstancePayload.ID, ((payload, context) -> {
            // If we are playing on a dedicated server, make sure to update locked values there as well
            if (context.server().isDedicatedServer()) {
                // Get the lock and its row & column indices
                LockInstance lock = payload.lock();
                int rowIndex = lock.index() / 9;
                int colIndex = lock.index() % 9;

                // Check if we are adding or removing this LockInstance from the list
                if (lock.locking()) { // Adding
                    LockedValues.pushLockedSlot(lock);
                } else { // Removing
                    LockedValues.popLockedSlot(lock);
                }

                // Swap the corresponding value on the boolean array.
                LockedValues.swapLockedArrayValue(rowIndex, colIndex);
            }
        }));
    }
}
