package net.joosemann.lockslot.networking.handlers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.networking.packets.AskForDataS2CPayload;
import net.joosemann.lockslot.networking.packets.LockDataPayload;
import net.joosemann.lockslot.util.HelperMethods;

public class ClientNetworkHandlers extends LockedValues {
    public static void registerClientReceivers() {
        // UpdateLockData Packet
        // Received when the server finds a data attachment of lock data on the player,
        // so that we can update it client-side as well.
        ClientPlayNetworking.registerGlobalReceiver(LockDataPayload.ID, ((payload, context) -> {
            // Update our lock data on the client side
            HelperMethods.updateLockData(payload.lockedList());
        }));

        // AskForData Packet
        // Received when the server needs the client's data via LockedValues.lockedList. Send it back here
        ClientPlayNetworking.registerGlobalReceiver(AskForDataS2CPayload.ID, ((dummy, context) -> {
            // Send back our client-side data to the server here
            LockDataPayload payload = new LockDataPayload(LockedValues.lockedList);
            ClientPlayNetworking.send(payload);
        }));
    }
}
