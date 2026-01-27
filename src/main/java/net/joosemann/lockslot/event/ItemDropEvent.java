package net.joosemann.lockslot.event;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.world.InteractionResult;

public class ItemDropEvent {

    private static InteractionResult handleLogicDrop() {
        LockSlot.LOGGER.info("In handleLogicDrop()");

        return InteractionResult.FAIL;
    }

    public static void registerItemDropEvent() {
        ItemDropCallback.EVENT.register(((player, item) -> {
            return handleLogicDrop();
        }));
    }
}
