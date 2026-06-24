package net.joosemann.lockslot;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.joosemann.lockslot.event.PlayerLoginEvent;
import net.joosemann.lockslot.event.ItemDropEvent;
import net.joosemann.lockslot.event.TooltipUpdateEvent;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.joosemann.lockslot.networking.handlers.ServerNetworkHandlers;
import net.joosemann.lockslot.networking.packets.AskForDataS2CPayload;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
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
		ItemTooltipCallback.EVENT.register(new TooltipUpdateEvent());
		ServerPlayConnectionEvents.JOIN.register(new PlayerLoginEvent());

		// Register custom networking packets

		// Register our "ask client for data" packet as S2C
		PayloadTypeRegistry.playS2C().register(AskForDataS2CPayload.ID, AskForDataS2CPayload.STREAM_CODEC);

		// Register sending lock data to both S2C and C2S, as we will be sending it back and forth.
		PayloadTypeRegistry.playS2C().register(LockDataPayload.ID, LockDataPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(LockDataPayload.ID, LockDataPayload.STREAM_CODEC);

		// Register server-side networking handlers
		ServerNetworkHandlers.registerServerReceivers();
	}
}