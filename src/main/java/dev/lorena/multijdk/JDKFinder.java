package dev.lorena.multijdk;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for searching and identifying installed JDKs on the system.
 * <p>
 * This class scans common installation directories and any custom locations specified in the settings.
 * It extracts JDK version and vendor information by reading the 'release' file found in JDK installations.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>
 *     JDKFinder finder = new JDKFinder();
 *     List<JDK> jdks = finder.findJDKs();
 * </pre>
 *
 * @author Lorena Nuñez
 * @version 1.3
 * @since 1.0
 */
@Slf4j
public class JDKFinder {
    /**
     * Searches for installed JDKs in common locations on the system.
     * <p>
     * The method looks for JDK installations in well-known directories and returns a list of detected JDKs with their version, path, and vendor information.
     * </p>
     *
     * @return a list of found {@link JDK} objects with version, path, and vendor information
     */
    public List<JDK> findJDKs() {
        Settings settings = SettingsManager.getSettings();
        
        String programFiles = System.getenv("ProgramW6432");
        String programFilesx86 = System.getenv("ProgramFiles(x86)");
        String localAppData = System.getenv("LOCALAPPDATA");
        
        
        List<String> jdkLocations = new ArrayList<>(Arrays.asList(
            programFiles + "\\Java",
            programFilesx86 + "\\Java",
            programFiles + "\\Amazon Corretto",
            programFiles + "\\Eclipse Adoptium",
            localAppData + "\\Programs\\Eclipse Adoptium",
            localAppData + "\\Programs\\Microsoft",
            programFiles + "\\ojdkbuild\\",
            programFiles + "\\Microsoft\\"
        ));
        
        log.trace(jdkLocations.toString());
        
        log.debug("Custom JDK locations from settings: {}", settings.getCustomJDKlocations());
        jdkLocations.addAll(settings.getCustomJDKlocations());
        
        List<JDK> jdks = extractJDKsfromLocations(jdkLocations);
        log.debug("Searching for JDK installations in common locations:");
        log.debug("Found JDK installations:");
        jdks.forEach(jdk -> log.debug("\t{}: {}", jdk.getVersion(), jdk.getPath()));
        return jdks;
    }
	
    /**
     * Extracts JDKs from the given list of directory paths.
     * <p>
     * For each path, this method searches recursively for 'java.exe' executables, then attempts to locate and parse the corresponding 'release' file to extract version and vendor information.
     * </p>
     *
     * @param paths list of directory paths to search for JDKs
     * @return a list of {@link JDK} objects found in the specified locations
     */
	private List<JDK> extractJDKsfromLocations(List<String> paths) {
        List<JDK> jdks = new ArrayList<>();
        IOFileFilter filter = new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return accept(file.getParentFile(), file.getName());
			}
			@Override
			public boolean accept(File dir, String name) {
				boolean isJavaExe = name.equalsIgnoreCase("java.exe");
		        boolean pathContainsJdk = Strings.CI.contains(dir.getAbsolutePath(), "jdk");
		        boolean pathDontContainsJre = !Strings.CI.contains(dir.getAbsolutePath(), "jre");
		        return isJavaExe && pathContainsJdk && pathDontContainsJre;
			}
        };
        
		paths.forEach(path -> {
        	Collection<File> files = FileUtils.listFiles(new File(path), filter, DirectoryFileFilter.DIRECTORY);
        	files.forEach(file ->  {
        		
        		File releaseFile = FileUtils.getFile(file.getParentFile().getParentFile().getAbsolutePath(), "release");
        		
        		if (releaseFile != null && releaseFile.exists() && releaseFile.canRead()) {
        			int version = extractVersionFromReleaseFile(releaseFile);
	        		String jdkPath = file.getAbsolutePath();
	        		String vendor = extractVendorFromReleaseFile(releaseFile);
	        		jdks.add(new JDK(version,jdkPath, vendor));
        		} else {
        			log.error("There was an error reading the RELEASE file for JDK at path: {}", file.getAbsolutePath());
        		}
        		
        	});
        });
        return jdks;
    }
	
    /**
     * Extracts the major version number from the 'release' file of a JDK installation.
     * <p>
     * The method reads the 'JAVA_VERSION' property from the release file and parses the major version number according to Java versioning conventions.
     * </p>
     *
     * @param file the 'release' file inside a JDK directory
     * @return the major version number of the JDK, or -1 if not found or on error
     */
	private int extractVersionFromReleaseFile(File file) {
		int numericVersion = -1;

		Properties properties = getPropertiesFromReleaseFile(file);

		String version = properties.getProperty("JAVA_VERSION").replace("\"", "");
 		String numbers = StringUtils.getDigits(version);

		if (version.startsWith("1.")) {
			numericVersion = Integer.parseInt(String.valueOf(numbers.charAt(1)));
		} else if (numbers.startsWith("9")) {
			numericVersion = Integer.parseInt(String.valueOf(numbers.charAt(0)));
		} else {
			numericVersion = Integer.parseInt(StringUtils.left(numbers, 2));
		}

		return numericVersion;
	}
	
    /**
     * Extracts the vendor name from the 'release' file of a JDK installation.
     * <p>
     * The method reads the 'IMPLEMENTOR' property from the release file.
     * </p>
     *
     * @param file the 'release' file inside a JDK directory
     * @return the vendor name, or null if not found
     */
	private String extractVendorFromReleaseFile(File file) {
		Properties properties = getPropertiesFromReleaseFile(file);
		String vendor = properties.getProperty("IMPLEMENTOR");
		return (vendor != null) ? vendor.trim() : null;
	}
	
    /**
     * Loads properties from a JDK 'release' file.
     * <p>
     * Reads the file content and loads it into a {@link Properties} object.
     * </p>
     *
     * @param file the 'release' file to read
     * @return a {@link Properties} object containing the file's properties, or null if the file cannot be read
     */
	private Properties getPropertiesFromReleaseFile(File file) {
		if (file == null) {
			log.error("File cannot be null");
			System.exit(0);
		}
		if (file.exists() && file.canRead()) {
			try {
				String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				Properties properties = new Properties();
				properties.load(new StringReader(fileContent));
				return properties;
			} catch (IOException ex) {
				log.error("There was an error loading properties from RELEASE file: {}", ex.getMessage());
			}
		} else {
			log.error("RELEASE files does not exists or JVM cannot read it: {}", file.getAbsolutePath());
			System.exit(0);
		}
		return new Properties();
	}
	
}