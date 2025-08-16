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

@Slf4j
public class JDKRunner {
	
	public void runJDK(JDK jdk, Arguments arguments) {
		
		try {
			List<String> commandList = new ArrayList<>();
			boolean hasArgs = arguments.getJarArgs().length > 0;
			
			commandList.add(jdk.getPath());
			commandList.add("-jar");
			commandList.add(arguments.getJarPath());
			
			if (hasArgs) {
				commandList.add("\" + arguments.getJarArgs() + \"");
			}
			
			String[] command = commandList.toArray(new String[0]);
			log.debug("Built command: {}", String.join(" ", command));
			
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
			
			
		} catch (IOException ex) {
			log.error("Failed to execute JAR file: {}", ex.getMessage());
			System.exit(1);
		} catch (InterruptedException ex) {
			log.error("Process was interrupted: {}", ex.getMessage());
			Thread.currentThread().interrupt();
			System.exit(1);
		}
		
		
	}
	

}
