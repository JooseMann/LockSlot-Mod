package net.joosemann.lockslot.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.client.HotkeyManager;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.joosemann.lockslot.util.LockInstance;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ListIterator;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;"), method = "mouseClicked", cancellable = true)
    private void preventInventoryMouseClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {

        LocalPlayer player = Minecraft.getInstance().player;

        // If we are in creative or spectator mode, don't prevent any mouse clicks.
        if (player != null && player.gameMode() != null) {
            GameType gameMode = player.gameMode();
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
            // Helper variables
            int adjustedIndex = calculateAdjustedIndex(slot);
            boolean alwaysShow = adjustedIndex >= 0 && adjustedIndex <= 35;
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

            // Only prevent the mouse click if the slot is locked
            // TODO: Make sure this is consistent across different screens
            if (LockedValues.determineSlotLockStatus(adjustedIndex, alwaysShow, screen instanceof InventoryScreen)) {
                if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
                cir.setReturnValue(false);
            }
        }

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"), method = "keyPressed", cancellable = true)
    private void keyPressEvent(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        // If the player tries to drop an item, make sure that the slot is not locked
        // If it is, prevent the item from being thrown.
        // Note: .options.keyDrop is the "dropping item" key, wrapping it in InputConstants.getKey() allows us
        // to get the keybind's numerical code to compare with the given keyEvent.
        if (keyEvent.input() == InputConstants.getKey(Minecraft.getInstance().options.keyDrop.saveString()).getValue()) {
            // Treat this like it's a click.

            LocalPlayer player = Minecraft.getInstance().player;

            // Don't prevent throwing items if the player is in creative or spectator mode (functionality is disabled)
            if (player != null && player.gameMode() != null) {
                GameType gameMode = player.gameMode();
                if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
            }

            Slot slot = this.hoveredSlot;
            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Null slot can not be thrown!");
                cir.setReturnValue(false);
            }
            else {
                // Helper variables
                int adjustedIndex = calculateAdjustedIndex(slot);
                boolean alwaysShow = adjustedIndex >= 0 && adjustedIndex <= 35;

                // TODO: Make sure this is consistent across different screens
                if (LockedValues.determineSlotLockStatus(adjustedIndex, alwaysShow, screen instanceof InventoryScreen)) {
                    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
                    cir.setReturnValue(false);
                }
            }
        }
        // Locking slots mechanism
        // Like with keyDrop above, we wrap our custom keybind in InputConstants.getKey() to get the key's numerical code.
        else if (keyEvent.input() == InputConstants.getKey(HotkeyManager.getLockKeybind().saveString()).getValue()) {

            // Only allow locking slots in the inventory
            if (!(screen instanceof InventoryScreen)) return;

            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null) return;

            // If the player is in creative or spectator mode, don't allow them to lock slots (the feature is disabled)
            if (player.gameMode() != null) {
                GameType gameMode = player.gameMode();
                if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
            }

            Slot slot = this.hoveredSlot;

            if (slot == null) {
                LockSlot.LOGGER.warn("WARNING: Attempting to lock a null slot!");
            }
            else if (slot.isFake()) { // Disallow "fake" slots (such as the result crafting inventory slot)
                return;
            }
            else {
                // Given that we must be in the inventory to get here, we can safely take
                // reference to the top-left slot as index 9 (as in the inventory), without
                // worrying about different containers having different slot indexes.
                Slot topLeftSlot = player.inventoryMenu.getSlot(9);

                // Everything will be in reference to the top left slot.
                // So, subtract the index, x, and y values respectively to base it on the top left slot.
                int adjustedIndex = slot.index - topLeftSlot.index;
                int adjustedX = slot.x - topLeftSlot.x;
                int adjustedY = slot.y - topLeftSlot.y;

                // Row and column of the slot in the inventory
                int row = adjustedIndex / 9;
                int col = adjustedIndex % 9;

                // Boolean determining if this slot should always be shown or not.
                // Our core inventory slots make up rows 0 - 3, with row 4 as an "extras" row.
                // We don't always want to show the extras row, but do with the core inventory.
                boolean alwaysEnabled = row >= 0 && row <= 3;

                // Check if adjustedIndex < 0. In that case, we are trying to lock something other than
                // the core inventory (e.g., armor) and need to shift the row and col values accordingly.
                if (adjustedIndex < 0) {
                    row = 4; // Additional row for armor, offhand, etc.
                    col = (adjustedIndex + 9) % 9; // Offset the adjusted index by 9 as to get positive values from % 9
                    alwaysEnabled = false; // Update alwaysEnabled
                }

                // isLocked reflects the updated value for this slot (i.e., now locked -> true)
                boolean isLocked = LockedValues.swapLockedArrayValue(row, col);

                // Push the now locked slot to the list of locked slots,
                // or pop it from the list, depending on whether the slot is now locked or not.
                if (isLocked) {
                    LockedValues.pushLockedSlot(new LockInstance(adjustedIndex, adjustedX, adjustedY, alwaysEnabled));
                }
                else {
                    int rc = LockedValues.popLockedSlot(new LockInstance(adjustedIndex, adjustedX, adjustedY, alwaysEnabled));

                    // Make sure it was successfully popped
                    // Display a console error if not
                    if (rc == -1) {
                        LockSlot.LOGGER.error("ERROR: Slot with item {} and (x, y) ({}, {}) was not popped from the list!", slot.getItem(), slot.x, slot.y);
                    }
                }

                // Play a sound effect when locking the slot
                Minecraft client = Minecraft.getInstance();
                SoundInstance sound = SimpleSoundInstance.forUI(SoundEvents.AMETHYST_CLUSTER_HIT, 1.0f, 1.0f);
                client.getSoundManager().play(sound);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V"), method = "checkHotbarKeyPressed", cancellable = true)
    protected void hotbarKeyPressMixin(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        // Here, a key press triggered a hotkey to or from a hotbar slot.
        // We need to check and make sure that both the slot hovered over and the corresponding hotbar slot are *not* locked.

        boolean canPress = this.allowHotkeyPress(keyEvent.input());
        if (!canPress) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V"), method = "checkHotbarMouseClicked", cancellable = true)
    protected void hotbarMouseClickMixin(MouseButtonEvent mouseButtonEvent, CallbackInfo ci) {
        // Like with hotbarKeyPressMixin above, this triggers when a mouse hotkey is pressed.
        // We need to make sure that slots are not locked before we let this key press go through.

        boolean canPress = this.allowHotkeyPress(mouseButtonEvent.input());
        if (!canPress) ci.cancel();
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

        LocalPlayer player = Minecraft.getInstance().player;

        // Don't try to render if the player is in creative or spectator mode (when the functionality is disabled)
        if (player != null && player.gameMode() != null) {
            GameType gameMode = player.gameMode();
            if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) return;
        }

        ListIterator<LockInstance> itr = LockedValues.getLockedListIterator();
        LockInstance lock;
        Slot referenceSlot = screen.getMenu().getSlot(this.getTopLeftSlotIndex());

        while (itr.hasNext()) {
            if (itr.previousIndex() == -1) {
                // At the start of the list, make sure to count the first element
                itr.next();
                lock = itr.previous();
                itr.next();
            }
            else {
                lock = itr.next();
            }

            // X and Y position for every slot, offset by the position of the reference (top left) slot and the top left of the container.
            // (Note: we need the leftPos and topPos because referenceSlot is still in reference to the container, not the window)
            x = referenceSlot.x + lock.x() + this.leftPos;
            y = referenceSlot.y + lock.y() + this.topPos;

            // Some locks should not always be shown (e.g., armor slots)
            // We only show these slots in the inventory, as that is the only place we will see them
            // TODO: Verify this!
            if (!lock.alwaysEnabled() && !(screen instanceof InventoryScreen)) continue;

            // Render the locked icon itself
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LockedValues.getLockRenderingId(), x, y, 0, 0, 16, 16, 16, 16);
        }
    }

    // Determines whether a hotkey can go through or not, based only on the key's index.
    // This abstracts away from keyboard presses vs. mouse clicks, as the underlying logic is the same.
    @Unique
    private boolean allowHotkeyPress(int keyIndex) {
        // Narrow out edge cases: if the slot we are hotkeying to is null, then just return false.
        if (this.hoveredSlot == null) return false;

        // Declare some useful variables
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        KeyMapping keySwapOffhand = Minecraft.getInstance().options.keySwapOffhand;

        // Figure out which hotbar index we are looking at

        KeyMapping correspondingKey = null;

        // Check for 1-9 hotkeys
        for (KeyMapping key : Minecraft.getInstance().options.keyHotbarSlots) {
            // Check for the same ASCII code -> same key
            if (keyIndex == InputConstants.getKey(key.saveString()).getValue()) {
                correspondingKey = key;
                break;
            }
        }

        // Check for offhand hotkey as well
        if (keyIndex == InputConstants.getKey(keySwapOffhand.saveString()).getValue()) {
            correspondingKey = keySwapOffhand;
        }

        if (correspondingKey == null) {
            // Could not find corresponding hotbar key, send a warning and stop.
            // Note: keyIndex holds the ASCII code, so casting to char just gives the corresponding character.
            LockSlot.LOGGER.warn("Could not find hotbar key corresponding to key press {}.", (char) keyIndex);
            return false;
        }

        // Index in the hotbar. The default key is the corresponding index as a string (starting from "1"),
        // and it's value is it's ASCII number (1 is 49, 2 is 50, 3 is 51, ..., 9 is 57).
        // So we offset the value by 49 to get indexes 0 through 8 (inclusive).
        // We then add back 27 to make this index refer to the hotbar, instead of the top row of the inventory.
        int hotbarIndex = (correspondingKey.getDefaultKey().getValue() - 49) + 27;

        // This logic does not hold if we are not dealing with the hotbar (i.e., we are dealing with the offhand).
        // Change that index accordingly.
        if (correspondingKey.equals(keySwapOffhand)) {
            hotbarIndex = 36; // Index of the offhand's slot (row 4, column 0).
        }

        // Check if the hotbar slot itself is locked.
        if (LockedValues.determineSlotLockStatus(hotbarIndex)) {
            // The hotbar slot here is locked, we need to prevent the hotkey.
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
            return false;
        }

        // Now check if the other slot we want to hotkey to is locked.
        // This slot will always be our hovered slot, so we base our calculations off of that.
        int standardizedIndex = this.hoveredSlot.index - this.getTopLeftSlotIndex();

        // Create some helper variables for determining if this slot is locked.
        int row = standardizedIndex / 9; // Row that the slot shows up on.
        boolean alwaysShow = row >= 0 && row <= 3; // Whether this slot should always show or not.

        // Now check if this slot is locked.
        // Note that we could be dealing with slots where alwaysShow is false here, so we need to explicitly check that.
        if (LockedValues.determineSlotLockStatus(standardizedIndex, alwaysShow, screen instanceof InventoryScreen)) {
            // The hovered slot is locked, so we need to prevent this key press.
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
            return false;
        }

        // If neither slot is locked, then the key press can go through. Return true to indicate this.
        return true;
    }

    @Unique
    private int getTopLeftSlotIndex() {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        int tryReferenceIndex = LockedValues.tryGetReferenceSlotIndex(screen);

        // We likely already computed this value in the reference slot cache.
        // If that's the case, then take the index from there instead of recalculating it.
        // Note: this function returns -1 if we have not already found it, and otherwise gives a positive integer index.
        if (tryReferenceIndex != -1) {
            return tryReferenceIndex;
        }

        // If not, then we have to manually compute it. Do that here.

        // Error check, -1 for invalid state
        if (Minecraft.getInstance().player == null) return -1;

        // The top-left slot of the inventory has index 9. Find the equivalent slot for this container
        Slot base = Minecraft.getInstance().player.inventoryMenu.getSlot(9);

        // Store the current item. We will have to overwrite it with a custom item temporarily,
        // so that we can tell which slot is the same across different screens.
        ItemStack prevItem = base.getItem();

        base.set(LockedIndicatorItem.ITEM.getDefaultInstance());

        for (Slot slot : screen.getMenu().slots) {
            // Slots will be effectively equivalent in everything except index and container
            // So, check the rest of the traits to make sure they are all equal
            if (slot.getItem().equals(base.getItem())) {
                // We found the correct slot
                // Return the slot back to its original state
                base.set(prevItem);

                // Add the new index to our reference cache, so that we don't have to recalculate this later.
                LockedValues.addReferenceSlot(screen, slot.index);

                // Return the index with respect to *this*, instead of the inventory
                return slot.index;
            }
        }

        // Not found, return -1 for error
        return -1;
    }

    @Unique
    private int calculateAdjustedIndex(Slot slot) {
        if (Minecraft.getInstance().player == null) return -1;

        // Calculate our slot index relative to the top-left inventory slot (index 9).
        return slot.index - getTopLeftSlotIndex();
    }
}
