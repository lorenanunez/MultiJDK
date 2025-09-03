package dev.lorena.multijdk;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling and parsing command-line arguments for the MultiJDK application.
 * <p>
 * This class uses Apache Commons CLI to define and parse the required and optional arguments
 * for running a JAR file with a specific JDK version. It is designed as a static utility class
 * and cannot be instantiated.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     Arguments args = ArgumentsHandler.getArguments(argsArray);
 * </pre>
 *
 * <ul>
 *   <li><b>-v, --version</b>: JDK version to use (required)</li>
 *   <li><b>-j, --jar</b>: Path to the JAR file to run (required)</li>
 *   <li><b>-a, --args</b>: Arguments to pass to the JAR file (optional, multiple allowed)</li>
 * </ul>
 *
 * @author Lorena Nuñez
 * @since 1.0
 * @version 1.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentsHandler {

	private static Arguments arguments = null;
	
    /**
     * Parses the command-line arguments and returns an {@link Arguments} object.
     * <p>
     * This method defines the expected options, parses the input arguments, and constructs
     * an {@link Arguments} instance with the parsed values. If parsing fails, it logs the error
     * and returns {@code null}.
     * </p>
     *
     * @param args the command-line arguments
     * @return an {@link Arguments} object with the parsed values, or {@code null} if parsing fails
     */
    public static Arguments getArguments(String[] args) throws MissingOptionException, NullPointerException {
		
		Options options = new Options();
		
		options.addOption(Option.builder("v")
				.longOpt("version")
				.hasArg()
				.desc("JDK version to use")
				.required()
				.get());
		
		options.addOption(Option.builder("j")
				.longOpt("jar")
				.hasArg()
				.desc("Path to the JAR file to run")
				.required()
				.get());
		
		options.addOption(Option.builder("a")
				.longOpt("args")
				.desc("Arguments to pass to the JVM")
				.hasArgs()
				.get());
		
		options.addOption(Option.builder("p")
				.longOpt("params")
				.desc("Params to pass to the JAR file")
				.hasArg()
				.get());
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			String v = cmd.getOptionValue("version");
			
			if (!StringUtils.isNumeric(v)) {
				log.error("JDK version must be a number");
				System.exit(1);
			}
			
			int jdkVersion = Integer.parseInt(v);
			String jarPath = cmd.getOptionValue("jar");
			
			String[] unknownJvmArguments = cmd.getOptionValues("args");
			String[] unknownJarParams = cmd.getOptionValues("params");
			
			Set<String> jvmArgs = (unknownJvmArguments != null) ? Arrays.asList(unknownJvmArguments).stream().collect(Collectors.toSet()) : new HashSet<>();
			Set<String> jarParams = (unknownJarParams != null) ? Arrays.asList(unknownJarParams).stream().collect(Collectors.toSet()) : new HashSet<>();

			boolean hasEncodingArg = jvmArgs.stream().anyMatch(arg -> arg.toLowerCase().startsWith("-dfile.encoding="));
			
			if (!hasEncodingArg) {
				String argument = String.format("-Dfile.encoding=%s", Charset.defaultCharset());
				log.debug("Encoding argument was not found, injecting argument: {}", argument);
				jvmArgs.add(argument);
			}
			
			arguments = new Arguments();
			arguments.setVersion(jdkVersion);
			arguments.setJarPath(jarPath);
			arguments.setJvmArgs(jvmArgs);
			arguments.setJarParams(jarParams);
			
			log.debug("Parsed arguments: {}", arguments);
			return arguments;
		} catch (ParseException e) {
			log.error("Failed to parse command line arguments");
			log.info("Usage: jdk <version> [-a <arg1> <arg2> ...] <jarPath> [-p <param1> <param2> ...]");
			System.exit(1);
		}
		
		// This return is unreachable, but required for compilation
		return null;
	}
    
    public static Optional<Arguments> getParsedArguments() {
		return Optional.ofNullable(arguments);
	}
	
}