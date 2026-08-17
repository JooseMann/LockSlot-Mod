package net.joosemann.lockslot.networking.handlers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.joosemann.lockslot.networking.packets.CheckLockDesyncPayload;
import net.joosemann.lockslot.networking.packets.LockInstancePayload;
import net.joosemann.lockslot.util.LockInstance;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;

public class ServerNetworkHandlers {
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
                    ServerLockedValues.pushLockedSlot(context.player().getUUID(), lock);
                } else { // Removing
                    ServerLockedValues.popLockedSlot(context.player().getUUID(), lock);
                }

                // Swap the corresponding value on the boolean array for this player.
                ServerLockedValues.toggleMapArrayLock(context.player().getUUID(), rowIndex, colIndex);
            }
        }));

        // CheckLockDesync Packet
        // Received from the client whenever we need to make sure that the locks haven't desynced,
        // and resyncs with the data from the client if necessary.
        ServerPlayNetworking.registerGlobalReceiver(CheckLockDesyncPayload.ID, ((payload, context) -> {
            // We want to check for desyncs in data. These desyncs will not happen in an integrated server
            // (i.e., singleplayer), so only go check for possible desyncs in a dedicated (multiplayer) server.
            if (context.server().isDedicatedServer()) {
                // Check if the data present in the packet matches what we have on the server.

                // Useful helper variables
                boolean dataMatches = true; // Whether the data matches by the end
                int lockIndex; // Index of the lock we're looking at
                UUID uuid = UUID.fromString(payload.data().uuid()); // Player UUID (convert from String to UUID)
                ListIterator<LockInstance> itr = payload.data().locks().listIterator(); // Lock data to check against

                // Check every lock value in the list, make sure they all match.
                while (itr.hasNext()) {
                    // Make sure we count the first element in the list
                    if (itr.previousIndex() == -1) {
                        itr.next();
                        lockIndex = itr.previous().index();
                        itr.next();
                    } else {
                        lockIndex = itr.next().index();
                    }

                    // Calculate the row and column of the slot based on its index
                    int row = lockIndex / 9;
                    int col = lockIndex % 9;

                    // Get the lock status
                    Optional<Boolean> isLocked = ServerLockedValues.getLockedMapArrayValue(uuid, row, col);

                    // Check if this slot is locked (isLocked is true)
                    if (!(isLocked.isPresent() && isLocked.get())) {
                        // Slot is *not* locked, we need to resync locks.
                        dataMatches = false;
                        break;
                    }
                }

                // If the data still matches, then we are still in sync and our work is done
                if (dataMatches) return;

                // Otherwise, we have to resync the data.
                // Reset server-side values, and update them based on what we have in `payload`.

                // Overwrite lockedMapList with the new values we were given.
                ServerLockedValues.lockedListInit(uuid, payload.data().locks());

                // Create a new Boolean[][] to overwrite lockedMapArray with.
                Boolean[][] updatedLocks = new Boolean[5][9];
                for (Boolean[] booleans : updatedLocks) {
                    Arrays.fill(booleans, false);
                }

                // Go back through our given list of locks and recalculate which indexes need to be locked.
                itr = payload.data().locks().listIterator(); // Reset itr
                while (itr.hasNext()) {
                    // Make sure we count the first element in the list
                    if (itr.previousIndex() == -1) {
                        itr.next();
                        lockIndex = itr.previous().index();
                        itr.next();
                    } else {
                        lockIndex = itr.next().index();
                    }

                    // Calculate the row and column of the slot based on its index
                    int row = lockIndex / 9;
                    int col = lockIndex % 9;

                    // Now set the corresponding value in our Boolean[][]
                    updatedLocks[row][col] = true;
                }

                // Update (overwrite) lockMapArray with our new data
                ServerLockedValues.lockedMapInit(uuid, updatedLocks);
            }
        }));
    }
}
