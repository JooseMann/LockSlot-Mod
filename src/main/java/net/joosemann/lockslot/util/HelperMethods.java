package net.joosemann.lockslot.util;

import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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

            // If our adjusted index is negative, then we are dealing with a slot in the 5th (extra) row
            // Adjust our row and col values accordingly
            if (lock.index() < 0) {
                row = 4; // Extras row
                col = (lock.index() + 9) % 9; // Make col positive without changing result by adding 9
            }

            // Since we found this row and column in the persistent data,
            // we know that its slot must be locked. Set the corresponding boolean to true.
            LockedValues.lockedArray[row][col] = true;
        }
    }

    // Gets the index of the top-left slot in the given screen, or -1 if not found.
    public static int getTopLeftSlotIndex(AbstractContainerScreen<?> screen) {
        int tryReferenceIndex = LockedValues.tryGetReferenceSlotIndex(screen);

        // We likely already computed this value in the reference slot cache.
        // If that's the case, then take the index from there instead of recalculating it.
        // Note: this function returns -1 if we have not already found it, and otherwise gives a positive integer index.
        if (tryReferenceIndex != -1) {
            return tryReferenceIndex;
        }

        // If not, then we have to manually compute it. Do that here.

        // Error check, -1 for invalid state
        if (Minecraft.getInstance().player == null) return -1;

        // The top-left slot of the inventory has index 9. Find the equivalent slot for this container
        Slot base = Minecraft.getInstance().player.inventoryMenu.getSlot(9);

        // Store the current item. We will have to overwrite it with a custom item temporarily,
        // so that we can tell which slot is the same across different screens.
        ItemStack prevItem = base.getItem();

        base.set(LockedIndicatorItem.ITEM.getDefaultInstance());

        for (Slot slot : screen.getMenu().slots) {
            // Find the equivalent index for the top-left slot by checking for the same custom item.
            // Only one slot should have this item (only the base slot, and only for this instant),
            // so search for whichever slot in this screen also has that item.
            if (slot.getItem().equals(base.getItem())) {
                // We found the correct slot, return the slot back to its original state
                base.set(prevItem);

                // Add the new index to our reference cache, so that we don't have to recalculate this later.
                LockedValues.addReferenceSlot(screen, slot.index);

                // Return the index with respect to the given screen, instead of the inventory
                return slot.index;
            }
        }

        // Not found, return -1 for error
        return -1;
    }
}
