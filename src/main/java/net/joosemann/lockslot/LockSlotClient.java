package net.joosemann.lockslot;

import net.fabricmc.api.ClientModInitializer;
import net.joosemann.lockslot.client.HotkeyManager;
import net.joosemann.lockslot.networking.handlers.ClientNetworkHandlers;

public class LockSlotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register our keybinds
        HotkeyManager.registerKeybinds();

        // Register our client-side networking handlers
        ClientNetworkHandlers.registerClientReceivers();
    }
}
