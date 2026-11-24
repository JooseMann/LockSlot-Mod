package net.joosemann.lockslot.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.joosemann.lockslot.modmenu.screens.LockSlotConfigScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Implementation entrypoint for ModMenu.<br>
 * This class only has to provide a configuration screen,
 * given by {@link LockSlotConfigScreen#createConfigScreen(Screen)}.
 * The rest of the data is pulled from {@code fabric.mod.json}.
 */
public class LockSlotModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LockSlotConfigScreen::createConfigScreen;
    }
}
