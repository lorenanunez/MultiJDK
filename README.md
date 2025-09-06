# MultiJDK
This is a simple tool I made for being able to execute different JAR files with different installed JDK's on Windows (for now). Some of my job software requires to be run on, for example, in Java 8, and others, in Java 11. As I got tired to constantly switch the JAVA\_HOME variable or write large commands (call me lazy) I wrote this tool. SDKMan inspired me, but this work on Windows. 

### How it works?
It scans for installed JDK's in common locations (Oracle, Amazon Coretto, Eclipse Temurin) and runs the provided JAR with the selected JDK version. It uses the RELEASE file to determine the JDK version, the vendor and the executable path.
Usage: ```jdk <version> <jarfile> <args (optional)>```

For example:
* ```jdk 8 myapp.jar```
* ```jdk 11 -Xmx524M otherapp.jar arg1 arg2```

### Pre-requisites:
* Java 1.8+
#### On Windows
* PowerShell
Download the release and extract in any folder. **Be sure to add it to PATH** and **have the permissions to run PowerShell scripts on your machine**.
To enable run PowerShell scripts, type this command: ```Set-ExecutionPolicy Unrestricted```
#### On Linux
* Be sure to add execute permmision to jdk file: "chmod +x jdk" and add MultiJDK folder to PATH

### Final words
This is my **FIRST** public project. Feel totally free to blame, report bugs or contribute in any way. I will be adding more features and brusing up this tool as I have spare time. Thanks.

