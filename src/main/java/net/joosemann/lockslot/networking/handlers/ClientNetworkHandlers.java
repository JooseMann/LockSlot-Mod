package net.joosemann.lockslot.networking.handlers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.joosemann.lockslot.data.LockConfigData;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.networking.packets.ModConfigPayload;
import net.joosemann.lockslot.util.ClientHelperMethods;
import net.joosemann.lockslot.util.PacketData;

public class ClientNetworkHandlers extends LockedValues {
    /**
     * Registers client-side network handlers on mod initialization.
     * As this is only relevant for the client, this is handled in
     * the client-side mod initialization.
     */
    public static void registerClientReceivers() {
        // Update Lock Data Packet
        // Received when the server finds a data attachment of lock data on the player,
        // so that we can update it client-side as well.
        ClientPlayNetworking.registerGlobalReceiver(LockDataPayload.ID, (payload, context) -> {
            // Update our lock data on the client side
            ClientHelperMethods.updateLockData(payload.lockedList());
        });

        // Data Request Packet - Config
        // Received by the (dedicated) server, so it knows what config the player is using.
        // The client only needs to send back a packet with the proper data.
        ClientPlayNetworking.registerGlobalReceiver(ModConfigPayload.ID, (payload, context) -> {
            if (payload.packetUse().equals(PacketData.PacketUse.REQUEST_DATA)) {
                // Send the requested data right back.
                ModConfigPayload configPacket = new ModConfigPayload(LockConfigData.shouldLockPickups(), PacketData.PacketUse.UPDATE_CONFIG);
                ClientPlayNetworking.send(configPacket);
            }
        });
    }
}
