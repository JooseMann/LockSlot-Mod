package net.joosemann.lockslot.util;

import net.joosemann.lockslot.data.LockedValues;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class HelperMethods extends LockedValues {

    // Update our mod data from persistent data
    public static void updateLockData(List<LockInstance> lockData) {
        // Set lockedList to the provided data in lockData.
        // We need to convert to a LinkedList<LockInstance> from a List<LockInstance>. Use the LinkedList constructor.
        LockedValues.lockedList = new LinkedList<>(lockData);

        // We also want to set lockedArray while we are here.
        // Each LockInstance has an index that we can use to place it into the array.
        // Make sure to clear out lockedArray first, because it may have some residual true's from another world.
        for (int i = 0; i < LockedValues.lockedArray.length; ++i) {
            Arrays.fill(LockedValues.lockedArray[i], false);
        }

        ListIterator<LockInstance> itr = LockedValues.getLockedListIterator();
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
            // we know that its slot must be locked. Set the corresponding boolean to true.
            LockedValues.lockedArray[row][col] = true;
        }
    }
}
