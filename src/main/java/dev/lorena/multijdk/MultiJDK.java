package dev.lorena.multijdk;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;

import com.formdev.flatlaf.FlatLightLaf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiJDK {
	
	@SneakyThrows
	public static void main(String[] args) {
		
		JDKFinder finder = null;
		
		if (SystemUtils.IS_OS_WINDOWS) {
			finder = new JDKFinder("windows");
		} else if (SystemUtils.IS_OS_LINUX) {
			finder = new JDKFinder("linux");
		} else if (SystemUtils.IS_OS_MAC) {
			log.error("MacOS is not supported yet.");
		}
		
		List<JDK> jdks = finder.findJDKs();
		Arguments arguments = ArgumentsHandler.getArguments(args);
			
		log.debug("Found arguments: {}", arguments);
		
		int jdkCount = (int) jdks.stream().filter(jdk -> jdk.getVersion() == arguments.getVersion()).count();
		
		runMultiJDK(jdkCount, jdks);
	}
	
	private static void runMultiJDK(int jdkCount, List<JDK> jdks) {
		JDKRunner runner = new JDKRunner();
		Arguments arguments = ArgumentsHandler.getParsedArguments().orElseThrow(null);
		JDK jdkToRun = null;
		
		switch (jdkCount) {
			case 0:
				log.error("No JDK found for version: {}", arguments.getVersion());
				System.exit(1);
				break;
			case 1:
				jdkToRun = jdks.stream().filter(j -> j.getVersion() == arguments.getVersion())
								.collect(Collectors.toList())
								.get(0);
				runner.runJDK(jdkToRun, arguments);
				break;
			default:
				log.info("Multiple JDKs found for version: {}", arguments.getVersion());
				if (ArgumentsHandler.getParsedArguments().isPresent()) {
					Settings settings = SettingsManager.getSettings();
					
					 if (settings.getPreferredJDKPerFile().containsKey(arguments.getJarPath())) {
						 jdkToRun = new JDK(arguments.getVersion(), settings.getPreferredJDKPerFile().get(arguments.getJarPath()), null);
						 runner.runJDK(jdkToRun, arguments);
					 } else {
						List<JDK> selectedVersionJDKs = jdks.stream().filter(jdk -> jdk.getVersion() == arguments.getVersion()).collect(Collectors.toList());
						FlatLightLaf.setup();
						JDKVersionChooser chooser = new JDKVersionChooser(selectedVersionJDKs);
						JDK selectedJDK = chooser.getChoosenJDK();
						runner.runJDK(selectedJDK, arguments);
					 }
				}
				break;
		}
	}

}