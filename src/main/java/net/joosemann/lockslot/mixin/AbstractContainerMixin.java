package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.client.LockedValues;
import net.joosemann.lockslot.event.ItemDropEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"), method = "mouseClicked", cancellable = true)
    private void preventInventoryMouseClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {

        // If we are in creative mode, don't prevent any mouse clicks.
        if (Minecraft.getInstance().player != null &&
                Objects.requireNonNull(Minecraft.getInstance().player.gameMode()).isCreative()) return;

        Slot slot = this.hoveredSlot;

        // The slot can be null if it is in a part of the inventory that is not a slot,
        // or if outside the inventory is clicked.
        if (slot == null) {
            LockSlot.LOGGER.warn("WARNING: Null slot can not be clicked!");

            cir.setReturnValue(false);
        }
        else {
            // Only prevent the mouse click if the slot is locked
            if (ItemDropEvent.determineSlotLockStatus(slot)) {
                cir.setReturnValue(false);
            }
        }

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"), method = "keyPressed", cancellable = true)
    private void LockSlotEvent(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

        // TODO: Allow the keybind to drop an item to be dynamic (so it doesn't have to always be just Q)
        // If the player tries to drop an item, make sure that the slot is not locked
        // If it is, prevent the item from being thrown.
        if (keyEvent.input() == GLFW.GLFW_KEY_Q) {
            // Treat this like it's a click.

            Slot slot = this.hoveredSlot;
            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Null slot can not be thrown!");
                cir.setReturnValue(false);
            }
            else {
                if (ItemDropEvent.determineSlotLockStatus(slot)) {
                    cir.setReturnValue(false);
                }
            }
        }
        else if (keyEvent.input() == GLFW.GLFW_KEY_LEFT_ALT) {
            // TODO: If this is not in the inventory, make sure to return
            // if (!((Object) this instanceof InventoryScreen)) { cir.setReturnValue(true); return; }

            Slot slot = this.hoveredSlot;

            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Attempting to lock a null slot!");

                // cir.setReturnValue(true);
            }
            else {
                int row = (slot.y - 84) / 18;
                int col = (slot.x - 8) / 18;

                // isLocked is true if the slot is newly locked
                boolean isLocked = LockedValues.swapLockedArrayValue(row, col);

                // Push the now locked slot to the list of locked slots,
                // or pop it from the list, depending on whether the slot is now locked or not.
                if (isLocked) {
                    LockedValues.pushLockedSlot(slot);
                }
                else {
                    LockedValues.popLockedSlot(slot);
                }

                String s = "Clicked Slot's Position: (" + row + ", " + col + ").";
                s += "After swapping, this slot is currently " + (LockedValues.getLockedValue(row, col) ? "" : "NOT ") + "locked.";

                LockSlot.LOGGER.info(s);
            }
        }
    }
}
