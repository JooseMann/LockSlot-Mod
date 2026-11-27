package net.joosemann.lockslot.modmenu.screens;

import com.google.gson.Gson;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.data.LockConfigData;
import net.joosemann.lockslot.networking.packets.ModConfigPayload;
import net.joosemann.lockslot.util.PacketData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple utility class to handle the mod's configuration.<br>
 * Importantly, this does <i>not</i> store the configuration data.
 * This is instead handled by {@link LockConfigData}.<br>
 * Uses Cloth Config as a dependency.
 */
@Environment(EnvType.CLIENT)
public class LockSlotConfigScreen {
    public static final String CONFIG_FILE_PATH = "config/lock-slot-config.json";

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
                .startBooleanToggle(Component.translatable("lock_pickups.lock-slot.config"),
                        LockConfigData.shouldLockPickups())
                .setDefaultValue(true)
                .setTooltip(Component.literal(lockPickupsTooltip))
                .setSaveConsumer(LockConfigData::setShouldLockPickups)
                .build());

        // Specify what to do to save the config. In our case, save it to a .json in writeConfigToFile()
        builder.setSavingRunnable(LockSlotConfigScreen::writeConfigToFile);

        return builder.build();
    }

    // Write our current configuration to disk, so that it is saved for future sessions.
    private static void writeConfigToFile() {
        // Base config
        String lockCfg = """
                {
                    "should_lock_pickups": "%s"
                }
                """;

        // Serialize the data with GSON
        Gson gson = new Gson();
        String data = gson.toJson(LockConfigData.shouldLockPickups());

        // Then add our data to the base config
        lockCfg = String.format(lockCfg, data);

        // Declare the config file, file may or may not exist
        File cfgFile = new File(CONFIG_FILE_PATH);

        // Now try to create the config file (if needed) and write to it.
        try {
            if (!cfgFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cfgFile.createNewFile();
            }

            FileWriter writer = new FileWriter(cfgFile);
            writer.write(lockCfg);
            writer.close();
        } catch (IOException e) {
            // The given file does not exist after it should have been created, or can otherwise not be written to.
            LockSlot.LOGGER.error("Could not write to LockSlot configuration file: " + CONFIG_FILE_PATH);
            LockSlot.LOGGER.error(e.getMessage());
        } finally {
            // If needed, send a packet to the server to update it of the config change
            IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
            // Determine if we are in a multiplayer world (modeled after the private method Minecraft.isMultiplayer())
            if (!Minecraft.getInstance().isLocalServer() || integratedServer != null && integratedServer.isPublished()) {
                // We are on a dedicated server, so the player should be non-null.
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    // Send a C2S packet to inform the server of the config update.
                    ModConfigPayload packet = new ModConfigPayload(LockConfigData.shouldLockPickups(), PacketData.PacketUse.UPDATE_LOCK_DATA);
                    ClientPlayNetworking.send(packet);
                }
            }
        }
    }
}
