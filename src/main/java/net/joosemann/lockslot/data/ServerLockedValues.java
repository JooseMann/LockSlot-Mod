package net.joosemann.lockslot.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.joosemann.lockslot.util.LockInstance;

import java.util.*;

// Class holding server-side data on which slots are locked.
// Unlike the client-side, some data on the server-side needs to have its own instance for each player
// For this reason, we separate additional data here that only the server will need to access.
@Environment(EnvType.SERVER)
public class ServerLockedValues {
    // A (5 row, 9 column) 2D array for each player that marks which inventory slots have been locked.
    // The arrays are wrapped around a Hash Map to associate a player (via their UUID) to their lock data.
    private static final HashMap<UUID, Boolean[][]> lockedMapArray = new HashMap<>();

    // A Linked List for each player, here used to keep track of all locks for persistent data.
    // Note that, on the client-side, the corresponding Linked List is also used to help render which slots are locked.
    private static final HashMap<UUID, LinkedList<LockInstance>> lockedMapList = new HashMap<>();

    // Initializes a player's lock data in lockedMapArray
    // Providing null for the `arr` parameter will begin with a Boolean[][] consisting of all false values.
    public static void lockedMapInit(UUID uuid, Boolean[][] arr) {
        if (arr != null) {
            // Initialize this player's data in arr
            lockedMapArray.put(uuid, arr);
        }
        else {
            // Initialize this player's data with a new Boolean[][] consisting of all false values.
            Boolean[][] newData = new Boolean[5][9];
            for (Boolean[] booleans : newData) {
                Arrays.fill(booleans, false);
            }
            lockedMapArray.put(uuid, newData);
        }
    }

    // Initialize a player's lock data in the locked list.
    // Providing null for `list` initializes the data with an empty LinkedList.
    public static void lockedListInit(UUID uuid, List<LockInstance> list) {
        if (list != null) {
            lockedMapList.put(uuid, new LinkedList<>(list));
        }
        else {
            lockedMapList.put(uuid, new LinkedList<>()); // Empty Linked List
        }
    }

    // Gets the locked value as an Optional.
    // If the UUID (a given player) is stored within the map, return that value.
    // Otherwise, return an empty Optional.
    public static Optional<Boolean> getLockedMapArrayValue(UUID uuid, int i, int j) {
        if (lockedMapArray.containsKey(uuid)) {
            return Optional.ofNullable(lockedMapArray.get(uuid)[i][j]);
        }
        return Optional.empty();
    }

    // Gets a player's list data if it exists, or an empty Optional otherwise.
    public static Optional<LinkedList<LockInstance>> getLockedMapList(UUID uuid) {
        return Optional.ofNullable(lockedMapList.get(uuid));
    }

    // Toggles the locked value at index (i, j) for the player given by uuid.
    public static void toggleMapArrayLock(UUID uuid, int i, int j) {
        if (!lockedMapArray.containsKey(uuid)) {
            lockedMapArray.put(uuid, new Boolean[5][9]);
        }

        // We can safely toggle the value directly without worrying about null,
        // since the if statement takes care of adding this UUID to the map if it was not previously there.
        lockedMapArray.get(uuid)[i][j] = !lockedMapArray.get(uuid)[i][j];
    }

    // Adds a slot to the list of locked slots for the player given by uuid
    public static void pushLockedSlot(UUID uuid, LockInstance lock) {
        // Add this player to the map list if needed
        if (!lockedMapList.containsKey(uuid)) {
            ServerLockedValues.lockedListInit(uuid, null); // Providing null initializes with an empty LinkedList
        }
        lockedMapList.get(uuid).add(lock); // Pushes to the end of the list
    }

    // Returns the index of the now-popped slot, or -1 if the slot is not found
    public static int popLockedSlot(UUID uuid, LockInstance given) {
        // If this player isn't in the map list, the slot isn't there, so don't waste effort trying to get to it.
        if (!lockedMapList.containsKey(uuid)) return -1;

        // From here on, lockedMapList.get(uuid) is guaranteed to be non-null

        // Need to find where the right slot to remove is, iterate through the list to find it
        ListIterator<LockInstance> itr = lockedMapList.get(uuid).listIterator();
        int index = 0;

        // Keep looping through the iterator until finding the given slot
        while (itr.hasNext()) {
            LockInstance lock = itr.next();

            // Make sure we account for the first element here
            if (index == 0) {
                lock = itr.previous();
                itr.next();
            }

            // Remove the slot if we found it in the list
            // Slots will be equal if their coordinates and indexes match
            if (lock.index() == given.index()
                    && lock.x() == given.x()
                    && lock.y() == given.y()) {

                // Remove this element from the list
                lockedMapList.get(uuid).remove(itr.nextIndex() - 1);
                return index;
            }

            ++index;
        }

        // Slot not found, return -1
        return -1;
    }
}
