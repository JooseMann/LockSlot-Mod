package net.joosemann.lockslot.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TooltipUpdateEvent implements ItemTooltipCallback {

    // If we are looking at our custom locked indicator item, then we want to add our own tooltip.
    // We use this item exclusively to help with rendering, so we don't want the end user to be messing around with it too much.
    // For that reason, we add a tooltip here that cautions the user against holding this item, as to avoid UI issues.
    @Override
    public void getTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list) {
       if (itemStack.getDisplayName().getString().equals("[Lock Indicator]")) {
           list.add(Component.translatable("itemTooltip.lock-slot.lock_indicator.0").withStyle(ChatFormatting.BLUE));
           list.add(Component.translatable("itemTooltip.lock-slot.lock_indicator.1").withStyle(ChatFormatting.GRAY));
           list.add(Component.translatable("itemTooltip.lock-slot.lock_indicator.2").withStyle(ChatFormatting.GRAY));
           list.add(Component.translatable("itemTooltip.lock-slot.lock_indicator.3").withStyle(ChatFormatting.GRAY));
       }
    }
}
