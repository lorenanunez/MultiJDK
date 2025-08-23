package dev.lorena.multijdk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SettingsManager is responsible for managing application settings, including loading,
 * saving, and initializing the settings file. It uses Gson for JSON serialization and
 * deserialization. This class is implemented as a singleton utility class.
 * 
 * @author Lorena Nuñez
 * @version 1.1
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettingsManager {
	
	private static final Gson gson = new GsonBuilder().setFormattingStyle(FormattingStyle.PRETTY).create();
	private static final Path settingsPath = getSettingsPath();
	private static Settings settings = null;
	
	/**
	 * Loads or initializes the settings file. If the file does not exist, it creates a new one
	 * with default values. If it exists, it loads the properties from the file.
	 */
	static {
		try {
			File file = settingsPath.toFile();
			if (!Files.exists(settingsPath) && file.getAbsoluteFile().getParentFile().canWrite()) {
				if (Files.createFile(settingsPath) != null) {
					log.debug("Creating new settings file at {}", file.getAbsolutePath());
					
					settings = new Settings();
					settings.setCustomJDKlocations(new ArrayList<String>());
					settings.setPreferredJDKPerFile(new HashMap<String, String>());
					
					FileWriter writer = new FileWriter(file);
					writer.write(gson.toJson(settings));
					writer.flush();
					writer.close();
				} else {
					log.error("An error has ocurred creating settings file at {}", file.getAbsolutePath());
				}
			} else {
				log.debug("Loading properties from {}", file.getAbsolutePath());
				
				StringBuilder sb = new StringBuilder();
				Scanner sc = new Scanner(file);
				
				while (sc.hasNext()) {
					sb.append(sc.nextLine());
				}
				
				settings = gson.fromJson(sb.toString(), Settings.class);
				
				sc.close();
			}
		} catch (Exception ex) {
			log.error("An error has ocurred", ex);
		}
	}
	
	/**
	 * Returns the current settings map.
	 *
	 * @return the settings map
	 */
	public static synchronized Settings getSettings() {
		return settings;
	}
	
	/**
	 * Saves the provided settings object to the settings file and updates the in-memory settings instance.
	 *
	 * @param settings the Settings object to be saved and set as the current settings
	 */
	public static synchronized void saveSettings(Settings settings) {
		try (FileWriter writer = new FileWriter(settingsPath.toFile())){
			SettingsManager.settings = settings;
			writer.write(gson.toJson(settings));
			writer.flush();
		} catch (IOException ex) {
			log.error("Error saving property.", ex);
		}
	}

	/**
	 * Determines the path where the settings.json file should be created.
	 * Attempts to use the directory of the running JAR file.
	 *
	 * @return the Path to the settings.json file
	 */
	private static Path getSettingsPath() {
		String jarDir = null;
		try {
			String path = SettingsManager.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			log.debug("JAR located at: {}", path);
			File jarFile = new File(path);
			jarDir = jarFile.getParent();
		} catch (URISyntaxException ex) {
			log.error("Could not determine JAR directory.", ex);
			System.exit(0);
		}
		return Paths.get(jarDir, "settings.json");
	}
}