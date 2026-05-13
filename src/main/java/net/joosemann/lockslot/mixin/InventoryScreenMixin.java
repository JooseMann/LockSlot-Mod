package net.joosemann.lockslot.mixin;

import net.joosemann.lockslot.client.LockedValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ListIterator;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(at = @At(value = "TAIL"), method = "renderBg")
    protected void renderBgMixin(GuiGraphics guiGraphics, float f, int k, int l, CallbackInfo ci) {
        // Render any slots that are locked

        int x, y;
        InventoryScreen inv = (InventoryScreen) (Object) this;

        // Don't try to render if there's no screen
        if (Minecraft.getInstance().screen == null) return;

        // X and Y coordinate of the inventory texture
        int leftPos = (inv.width - 176) / 2;
        int topPos = (inv.height - 166) / 2;

        ListIterator<Slot> itr = LockedValues.getLockedListIterator();
        Slot slot;

        while (itr.hasNext()) {
            if (itr.previousIndex() == -1) {
                // If at the start of the list, make sure to include the first element
                itr.next();
                slot = itr.previous();
                itr.next();
            }
            else {
                slot = itr.next();
            }

            // X and Y position of each slot, offset by the location of the inventory
            x = leftPos + slot.x;
            y = topPos + slot.y;

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LockedValues.getLockRenderingId(), x, y, 0, 0, 16, 16, 16, 16);
        }
    }
}
