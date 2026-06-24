package net.joosemann.lockslot.event;

import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

public class ItemDropEvent {

    public static void registerItemDropEvent() {
        ItemDropCallback.EVENT.register(((player, item) -> {
            // Here, slotIndex only holds which value of the hotbar we're using (values 0 - 8).
            // We can treat it as the "column" with the last row of the inventory, which is just the hotbar.
            int slotIndex = player.getInventory().getSelectedSlot();

            if (LockedValues.getLockedValue(3, slotIndex)) {
                player.displayClientMessage(Component.literal("Slot is locked!"), false);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        }));
    }
}
