package dev.lorena.multijdk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to run a JAR file using a specified JDK and arguments.
 * <p>
 * This class builds and executes a command to run a JAR file with the selected JDK,
 * passing any additional arguments as needed. It also handles process input/output streams
 * and logs errors or process output as appropriate.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>
 *     JDKRunner runner = new JDKRunner();
 *     runner.runJDK(jdk, arguments);
 * </pre>
 *
 * @author Lorena Nuñez
 * @version 1.3
 * @since 1.0
 */
@Slf4j
public class JDKRunner {
	
	/**
	 * Runs a JAR file using the specified JDK and arguments.
	 * <p>
	 * Builds and executes a command to run the JAR file with the selected JDK,
	 * passing any additional arguments as needed. Handles process input/output streams
	 * and logs errors or process output as appropriate.
	 * </p>
	 *
	 * @param jdk the {@link JDK} to use for running the JAR
	 * @param arguments the {@link Arguments} containing the JAR path and arguments
	 */
	public void runJDK(JDK jdk, Arguments arguments) {
		
		log.debug("Running JAR: {} with JDK: {} (version {})", arguments.getJarPath(), jdk.getPath(), jdk.getVersion());
				
		List<String> commandList = new ArrayList<>();
		boolean hasArgs = !arguments.getJvmArgs().isEmpty();
		boolean hasParams = !arguments.getJarParams().isEmpty();
		
		commandList.add(jdk.getPath());
			
		if (hasArgs) {
			log.debug("Has arguments: {}", String.join(" ", arguments.getJvmArgs()));
			commandList.addAll(arguments.getJvmArgs());
		}
			
		commandList.add("-jar");
		commandList.add(arguments.getJarPath());
			
		if (hasParams) {
			log.debug("Has params: {}", String.join(" ", arguments.getJarParams()));
			commandList.addAll(arguments.getJarParams());
		}
			
		
		String command = String.join(" ", commandList);
			
		log.debug("Built command: {}", String.join(" ", command));
		run(command);	
	}

	/**
	 * Builds a {@link Runnable} that reads user input from the console and writes it to the process's input stream.
	 * <p>
	 * This method creates a thread action that continuously reads lines from {@code System.in}
	 * and writes them to the provided process's input stream, allowing interactive input to the process.
	 * </p>
	 *
	 * @param process the {@link Process} whose input stream will receive user input
	 * @return a {@link Runnable} that handles forwarding user input to the process
	 */
	private Runnable buildInputActions(Process process) {
		return () -> {
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			BufferedWriter processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			String line = "";
			while (true) {
				try {
					line = userInput.readLine();
					if (line == null) {
						log.debug("EOF received from user input. Closing process input stream...");
						break;
					}
					processWriter.write(line);
					processWriter.newLine(); // Ensure a newline is sent
					processWriter.flush();
				} catch (IOException ex) {
					log.error("An error has ocurred on the process IO.", ex);
				}
			}
		};
	}
	
	/**
	 * Builds a {@link Runnable} that reads the standard output of the given process and prints it to {@code System.out}.
	 * <p>
	 * This method creates a thread action that continuously reads characters from the process's standard output stream
	 * and prints them to the console, allowing real-time display of the process output.
	 * </p>
	 *
	 * @param process the {@link Process} whose standard output will be read
	 * @return a {@link Runnable} that handles reading and printing the process output
	 */
	private Runnable buildStdoutReaderAction(Process process) {
		return () -> {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
			try {
	              int character;
	              log.trace("Waiting for process output...");
	              while ((character = reader.read()) != -1) {
	                  System.out.print((char) character);
	                  System.out.flush();
	              }
	              log.trace("Process output stream closed.");
	          } catch (IOException e) {
	              log.error("Error reading process output: {}", e.getMessage());
	          }
		};
	}
	
	/**
	 * Builds a {@link Runnable} that reads the standard error of the given process and prints it to {@code System.err}.
	 * <p>
	 * This method creates a thread action that continuously reads characters from the process's error stream
	 * and prints them to the error console, allowing real-time display of the process error output.
	 * </p>
	 *
	 * @param process the {@link Process} whose error stream will be read
	 * @return a {@link Runnable} that handles reading and printing the process error output
	 */
	private Runnable buildStderrReaderAction(Process process) {
		return () -> {
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.defaultCharset()));
			try {
	              int character;
	              log.trace("Waiting for process error output...");
	              while ((character = errorReader.read()) != -1) {
	                  System.err.print((char) character);
	                  System.err.flush();
	              }
	              log.trace("Process error stream closed.");
	          } catch (IOException e) {
	              log.error("Error reading process error output: {}", e.getMessage());
	          }
		};
	}
	
	/**
	 * Executes the given command as a new process and manages its input, output, and error streams.
	 * <p>
	 * This method starts the process, creates and starts threads to handle user input, standard output,
	 * and standard error streams, and waits for the process to finish. Logs the process exit code upon completion.
	 * </p>
	 *
	 * @param command the command string to execute
	 */
	@SneakyThrows
	private void run(String command) {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);
		
		
		Thread inputThread = new Thread(buildInputActions(process));
		inputThread.setName("inputThread");
		inputThread.setDaemon(true);
		inputThread.start();

		Thread stdoutThread = new Thread(buildStdoutReaderAction(process));
		stdoutThread.setName("stdoutThread");
		stdoutThread.setDaemon(true);
		stdoutThread.start();

		Thread stderrThread = new Thread(buildStderrReaderAction(process));
		stderrThread.setName("stderrThread");
		stderrThread.setDaemon(true);
		stderrThread.start();

		process.waitFor();
		log.debug("Process finished with exit code: {}", process.exitValue());
		System.exit(process.exitValue());
	}
	
}