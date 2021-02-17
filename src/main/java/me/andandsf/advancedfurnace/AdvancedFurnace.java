package me.andandsf.advancedfurnace;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancedFurnace implements ModInitializer {

	public static final String MOD_ID = "advancedfurnace";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MOD_ID);
	@Override
	public void onInitialize() {
		LOGGER.info("hello!");
	}
}
