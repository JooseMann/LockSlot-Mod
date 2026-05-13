package net.joosemann.lockslot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.joosemann.lockslot.client.HotkeyManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.chat.Component;

public class LockSlotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HotkeyManager.registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (HotkeyManager.getLockKeybind().consumeClick()) {

                double x = MouseHandler.getScaledXPos(client.getWindow(), 1.0);
                double y = MouseHandler.getScaledYPos(client.getWindow(), 1.0);

                client.player.displayClientMessage(Component.literal(
                        "Slot at coordinate (" + x + ", " + y +  ") locked!"), false);

                LockSlot.LOGGER.info("Slot at coordinate (" + x + ", " + y +  ") locked!");
            }
        });
    }
}
