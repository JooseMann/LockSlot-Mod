package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.event.custom.ItemDropCallback;
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

    // Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"), method = "removeFromSelected", cancellable = true)
    private void onItemDrop(boolean bl, CallbackInfoReturnable<ItemStack> cir) {
        InteractionResult result = ItemDropCallback.EVENT.invoker().interact(this.player, this.getSelectedItem());

        if (result == InteractionResult.FAIL) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

}
