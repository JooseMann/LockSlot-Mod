package net.joosemann.lockslot.client;

import net.joosemann.lockslot.LockSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

import java.util.LinkedList;
import java.util.ListIterator;

public class LockedValues {
    // 2D array that represents which slots in the inventory are currently locked.
    // NOTE: The last row is allocated for other slots (armor, off-hand, & crafting)
    private static final boolean[][] lockedArray = new boolean[5][9];

    // Linked List of all currently locked slots.
    private static final LinkedList<Slot> lockedList = new LinkedList<>();

    private static final Identifier id = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "textures/gui/locked_slot_bkg.png");

    public static Identifier getLockRenderingId() {
        return id;
    }

    public static boolean getLockedValue(int i, int j) {
        return lockedArray[i][j];
    }

    // Get the iterator of lockedList
    public static ListIterator<Slot> getLockedListIterator() {
        return lockedList.listIterator();
    }

    // Swap whether a specific slot is locked or not
    // Lock if it is unlocked, and unlock if it is locked
    // Return the new status of the given slot
    public static boolean swapLockedArrayValue(int i, int j) {
        lockedArray[i][j] = !lockedArray[i][j];
        return lockedArray[i][j];
    }

    // Adds a slot to the list of locked slots
    public static void pushLockedSlot(Slot slot) {
        lockedList.add(slot); // Pushes to the end of the list
    }

    // Returns the index of the now-popped slot, or -1 if the slot is not found
    public static int popLockedSlot(Slot slot) {
        // Need to find where the right slot to remove is, iterate through the list to find it
        ListIterator<Slot> itr = lockedList.listIterator();
        int index = 0;

        // Keep looping through the iterator until finding the given slot
        while (itr.hasNext()) {
            Slot lockedSlot = itr.next();

            // Make sure we account for the first element here
            if (index == 0) {
                lockedSlot = itr.previous();
                itr.next();
            }

            // Remove the slot if we found it in the list
            // Slots will only be equal if their indices match
            if (lockedSlot.index == slot.index) {
                // Remove this element from the list
                lockedList.remove(itr.nextIndex() - 1);
                return index;
            }

            ++index;
        }

        // Slot not found, return -1
        return -1;
    }
}
