package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.util.HelperMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Shadow
    public abstract ItemStack getCarried();

    @Inject(at = @At(value = "HEAD"), method = "doClick", cancellable = true)
    private void preventItemSwap(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
        // Check when clicking in the inventory if we are swapping two items around (one carried, one in a slot).
        // If the involved slot is locked, then prevent the items from swapping.

        // Current menu
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        // Check to make sure our index i is valid (may not be if not clicking on a slot).
        if (i < 0 || i > menu.slots.size()) return;

        // Base slot
        Slot baseSlot = menu.getSlot(i);

        if (baseSlot == null) return;

        // Items involved, both should be non-empty if trying to swap
        ItemStack baseItem = baseSlot.getItem();
        ItemStack carriedItem = this.getCarried();

        // Check if we are clicking on an item with another item carried (normally, swapping the items)
        if (clickType == ClickType.PICKUP && !baseItem.isEmpty() && !carriedItem.isEmpty()) {
            // Make sure that we don't have 2 of the same item. In that case, we are adding items together in a stack,
            // and do not want to run code to treat it as if we were swapping them (handled elsewhere).
            if (baseItem.is(carriedItem.getItem())) return;

            // If so, check if the given slot is locked. Prevent the items from being swapped if so.

            // Standardized index for this slot (top-left is always 0)
            int topLeftIndex = HelperMethods.getTopLeftSlotIndex((AbstractContainerScreen<?>) Minecraft.getInstance().screen);
            int standardizedIndex = i - topLeftIndex;

            // Helper variables
            int row = standardizedIndex / 9;
            boolean alwaysShow = row >= 0 && row <= 3;

            // Now determine if this slot is locked
            if (LockedValues.determineSlotLockStatus(standardizedIndex, alwaysShow, menu instanceof InventoryMenu)) {
                // Slot is locked, prevent the item swap.
                if (player != null) player.displayClientMessage(Component.literal("Slot is locked!"), true);
                ci.cancel();
            }
        }
    }
}
