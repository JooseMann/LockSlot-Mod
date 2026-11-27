package net.joosemann.lockslot.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>A simple helper class to store configuration data.
 * Data is stored in {@code config/lock-slot-config.json}
 * and is read in at {@code modmenu/LockSlotConfigScreen.java}.</p>
 */
public class LockConfigData {
    /* Configuration Values */

    // Determines whether items can be picked up in an empty locked slot.
    // True -> Items are NOT picked up in empty locked slots - Default
    // False -> Items are picked up in empty locked slots (i.e., locked slots have no effect on item pickups)
    @Environment(EnvType.CLIENT) private static boolean SHOULD_LOCK_PICKUPS = true;
    @Environment(EnvType.SERVER) private static final Map<UUID, Boolean> SHOULD_LOCK_PICKUPS_BY_PLAYER = new HashMap<>();

    /* Getters */

    @Environment(EnvType.CLIENT)
    public static boolean shouldLockPickups() {
        return SHOULD_LOCK_PICKUPS;
    }

    @Environment(EnvType.SERVER)
    public static Optional<Boolean> shouldLockPickupsForPlayer(UUID uuid) {
        if (SHOULD_LOCK_PICKUPS_BY_PLAYER.containsKey(uuid)) {
            return Optional.of(SHOULD_LOCK_PICKUPS_BY_PLAYER.get(uuid));
        }
        return Optional.empty();
    }

    /* Setters */

    @Environment(EnvType.CLIENT)
    public static void setShouldLockPickups(boolean newValue) {
        SHOULD_LOCK_PICKUPS = newValue;
    }

    @Environment(EnvType.SERVER)
    public static void setShouldLockPickupsForPlayer(UUID uuid, boolean newValue) {
        // Note: This overwrites whatever was here previously.
        SHOULD_LOCK_PICKUPS_BY_PLAYER.put(uuid, newValue);
    }
}
