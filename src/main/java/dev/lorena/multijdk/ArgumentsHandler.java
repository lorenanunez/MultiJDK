package dev.lorena.multijdk;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
 * @version 1.0
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentsHandler {

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
    public static Arguments getArguments(String[] args) {
		
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
				.desc("Arguments to pass to the JAR file")
				.hasArgs()
				.get());
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			int jdkVersion = Integer.parseInt(cmd.getOptionValue("version"));
			String jarPath = cmd.getOptionValue("jar");
			String[] jarArgs = cmd.getOptionValues("args");
			
			Arguments arguments = new Arguments();
			arguments.setVersion(jdkVersion);
			arguments.setJarPath(jarPath);
			arguments.setJarArgs(jarArgs != null ? jarArgs : new String[0]);
			
			return arguments;
		} catch (ParseException e) {
			log.error("Failed to parse command line arguments", e);
			return null;
		}
		
	}
	
}