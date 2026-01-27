package net.joosemann.lockslot.client;

public class LockedValues {
    // NOTE: The last row is allocated for other slots (armor, off-hand, & crafting)
    private static boolean[][] lockedArray = new boolean[5][9];

    public static boolean getLockedValue(int i, int j) {
        return lockedArray[i][j];
    }

    public static boolean[][] getLockedArray() {
        return lockedArray;
    }

    public static void swapLockedArrayValue(int i, int j) {
        lockedArray[i][j] = !lockedArray[i][j];
    }
}
