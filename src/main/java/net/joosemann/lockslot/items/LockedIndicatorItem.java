package net.joosemann.lockslot.items;

import net.joosemann.lockslot.LockSlot;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class LockedIndicatorItem {
    public static Item ITEM = setup(Item::new, new Item.Properties());

    // Dummy initialize function to tell Minecraft that we added an item
    public static void initialize() {}

    private static <T extends Item> T setup(Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "locked_indicator"));

        T item = itemFactory.apply(settings.setId(itemKey));

        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
}
