package net.joosemann.lockslot.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.joosemann.lockslot.data.LockedValues;
import net.minecraft.core.NonNullList;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    // (Client-side) Prevent an item from dropping from the hotbar using the Q key if that slot is locked.
    @Environment(EnvType.CLIENT)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"), method = "removeFromSelected", cancellable = true)
    private void onClientItemDrop(boolean bl, CallbackInfoReturnable<ItemStack> cir) {
        // Here, slotIndex only holds which value of the hotbar we're using (values 0 - 8).
        // We can treat it as the "column" with the last row (index 3) of the inventory, which is just the hotbar.
        int slotIndex = player.getInventory().getSelectedSlot();

        // Check if this value is locked on the client
        if (LockedValues.getLockedValue(3, slotIndex)) {
            // Slot is locked, tell the player and prevent the interaction.
            player.displayClientMessage(Component.literal("Slot is locked!"), true);
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
        }
    }

    // (Server-side) Prevent dropping items from the hotbar if the corresponding slot is locked.
    @Environment(EnvType.SERVER)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"), method = "removeFromSelected", cancellable = true)
    private void onServerItemDrop(boolean bl, CallbackInfoReturnable<ItemStack> cir) {
        // This is used with the same reasoning as above: it gets the hotbar value as an index,
        // which we can give to ServerLockedValues to check if that slot is locked or not.
        int slotIndex = player.getInventory().getSelectedSlot();

        // Check if the slot is locked on the server
        Optional<Boolean> res = ServerLockedValues.getLockedMapArrayValue(player.getUUID(), 3, slotIndex);

        if (res.isPresent() && res.get()) {
            // Slot is locked, prevent the interaction.
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
        }
    }

    // (Client-side) Prevent items from being picked up in an empty locked slot.
    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(at = @At(value = "RETURN"), method = "getFreeSlot")
    private int getClientsideNonLockedFreeSlot(int original) {
        return this.getOpenSlot(original, EnvType.CLIENT);
    }

    // (Server-side) Prevent items from being picked up in an empty locked slot.
    @Environment(EnvType.SERVER)
    @ModifyReturnValue(at = @At(value = "RETURN"), method = "getFreeSlot")
    private int getServersideNonLockedFreeSlot(int original) {
        return this.getOpenSlot(original, EnvType.SERVER);
    }

    @Unique
    private int getOpenSlot(int original, EnvType env) {
        // If no slot was found originally, then we won't find one with stricter conditions.
        // Return -1 to signal "no valid slot found".
        if (original == -1) return -1;

        // Get our row and column.
        // The inventory measures indexes differently than our lock data: here, the hotbar is first, then the
        // rest of the rows top-to-bottom. Our data goes top-to-bottom the whole way, so we need to adjust the row.
        int row = original / 9 - 1; // - 1 shifts all rows but the hotbar (= -1) into the right spot
        int col = original % 9;

        // Then correct the hotbar case (row == -1 -> row = 3)
        if (row == -1) row = 3;

        // Check if this slot is unlocked, in which case we can use the original value.
        if (env.equals(EnvType.CLIENT)) { // Check with Client-side data
            if (!LockedValues.getLockedValue(row, col)) return original;
        }
        else { // Check with Server-side data
            Optional<Boolean> isLocked = ServerLockedValues.getLockedMapArrayValue(player.getUUID(), row, col);
            if (isLocked.isPresent() && !isLocked.get()) return original;
        }

        // Otherwise, this slot is locked, and we need to find another unlocked slot.
        for (int i = original + 1; i < this.items.size(); ++i) {
            // Update row and col
            row = i >= 9 ? i / 9 - 1 : 3; // Same logic as above: i / 9 - 1 if i / 9 > 0, otherwise 3
            col = i % 9;
            // Find another slot is that both empty and unlocked
            if (this.items.get(i).isEmpty()) {
                // Check client-side case
                if (env.equals(EnvType.CLIENT)) {
                    if (!LockedValues.getLockedValue(row, col)) return i;
                }
                // Check server-side case
                else {
                    Optional<Boolean> isLocked = ServerLockedValues.getLockedMapArrayValue(player.getUUID(), row, col);
                    if (isLocked.isPresent() && !isLocked.get()) return i;
                }
            }
        }

        // We couldn't find any other unlocked slot, so prevent the item from being picked up at all.
        // We do this by returning -1, signaling "no valid slot found".
        return -1;
    }
}