#!/bin/bash
# MultiJDK Linux launcher script
#
# Usage: jdk <version> [<jvmParams> ...] <jar> [<jarParams> ...]
#
# This script launches a Java application using a specified JDK version.
# - The first argument is the JDK version to use.
# - Arguments before the .jar file are passed as JVM parameters.
# - The first argument ending with .jar is treated as the JAR file to run.
# - Arguments after the .jar file are passed to the JAR as parameters.

# Check if at least two arguments are provided (JDK version and JAR file)
if [ $# -lt 2 ]; then
    echo "Usage: jdk <version> [<jvmParams> ...] <jar> [<jarParams> ...]" >&2
    exit 1
fi

# Store the JDK version and shift to process the rest of the arguments
version="$1"
shift

# Find the index of the first argument ending with .jar
jarIndex=-1
count=0
for arg in "$@"; do
    case "$arg" in
        *.jar)
            jarIndex=$count
            break
            ;;
    esac
    count=$((count+1))
done

# If no .jar file is found, exit with error
if [ "$jarIndex" -lt 0 ]; then
    echo "No .jar file specified." >&2
    exit 1
fi

# Reset positional parameters to process arguments by index
set -- "$@"
# Extract the jar file path
jar="${@:$((jarIndex+1)):1}"

# Collect JVM parameters (arguments before the .jar file)
jvmParams=""
if [ "$jarIndex" -gt 0 ]; then
    for i in $(seq 1 $jarIndex); do
        jvmParams="$jvmParams ${@:$i:1}"
    done
fi

# Collect JAR parameters (arguments after the .jar file)
jarParams=""
if [ $((jarIndex+2)) -le $# ]; then
    for i in $(seq $((jarIndex+2)) $#); do
        jarParams="$jarParams ${@:$i:1}"
    done
fi

# Get the directory of this script
scriptDir="$(dirname "$0")"
# Get the absolute path of the jar file
jarAbsolute="$(readlink -f "$jar")"

# Build the command to run the MultiJDK Java launcher with the correct arguments
command="java -jar \"$scriptDir/jdk.jar\" -v $version -j \"$jarAbsolute\""

# If there are JVM parameters, add them with -a
if [ -n "$jvmParams" ]; then
    command="$command -a $jvmParams"
fi
# If there are JAR parameters, add them with -p
if [ -n "$jarParams" ]; then
    command="$command -p $jarParams"
fi

# Execute the constructed command
eval $command