package net.joosemann.lockslot.event;

import net.joosemann.lockslot.client.LockedValues;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.Slot;

public class ItemDropEvent {

    private static InteractionResult handleLogicDrop() {
        return InteractionResult.FAIL;
    }

    public static void registerItemDropEvent() {
        ItemDropCallback.EVENT.register(((player, item) -> {
            return handleLogicDrop();
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
