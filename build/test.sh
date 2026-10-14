#!/bin/sh
#
# Compiles and runs the JUnit tests locally, without Ant or an IDE.
#
# Usage:
#   ./build/test.sh
#   ./build/test.sh de.willuhn.jameica.ssh.SshTunnelConfigTest
#
# Without arguments, all classes ending in "Test" below the "test" directory
# are compiled and executed. The compiled classes are written to "test-bin"
# (which is ignored by git).
#
set -e

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

# Locate the JDK tools. Prefer JAVA_HOME, fall back to the PATH.
javac="javac"
java="java"
if [ -n "$JAVA_HOME" ]; then
  [ -x "$JAVA_HOME/bin/javac" ] && javac="$JAVA_HOME/bin/javac"
  [ -x "$JAVA_HOME/bin/java" ]  && java="$JAVA_HOME/bin/java"
fi

# Build the classpath from lib/ (excluding the DB drivers and the foreign
# SWT platforms) plus the test libraries.
libjars=$(find lib -name '*.jar' \
  | grep -v -E '/(h2|mariadb|mysql)/' \
  | grep -v -E 'linux|macos')
testjars=$(find test/lib -name '*.jar')
cp=$(printf '%s\n' $libjars $testjars | tr '\n' ':')

# Collect the test sources and their class names.
test_sources=$(find test -name '*Test.java')
if [ -z "$test_sources" ]; then
  echo "No tests found" >&2
  exit 1
fi
test_classes=$(printf '%s\n' $test_sources | sed 's#^test/##; s#/#.#g; s#\.java$##')

# Compile.
out="test-bin"
rm -rf "$out"
mkdir -p "$out"

$javac -encoding ISO-8859-1 -nowarn -cp "$cp" -sourcepath "src:test" -d "$out" $test_sources

# Run.
run_cp="$out:$cp"
if [ "$#" -gt 0 ]; then
  $java -cp "$run_cp" org.junit.runner.JUnitCore "$@"
else
  $java -cp "$run_cp" org.junit.runner.JUnitCore $test_classes
fi
