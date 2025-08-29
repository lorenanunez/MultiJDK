# MultiJDK PowerShell launcher script
#
# Usage: jdk <version> [<jvmParams> ...] <jar> [<jarParams> ...]
#
# This script launches a Java application using a specified JDK version.
# - The first argument is the JDK version to use.
# - Arguments before the .jar file are passed as JVM parameters.
# - The first argument ending with .jar is treated as the JAR file to run.
# - Arguments after the .jar file are passed to the JAR as parameters.
#
# The script constructs a command to run the MultiJDK Java launcher (jdk.jar)
# with the appropriate arguments and invokes it.

param (
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Args
)

# Ensure at least a version and a jar file are provided
if ($Args.Count -lt 2) {
    Write-Error "Usage: jdk <version> [<jvmParams> ...] <jar> [<jarParams> ...]"
    exit 1
}

$version = $Args[0]

# Find the first argument ending with .jar (case-insensitive)
$jarIndex = -1
for ($i = 0; $i -lt $Args.Count; $i++) {
    if ($Args[$i] -match '\.jar$') {
        $jarIndex = $i
        break
    }
}
if ($jarIndex -lt 0) {
    Write-Error "No .jar file specified."
    exit 1
}

# Extract the jar file, JVM parameters, and JAR parameters
$jar = $Args[$jarIndex]
$jvmParams = if ($jarIndex -gt 1) { $Args[1..($jarIndex-1)] -join ' ' } elseif ($jarIndex -eq 1) { '' } else { '' }
$jarParams = if ($jarIndex+1 -lt $Args.Count) { $Args[($jarIndex+1)..($Args.Count-1)] -join ' ' } else { '' }

# Get the script directory and resolve the absolute path of the jar
$scriptDir = $PSScriptRoot
$jarAbsolute = (Resolve-Path $jar).Path
$command = "java"

# Add JVM parameters if present
if ($jvmParams) {
    $command += " $jvmParams"
}

# Add the MultiJDK launcher and required arguments
$command += " -jar $scriptDir\jdk.jar -v $version -j `"$jarAbsolute`""

# Add JAR parameters if present
if ($jarParams) {
    $command += " $jarParams"
}

# Execute the constructed command
Invoke-Expression $command