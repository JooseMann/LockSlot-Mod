package net.joosemann.lockslot.modmenu.screens;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Simple utility class to handle the mod's configuration.<br>
 * Uses Cloth Config as a dependency.
 */
@Environment(EnvType.CLIENT)
public class LockSlotConfigScreen {

    private static boolean SHOULD_LOCK_PICKUPS = true;

    /**
     * Creates a simple configuration screen for LockSlot.
     *
     * @param parent The parent screen to the newly created screen.
     * @return A screen for LockSlot's configuration.
     */
    public static Screen createConfigScreen(Screen parent) {
        // Create the overall config screen builder
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.lock-slot.config"));

        // Dummy category to list everything under (with only one category, the name is ignored).
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("category.lock-slot.config"));

        // Create a config entry for locking item pickups
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        String lockPickupsTooltip = """
                Toggle whether empty locked slots should pick up items or not.
                Yes -> Items are NOT picked up in empty locked slots.
                No -> Items are picked up as normal (locking a slot has no effect on item pickups).""";

        // Configure it and add it to the category
        category.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("lock_pickups.lock-slot.config"), SHOULD_LOCK_PICKUPS)
                .setDefaultValue(true)
                .setTooltip(Component.literal(lockPickupsTooltip))
                .setSaveConsumer(newValue -> SHOULD_LOCK_PICKUPS = newValue)
                .build());

        return builder.build();
    }
}
