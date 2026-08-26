package net.joosemann.lockslot.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.joosemann.lockslot.data.LockedValues;
import net.joosemann.lockslot.data.ServerLockedValues;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    @Final
    public Player player;

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
            player.displayClientMessage(Component.literal("Slot is locked!"), false);
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
}
