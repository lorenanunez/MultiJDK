package dev.lorena.multijdk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
 * @version 1.0
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
			
			
		String[] command = commandList.toArray(new String[0]);
		log.debug("Built command: {}", String.join(" ", command));
		try {
			run(command);
		} catch (IOException | InterruptedException e) {
			log.error("Error running command: {}", e.getMessage());
			Thread.currentThread().interrupt();
		}		
	}
	
	private void run(String[] command) throws IOException, InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.ISO_8859_1));
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.ISO_8859_1));
      
		Thread inputThread = new Thread(() -> {
          try {
              BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
              BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.ISO_8859_1));
              String line;
              while ((line = userInput.readLine()) != null) {
                  processInput.write(line);
                  processInput.newLine();
                  processInput.flush();
              }
          } catch (IOException e) {
              log.error("Error reading user input: {}", e.getMessage());
          }
      });
      inputThread.setDaemon(true);
      inputThread.start();
      
      Thread stdoutThread = new Thread(() -> {
          try {
              int c;
              while ((c = reader.read()) != -1) {
                  System.out.print((char) c);
              }
          } catch (IOException e) {
              log.error("Error reading process output: {}", e.getMessage());
          }
      });
      stdoutThread.setDaemon(true);
      stdoutThread.start();

      Thread stderrThread = new Thread(() -> {
          try {
              int c;
              while ((c = errorReader.read()) != -1) {
                  System.err.print((char) c);
              }
          } catch (IOException e) {
              log.error("Error reading process error output: {}", e.getMessage());
          }
      });
      stderrThread.setDaemon(true);
      stderrThread.start();
      process.waitFor();
	}
	

}