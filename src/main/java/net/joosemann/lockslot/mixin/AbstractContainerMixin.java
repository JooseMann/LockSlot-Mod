package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.client.LockedValues;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerMixin {

    @Shadow
    @Nullable
    protected abstract Slot getHoveredSlot(double d, double e);

    @Shadow
    private @Nullable Slot clickedSlot;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"), method = "mouseClicked", cancellable = true)
    private void preventInventoryMouseClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {

        Slot slot = this.hoveredSlot;

        if (slot == null) {
            LockSlot.LOGGER.info("SLOT IS NULL");

            cir.setReturnValue(false);
        }
        else {
            // TODO: Make sure this works based on the gamemode!
            // This is for survival, the equivalent for creative would be (slot.x - 9) / 18
            // TODO: Get the other values for extra slots! (armor, off-hand, and crafting)
            // NOTE: The values from slot.y and slot.x are flipped for row and col,
            // because row corresponds to the y-axis and not the x-axis.
            int row = (slot.y - 84) / 18;
            // Creative equivalent here is ??? TODO FIGURE THIS OUT
            int col = (slot.x - 8) / 18;

            String s = "Clicked Slot's Position: (" + row + ", " + col + ").";
            s += "It is currently " + (LockedValues.getLockedValue(row, col) ? "" : "NOT ") + "locked.";

            LockSlot.LOGGER.info(s);

             if (LockedValues.getLockedValue(row, col)) {
                // LockSlot.LOGGER.info("Slot is locked!");
                cir.setReturnValue(false);
             }
        }

    }
}
