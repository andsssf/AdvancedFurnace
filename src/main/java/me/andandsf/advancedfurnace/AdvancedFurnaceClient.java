package me.andandsf.advancedfurnace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class AdvancedFurnaceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(AdvancedFurnace.ADVANCED_FURNACE_SCREEN_HANDLER, AdvancedFurnaceScreen::new);
    }
}
