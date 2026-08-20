package net.joosemann.lockslot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.joosemann.lockslot.client.HotkeyManager;
import net.joosemann.lockslot.event.RegisterClientCommandEvent;
import net.joosemann.lockslot.event.TooltipUpdateEvent;
import net.joosemann.lockslot.networking.handlers.ClientNetworkHandlers;

public class LockSlotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register our keybinds
        HotkeyManager.registerKeybinds();

        // Register client-side events
        ClientCommandRegistrationCallback.EVENT.register(new RegisterClientCommandEvent());
        ItemTooltipCallback.EVENT.register(new TooltipUpdateEvent());

        // Register our client-side networking handlers
        ClientNetworkHandlers.registerClientReceivers();
    }
}
