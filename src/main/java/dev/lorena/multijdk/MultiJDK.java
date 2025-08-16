package dev.lorena.multijdk;

import java.util.List;
import java.util.stream.Collectors;

import com.formdev.flatlaf.FlatLightLaf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiJDK {
	
	public static void main(String[] args) {
		
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
			log.error("MultiJDK is only supported on Windows. For now.");
			System.exit(1);
		}
		
		if (args.length > 3 && args.length < 2) {
			log.error("Usage: jdk <version> <jar> [-a <args>]");
			System.exit(1);
		}
		
		JDKFinder finder = new JDKFinder();
		List<JDK> jdks = finder.findJDKs();
		Arguments arguments = ArgumentsHandler.getArguments(args);
		JDKRunner runner = new JDKRunner();
		
		long jdkCount = jdks.stream().filter(jdk -> jdk.getVersion() == arguments.getVersion()).count();
		
		if (jdkCount == 0) {
			log.error("No JDK found for version: {}", arguments.getVersion());
			System.exit(1);
		}
		
		if (jdkCount > 1 ) {
			List<JDK> selectedVersionJDKs = jdks.stream().filter(jdk -> jdk.getVersion() == arguments.getVersion()).collect(Collectors.toList());
			FlatLightLaf.setup();
			JDKVersionChooser chooser = new JDKVersionChooser(selectedVersionJDKs);
			JDK selectedJDK = chooser.getChoosenJDK();
			
			runner.runJDK(selectedJDK, arguments);
		} else {
			JDK jdkToRun = jdks.stream().filter(jdk -> jdk.getVersion() == arguments.getVersion()).findFirst().orElse(null);
			runner.runJDK(jdkToRun, arguments);
		}
		
	}

}