package net.joosemann.lockslot.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * <p>A simple helper class to store configuration data.<br>
 * Data is stored in {@code config/lock-slot-config.json}
 * and is read in at {@code modmenu/LockSlotConfigScreen.java}.</p>
 *
 * <p>As mod configuration is only relevant to individual players,
 * this class is <b>client-side only</b>.</p>
 */
@Environment(EnvType.CLIENT)
public class LockConfigData {
    /* Configuration Values */

    // Determines whether items can be picked up in an empty locked slot.
    // True -> Items are NOT picked up in empty locked slots - Default
    // False -> Items are picked up in empty locked slots (i.e., locked slots have no effect on item pickups)
    private static boolean SHOULD_LOCK_PICKUPS = true;

    /* Getters */

    public static boolean shouldLockPickups() {
        return SHOULD_LOCK_PICKUPS;
    }

    /* Setters */

    public static void setShouldLockPickups(boolean newValue) {
        SHOULD_LOCK_PICKUPS = newValue;
    }
}
