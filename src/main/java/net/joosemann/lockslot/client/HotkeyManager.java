package net.joosemann.lockslot.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.joosemann.lockslot.LockSlot;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class HotkeyManager {
    private static KeyMapping lockKeybind;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(LockSlot.MOD_ID, "test")
    );

    public static KeyMapping getLockKeybind() {
        return lockKeybind;
    }

    public static void registerKeybinds() {
        lockKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.lock-slot.lock",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY
        ));
    }
}
