package vai.hbtweaks.context;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HBTweaksContext implements ModInitializer {
	public static final String MOD_ID = "hb-tweaks-context";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		LOGGER.info(Paths.get("test.txt").toAbsolutePath().toString());
	}
}