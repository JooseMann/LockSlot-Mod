package net.joosemann.lockslot.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.joosemann.lockslot.data.ServerLockedValues;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

// Helper class for various methods that manipulate lock data on the server-side.
@Environment(EnvType.SERVER)
public class ServerHelperMethods {

    // Update our mod data on the server-side with the given lock data.
    // While most data is handled client-side, some parts (e.g., persistent data) require the server's help.
    public static void updateServerLockData(UUID uuid, List<LockInstance> lockData) {
        // Update the server's lockedList
        ServerLockedValues.lockedListInit(uuid, lockData);

        // We also want to set lockedMap to keep our data updated.

        // Make sure to clear out lock data first, because it may have some residual true's from another world.
        ServerLockedValues.lockedMapInit(uuid, null); // Calling init with null results in all false

        ListIterator<LockInstance> itr = lockData.listIterator();
        LockInstance lock;

        // Iterate through every LockInstance
        while (itr.hasNext()) {
            if (itr.previousIndex() == -1) {
                // At the start of the list, make sure to count the first element
                itr.next();
                lock = itr.previous();
                itr.next();
            } else {
                lock = itr.next();
            }

            // Find the corresponding row and column for this slot.
            // Note that the indexes are offset by the top-left slot, so the top-left slot has an index of 0.
            int row = lock.index() / 9;
            int col = lock.index() % 9;

            // Since we found this row and column in the persistent data,
            // we know that its slot must be locked.
            // Every value defaults to false, so toggle it to true.
            ServerLockedValues.toggleMapArrayLock(uuid, row, col);
        }
    }
}
