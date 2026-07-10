package net.joosemann.lockslot;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.joosemann.lockslot.event.ItemDropEvent;
import net.joosemann.lockslot.event.PlayerLoginEvent;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.joosemann.lockslot.networking.handlers.ServerNetworkHandlers;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.networking.packets.LockInstancePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockSlot implements ModInitializer {
	public static final String MOD_ID = "lock-slot";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		// Register custom item
		LockedIndicatorItem.initialize();

		// Register events

		ItemDropEvent.registerItemDropEvent();
		ServerPlayConnectionEvents.JOIN.register(new PlayerLoginEvent());

		// Register custom networking packets

		// Register sending a list of lock data S2C, so that we can send all of our persistent data at once when needed.
		PayloadTypeRegistry.playS2C().register(LockDataPayload.ID, LockDataPayload.STREAM_CODEC);

		// Register an individual LockInstance packet as C2S, to send whenever the client updates a lock.
		PayloadTypeRegistry.playC2S().register(LockInstancePayload.ID, LockInstancePayload.STREAM_CODEC);

		// Register server-side networking handlers
		ServerNetworkHandlers.registerServerReceivers();
	}
}