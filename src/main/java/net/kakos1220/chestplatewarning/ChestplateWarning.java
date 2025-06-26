package net.kakos1220.chestplatewarning;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ChestplateWarning implements ModInitializer {
	public static final String MOD_ID = "chestplatewarning";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		loadConfig(CONFIG_FILE);
		saveConfig(CONFIG_FILE);
	}


	public static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(),
			"ChestplateWarning.properties");
	public static boolean isModDisabled = false;
	public static boolean isFlashingOn = true;
	public static boolean isDamageWarningOn = true;
	public static int elytraDurabilityThreshold = 10;
	public static boolean considerUnbreaking = true;

	public static final Map<String, Boolean> worldToggles = new HashMap<>();
	public static boolean isCurrentWorldDisabled = false;

	public static final Map<String, Boolean> serverToggles = new HashMap<>();
	public static boolean isCurrentServerDisabled = false;

	public static String key(String raw) {
		return raw.toLowerCase().replace(' ', '_');
	}

	public static void loadConfig(File file) {
		try {
			Properties cfg = new Properties();
			if (!file.exists()) {
				saveConfig(file);
			}
			cfg.load(new FileInputStream(file));
			try (FileInputStream fis = new FileInputStream(file)) {
				cfg.load(fis);
			}

			isModDisabled = Boolean.parseBoolean(cfg.getProperty("isModDisabled", "false"));
			isFlashingOn = Boolean.parseBoolean(cfg.getProperty("isFlashingOn", "true"));
			isDamageWarningOn = Boolean.parseBoolean(cfg.getProperty("isDamageWarningOn", "true"));
			elytraDurabilityThreshold = Integer.parseInt(cfg.getProperty("elytraDurabilityThreshold", "10"));
			considerUnbreaking = Boolean.parseBoolean(cfg.getProperty("considerUnbreaking", "true"));

			for (String prop : cfg.stringPropertyNames()) {
				if (prop.startsWith("world.")) {
					String key = prop.substring("world.".length()).toLowerCase();
					boolean enabled = Boolean.parseBoolean(cfg.getProperty(prop, "true"));
					worldToggles.put(key, enabled);
				}

				if (prop.startsWith("server.")) {
					String key = prop.substring("server.".length()).toLowerCase();
					boolean enabled = Boolean.parseBoolean(cfg.getProperty(prop, "true"));
					serverToggles.put(key, enabled);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig(File file) {
		try (FileOutputStream fos = new FileOutputStream(file, false)) {
			fos.write(("isModDisabled=" + isModDisabled + "\n").getBytes());
			fos.write(("isFlashingOn=" + isFlashingOn + "\n").getBytes());
			fos.write(("isDamageWarningOn=" + isDamageWarningOn + "\n").getBytes());
			fos.write(("elytraDurabilityThreshold=" + elytraDurabilityThreshold + "\n").getBytes());
			fos.write(("considerUnbreaking=" + considerUnbreaking + "\n").getBytes());

			for (var e : worldToggles.entrySet()) {
				fos.write(("world."  + e.getKey() + "=" + e.getValue() + "\n").getBytes());
			}
			for (var e : serverToggles.entrySet()) {
				fos.write(("server." + e.getKey() + "=" + e.getValue() + "\n").getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}