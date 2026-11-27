package net.joosemann.lockslot;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.joosemann.lockslot.event.PlayerLoginEvent;
import net.joosemann.lockslot.event.RegisterServerCommandEvent;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.joosemann.lockslot.networking.handlers.ServerNetworkHandlers;
import net.joosemann.lockslot.networking.packets.ModConfigPayload;
import net.joosemann.lockslot.networking.packets.PlayerDataPayload;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.networking.packets.LockInstancePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockSlot implements ModInitializer {
	public static final String MOD_ID = "lock-slot";

	/**
	 * Logger for the LockSlot mod. Whenever information needs to be logged,
	 * the mod uses this instead of {@code System.out.println()} to do so. That
	 * way, the user can see easily which mod the logging information is from.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Note: In a mod-load-ready state, so some resources and other things may be uninitialized

		/* Register Mod Classes */

		// Register custom item
		LockedIndicatorItem.initialize();

		// Register events
		CommandRegistrationCallback.EVENT.register(new RegisterServerCommandEvent());
		ServerPlayConnectionEvents.JOIN.register(new PlayerLoginEvent());

		/* Register networking packets & handlers */

		// Register sending a list of lock data S2C, so that we can send all of our persistent data at once when needed.
		PayloadTypeRegistry.playS2C().register(LockDataPayload.ID, LockDataPayload.STREAM_CODEC);

		// Register an individual LockInstance packet as C2S, to send whenever the client updates a lock.
		PayloadTypeRegistry.playC2S().register(LockInstancePayload.ID, LockInstancePayload.STREAM_CODEC);

		// Register a data packet for a single player C2S that checks for and prevents desyncs.
		PayloadTypeRegistry.playC2S().register(PlayerDataPayload.ID, PlayerDataPayload.STREAM_CODEC);

		// Register a config update packet, for when a client-side config is changed in a world.
		// This must be sent back and forth between client and server, so register it as both C2S and S2C.
		PayloadTypeRegistry.playC2S().register(ModConfigPayload.ID, ModConfigPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ModConfigPayload.ID, ModConfigPayload.STREAM_CODEC);

		// Register server-side networking handlers
		ServerNetworkHandlers.registerServerReceivers();

		/* Send a "Mod Loaded!" Log Message */
		LockSlot.LOGGER.info("LockSlot Mod Loaded!");
	}
}