package net.joosemann.lockslot.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.event.custom.ItemDropCallback;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    @Final
    public Player player;

    @Shadow
    public abstract ItemStack getSelectedItem();

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    // Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"), method = "removeFromSelected", cancellable = true)
    private void onItemDrop(boolean bl, CallbackInfoReturnable<ItemStack> cir) {
        InteractionResult result = ItemDropCallback.EVENT.invoker().interact(this.player, this.getSelectedItem());

        if (result == InteractionResult.FAIL) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    // Prevent items from being picked up in an empty locked slot.
    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(at = @At(value = "RETURN"), method = "getFreeSlot")
    private int getNonLockedFreeSlot(int original) {

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
        if (!LockedValues.getLockedValue(row, col)) return original;

        // Otherwise, this slot is locked, and we need to find another unlocked slot.
        for (int i = original + 1; i < this.items.size(); ++i) {
            // Update row and col
            row = i >= 9 ? i / 9 - 1 : 3; // Same logic as above: i / 9 - 1 if i / 9 > 0, otherwise 3
            col = i % 9;
            // Find another slot is that both empty and unlocked
            if (this.items.get(i).isEmpty() && !LockedValues.getLockedValue(row, col)) {
                return i;
            }
        }

        // We couldn't find any other unlocked slot, so prevent the item from being picked up at all.
        // We do this by returning -1, signaling "no valid slot found".
        return -1;
    }
}
