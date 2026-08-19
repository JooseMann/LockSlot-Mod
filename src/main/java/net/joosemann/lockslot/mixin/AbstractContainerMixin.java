package net.joosemann.lockslot.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.joosemann.lockslot.LockSlot;
import net.joosemann.lockslot.client.HotkeyManager;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.items.LockedIndicatorItem;
import net.joosemann.lockslot.networking.packets.PlayerDataPayload;
import net.joosemann.lockslot.networking.packets.LockInstancePayload;
import net.joosemann.lockslot.util.LockInstance;
import net.joosemann.lockslot.util.PacketData;
import net.joosemann.lockslot.util.PlayerLockData;
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

    @Unique
    private int numLocksToggled = 0;

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
            // Only prevent the mouse click if the slot is locked
            // This doubles as preventing hotkeys controlled by the mouse, if the slot is locked
            // TODO: Make sure this is consistent across different screens
            if (LockedValues.determineSlotLockStatus(slot, this.leftPos, this.topPos)) {
                if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
                cir.setReturnValue(false);
            }
        }

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z"), method = "keyPressed", cancellable = true)
    private void keyPressEvent(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {

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
                // TODO: Make sure this is consistent across different screens
                if (LockedValues.determineSlotLockStatus(slot, this.leftPos, this.topPos)) {
                    if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(Component.literal("Slot is locked!"), false);
                    cir.setReturnValue(false);
                }
            }
        }
        // Locking slots mechanism
        // Like with keyDrop above, we wrap our custom keybind in InputConstants.getKey() to get the key's numerical code.
        else if (keyEvent.input() == InputConstants.getKey(HotkeyManager.getLockKeybind().saveString()).getValue()) {

            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

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
            else {
                int row = (slot.y - 84) / 18;
                int col = (slot.x - 8) / 18;

                // isLocked is true if the slot is newly locked
                boolean isLocked = LockedValues.swapLockedArrayValue(row, col);

                // Given that we must be in the inventory to get here, we can safely take
                // reference to the top-left slot as index 9 (as in the inventory), without
                // worrying about different containers having different slot indexes.
                Slot topLeftSlot = player.inventoryMenu.getSlot(9);

                // Everything will be in reference to the top left slot.
                // So, subtract the index, x, and y values respectively to base it on the top left slot.
                int adjustedIndex = slot.index - topLeftSlot.index;
                int adjustedX = slot.x - topLeftSlot.x;
                int adjustedY = slot.y - topLeftSlot.y;

                // Push the now locked slot to the list of locked slots,
                // or pop it from the list, depending on whether the slot is now locked or not.
                // Also send a packet to the server so that it knows of the updated state
                if (isLocked) {
                    LockInstance lock = new LockInstance(adjustedIndex, adjustedX, adjustedY);

                    LockedValues.pushLockedSlot(lock);

                    LockInstancePayload packet = new LockInstancePayload(lock, PacketData.PacketUse.LOCKING);
                    ClientPlayNetworking.send(packet);
                }
                else {
                    LockInstance lock = new LockInstance(adjustedIndex, adjustedX, adjustedY);

                    int rc = LockedValues.popLockedSlot(lock);

                    // Make sure it was successfully popped
                    // Display a console error if not
                    if (rc == -1) {
                        LockSlot.LOGGER.error("ERROR: Slot with item {} and (x, y) ({}, {}) was not popped from the list!", slot.getItem(), slot.x, slot.y);
                    }

                    LockInstancePayload packet = new LockInstancePayload(lock, PacketData.PacketUse.UNLOCKING);
                    ClientPlayNetworking.send(packet);
                }

                String s = "Clicked Slot's Position: (" + row + ", " + col + "), index:" + slot.index + "\n";
                s += "After swapping, this slot is currently " + (LockedValues.getLockedValue(row, col) ? "" : "NOT ") + "locked.";

                LockSlot.LOGGER.info(s);

                // Play a sound effect when locking the slot
                Minecraft client = Minecraft.getInstance();
                SoundInstance sound = SimpleSoundInstance.forUI(SoundEvents.AMETHYST_CLUSTER_HIT, 1.0f, 1.0f);
                client.getSoundManager().play(sound);

                // Increment the number of total slots we've toggled
                ++this.numLocksToggled;

                // After a certain amount of locks activated (10 here),
                // send a packet to make sure we haven't desynced from the server.
                if (this.numLocksToggled % 10 == 0) {
                    // Get all of our lock data as is
                    PlayerLockData playerData = new PlayerLockData(player.getStringUUID(), LockedValues.getLockedList());

                    // Write as a packet
                    PlayerDataPayload payload = new PlayerDataPayload(playerData, PacketData.PacketUse.CHECK_DESYNC);

                    // Send the packet to the server
                    ClientPlayNetworking.send(payload);
                }
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

            // Render the locked icon itself
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LockedValues.getLockRenderingId(), x, y, 0, 0, 16, 16, 16, 16);
        }
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
}
