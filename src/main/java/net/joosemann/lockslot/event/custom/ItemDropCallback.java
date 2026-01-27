package net.joosemann.lockslot.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemDropCallback {
    Event<ItemDropCallback> EVENT = EventFactory.createArrayBacked(ItemDropCallback.class,
            (listeners) -> (player, item) -> {
               for (ItemDropCallback listener : listeners) {
                   InteractionResult result = listener.interact(player, item);

                   if (result != InteractionResult.PASS) {
                       return result;
                   }
               }

               return InteractionResult.PASS;
            });


    InteractionResult interact(Player player, ItemStack item);
}
