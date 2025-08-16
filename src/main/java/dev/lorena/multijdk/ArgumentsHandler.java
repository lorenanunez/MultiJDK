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

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArgumentsHandler {

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
