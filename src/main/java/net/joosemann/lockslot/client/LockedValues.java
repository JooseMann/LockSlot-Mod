package net.joosemann.lockslot.client;

import com.mojang.datafixers.util.Pair;
import net.joosemann.lockslot.LockSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class LockedValues {
    // 2D array that represents which slots in the inventory are currently locked.
    // NOTE: The last row is allocated for other slots (armor, off-hand, & crafting)
    private static final boolean[][] lockedArray = new boolean[5][9];

    // Linked List of all currently locked slots.
    // Holds the coordinates to every found slot
    private static final LinkedList<Pair<Integer, Integer>> lockedList = new LinkedList<>();

    // Hashmap that associates a container screen to the index of the top-left slot.
    // This acts as a cache when rendering locked icons, so we don't need to recalculate that top-left slot every frame.
    private static final HashMap<AbstractContainerScreen<?>, Integer> referenceSlotCache = new HashMap<>();

    private static final Identifier lockRenderingId = Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "textures/gui/locked_slot_bkg.png");

    public static Identifier getLockRenderingId() {
        return lockRenderingId;
    }

    public static boolean getLockedValue(int i, int j) {
        return lockedArray[i][j];
    }

    // Get the iterator of lockedList
    public static ListIterator<Pair<Integer, Integer>> getLockedListIterator() {
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
    public static void pushLockedSlot(int x, int y) {
        lockedList.add(new Pair<>(x, y)); // Pushes to the end of the list
    }

    // Returns the index of the now-popped slot, or -1 if the slot is not found
    public static int popLockedSlot(int x, int y) {
        // Need to find where the right slot to remove is, iterate through the list to find it
        ListIterator<Pair<Integer, Integer>> itr = lockedList.listIterator();
        int index = 0;

        // Keep looping through the iterator until finding the given slot
        while (itr.hasNext()) {
            Pair<Integer, Integer> coordinates = itr.next();

            // Make sure we account for the first element here
            if (index == 0) {
                coordinates = itr.previous();
                itr.next();
            }

            // Remove the slot if we found it in the list
            // Slots will be equal if their coordinates match
            if (coordinates.getFirst() == x && coordinates.getSecond() == y) {
                // Remove this element from the list
                lockedList.remove(itr.nextIndex() - 1);
                return index;
            }

            ++index;
        }

        // Slot not found, return -1
        return -1;
    }

    public static int tryGetReferenceSlotIndex(AbstractContainerScreen<?> screen) {
        if (referenceSlotCache.containsKey(screen)) {
            return referenceSlotCache.get(screen);
        }
        return -1;
    }

    // Adds a screen's index to the list of reference slots
    public static void addReferenceSlot(AbstractContainerScreen<?> screen, int index) {
        referenceSlotCache.put(screen, index);
    }

    public static boolean determineSlotLockStatus(Slot slot, int leftPos, int topPos) {
        // TODO: Get the other values for extra slots! (armor and off-hand)
        // NOTE: The values from slot.y and slot.x are flipped for row and col,
        // because row corresponds to the y-axis and not the x-axis.
        int row = (slot.y - 84) / 18;
        int col = (slot.x - 8) / 18;

        // In many containers (chests, crafting tables, etc.), any additional slots may not be locked.
        // Those additional slots are always above our inventory, so these cases will have row < 0.
        if (row < 0 || col < 0) return false;

        return getLockedValue(row, col);
    }
}
