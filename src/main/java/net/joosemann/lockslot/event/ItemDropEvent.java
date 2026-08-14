package net.joosemann.lockslot.event;

import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.Optional;

public class ItemDropEvent {

    public static void registerItemDropEvent() {
        ItemDropCallback.EVENT.register(((player, item) -> {
            // Here, slotIndex only holds which value of the hotbar we're using (values 0 - 8).
            // We can treat it as the "column" with the last row of the inventory, which is just the hotbar.
            int slotIndex = player.getInventory().getSelectedSlot();

            // Check client-side interactions first.
            if (player.level().isClientSide() && LockedValues.getLockedValue(3, slotIndex)) {
                // Slot is locked, prevent the interaction.
                player.displayClientMessage(Component.literal("Slot is locked!"), false);
                return InteractionResult.FAIL;
            }
            // Then check server-side interactions.
            else if (!player.level().isClientSide()) {
                // Check server-side if the slot is locked.
                Optional<Boolean> res = ServerLockedValues.getLockedMapArrayValue(player.getUUID(), 3, slotIndex);
                if (res.isPresent() && res.get()) {
                    // Slot is locked, prevent the interaction.
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.PASS;
        }));
    }
}
