package dev.lorena.multijdk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class JDKFinder {
	
	public List<JDK> findJDKs() {
		
		List<JDK> jdks = new ArrayList<>();
		List<String> commonLocations = Arrays.asList(
				"C:\\Program Files\\Java",
				"C:\\Program Files (x86)\\Java",
				"C:\\Program Files\\Amazon Corretto",
				"C:\\Program Files\\Eclipse Adoptium",
				System.getenv("LOCALAPPDATA") + "\\Programs\\Eclipse Adoptium"
		);

		log.debug("Searching for JDK installations in common locations: {}", commonLocations);
		
		commonLocations.forEach(location -> {
			
			Collection<File> files = FileUtils.listFiles(
					new File(location), 
					new RegexFileFilter("(^|\\\\)java\\.exe$"), 
					DirectoryFileFilter.DIRECTORY
			);
			
			log.debug("Looking in: {}", location);
			
			files.forEach(file -> {
				String path = file.getAbsolutePath();
				if (!path.contains("jre")) {
					String croppedPath = path.substring(path.indexOf("\\jdk") + 1, path.indexOf("\\bin"));
					String numbersOnly = croppedPath.replaceAll("jdk|-", "");
					String vendor = extractVendorFromPath(path);
					
					if (numbersOnly.startsWith("1.")) {
						jdks.add(new JDK(Integer.parseInt(String.valueOf(numbersOnly.charAt(2))), path, vendor));
					} else if (numbersOnly.indexOf(".") == 1) {
						jdks.add(new JDK(Integer.parseInt(String.valueOf(numbersOnly.charAt(0))), path, vendor));
					} else if (numbersOnly.length() > 2) {
						jdks.add(new JDK(Integer.parseInt(numbersOnly.substring(0, 2)), path, vendor));
					} else {
						jdks.add(new JDK(Integer.parseInt(numbersOnly), path, vendor));
					}
					
				}
			});
		});
		
		log.debug("Found JDK installations:");
		jdks.forEach(jdk -> log.debug("\t{}: {}", jdk.getVersion(), jdk.getPath()));
		
		return jdks;
	}
	
	private String extractVendorFromPath(String path) {
		if (path.contains("Amazon Corretto")) {
			return "Amazon Corretto";
		} else if (path.contains("Eclipse Adoptium")) {
			return "Eclipse Adoptium";
		} else {
			return "Oracle";
		}
	}

}
