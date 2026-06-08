package net.joosemann.lockslot.event;

import net.joosemann.lockslot.client.LockedValues;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;

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

    public static boolean determineSlotLockStatus(Slot slot) {
        // TODO: Get the other values for extra slots! (armor, off-hand, and crafting)
        // NOTE: The values from slot.y and slot.x are flipped for row and col,
        // because row corresponds to the y-axis and not the x-axis.
        int row = (slot.y - 84) / 18;
        int col = (slot.x - 8) / 18;

        return LockedValues.getLockedValue(row, col);
    }
}
