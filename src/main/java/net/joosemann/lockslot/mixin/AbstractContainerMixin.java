package net.joosemann.lockslot.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.client.HotkeyManager;
import net.joosemann.lockslot.client.LockedValues;
import net.joosemann.lockslot.event.ItemDropEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ListIterator;
import java.util.Objects;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"), method = "mouseClicked", cancellable = true)
    private void preventInventoryMouseClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {

        // If we are in creative or spectator mode, don't prevent any mouse clicks.
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.gameMode() != null) {
            GameType gameMode = Minecraft.getInstance().player.gameMode();
            if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
        }

        Slot slot = this.hoveredSlot;

        // The slot can be null if it is in a part of the inventory that is not a slot,
        // or if outside the inventory is clicked.
        if (slot == null) {
            LockSlot.LOGGER.warn("WARNING: Null slot can not be clicked!");

            cir.setReturnValue(false);
        }
        else {
            // Only prevent the mouse click if the slot is locked
            // TODO: Make sure this is consistent across different screens
            if (ItemDropEvent.determineSlotLockStatus(slot)) {
                cir.setReturnValue(false);
            }
        }

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"), method = "keyPressed", cancellable = true)
    private void LockSlotEvent(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

        // If the player tries to drop an item, make sure that the slot is not locked
        // If it is, prevent the item from being thrown.
        // Note: .options.keyDrop is the "dropping item" key, wrapping it in InputConstants.getKey() allows us
        // to get the keybind's numerical code to compare with the given keyEvent.
        if (keyEvent.input() == InputConstants.getKey(Minecraft.getInstance().options.keyDrop.saveString()).getValue()) {
            // Treat this like it's a click.

            // Don't prevent throwing items if the player is in creative or spectator mode (functionality is disabled)
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.gameMode() != null) {
                GameType gameMode = Minecraft.getInstance().player.gameMode();
                if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
            }

            Slot slot = this.hoveredSlot;
            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Null slot can not be thrown!");
                cir.setReturnValue(false);
            }
            else {
                // TODO: Make sure this is consistent across different screens
                if (ItemDropEvent.determineSlotLockStatus(slot)) {
                    cir.setReturnValue(false);
                }
            }
        }
        // Locking slots mechanism
        // Like with keyDrop above, we wrap our custom keybind in InputConstants.getKey() to get the key's numerical code.
        else if (keyEvent.input() == InputConstants.getKey(HotkeyManager.getLockKeybind().saveString()).getValue()) {
            // If the player is in creative or spectator mode, don't allow them to lock slots (the feature is disabled)
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.gameMode() != null) {
                GameType gameMode = Minecraft.getInstance().player.gameMode();
                if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
            }

            Slot slot = this.hoveredSlot;

            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Attempting to lock a null slot!");
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

                String s = "Clicked Slot's Position: (" + row + ", " + col + "), index:" + slot.index + "\n";
                s += "After swapping, this slot is currently " + (LockedValues.getLockedValue(row, col) ? "" : "NOT ") + "locked.";

                LockSlot.LOGGER.info(s);
            }
        }
    }

    @Inject(at = @At(value = "TAIL"), method = "renderBackground")
    public void renderLocksMixin(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        // Render any slots that are locked

        int x, y;
        // We don't know what type of menu we're rendering at runtime
        // So we use a wildcard cast to get the screen, since screen.getMenu().getSlot(int) still works fine
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        // Don't try to render if there's no screen
        if (Minecraft.getInstance().screen == null) return;

        // Don't try to render if the player is in creative or spectator mode (when the functionality is disabled)
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.gameMode() != null) {
            GameType gameMode = Minecraft.getInstance().player.gameMode();
            if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
        }

        // Coordinates for the top left of the container, to use as reference when rendering locked icons.
        // The values 176 and 166 come from the container's (scaled) texture width and height.
        int leftPos = (screen.width - 176) / 2;
        int topPos = (screen.height - 166) / 2;

        // Some screens have a recipe book menu that offsets the menu.
        // Check if that is the case here, so we can adjust our scaling accordingly.
        if (screen instanceof AbstractRecipeBookScreen<? extends RecipeBookMenu> recipeScreen && Minecraft.getInstance().player != null) {
            RecipeBookType type = recipeScreen.getMenu().getRecipeBookType();
            boolean recipeBookOpen = Minecraft.getInstance().player.getRecipeBook().isOpen(type);

            if (recipeBookOpen) {
                // When the recipe book is open, the "left" of the inventory is close to the center of the screen.
                // This value gives the left position when the recipe book is open.
                leftPos = (screen.width - 22) / 2;
            }
        }

        ListIterator<Slot> itr = LockedValues.getLockedListIterator();
        Slot slot;

        while (itr.hasNext()) {
            if (itr.previousIndex() == -1) {
                // At the start of the list, make sure to count the first element
                itr.next();
                slot = itr.previous();
                itr.next();
            }
            else {
                slot = itr.next();
            }

            // X and Y position for every slot, offset by the position of the container screen.
            x = leftPos + slot.x;
            y = topPos + slot.y;

            // Render the locked icon itself
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LockedValues.getLockRenderingId(), x, y, 0, 0, 16, 16, 16, 16);
        }
    }

}
