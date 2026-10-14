#
# Compiles and runs the JUnit tests locally, without Ant or an IDE.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File build\test.ps1
#   powershell -ExecutionPolicy Bypass -File build\test.ps1 de.willuhn.jameica.ssh.SshTunnelConfigTest
#
# Without arguments, all classes ending in "Test" below the "test" directory
# are compiled and executed. The compiled classes are written to "test-bin"
# (which is ignored by git).
#
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

# Locate the JDK tools. Prefer JAVA_HOME, fall back to the PATH.
$javac = "javac"
$java  = "java"
if ($env:JAVA_HOME)
{
  if (Test-Path "$env:JAVA_HOME\bin\javac.exe") { $javac = "$env:JAVA_HOME\bin\javac.exe" }
  if (Test-Path "$env:JAVA_HOME\bin\java.exe")  { $java  = "$env:JAVA_HOME\bin\java.exe" }
}

# Build the classpath from lib/ (excluding the DB drivers and the foreign
# SWT platforms) plus the test libraries.
$libJars = Get-ChildItem -Recurse -Filter *.jar -Path "lib" |
  Where-Object {
    $_.FullName -notmatch '\\h2\\'      -and
    $_.FullName -notmatch '\\mariadb\\' -and
    $_.FullName -notmatch '\\mysql\\'   -and
    $_.FullName -notmatch 'linux'       -and
    $_.FullName -notmatch 'macos'
  } |
  ForEach-Object { $_.FullName }
$testJars = Get-ChildItem "test\lib\*.jar" | ForEach-Object { $_.FullName }
$cp = (@($libJars) + @($testJars)) -join [IO.Path]::PathSeparator

# Determine the test classes to run. Either from the command line or by
# scanning the test directory.
if ($args.Count -gt 0)
{
  $testClasses = $args
  $testSources = Get-ChildItem -Recurse -Filter *Test.java -Path "test" | ForEach-Object { $_.FullName }
}
else
{
  $testDir = (Resolve-Path "test").Path
  $files = Get-ChildItem -Recurse -Filter *Test.java -Path "test"
  if ($files.Count -eq 0) { Write-Error "No tests found"; exit 1 }
  $testSources = $files | ForEach-Object { $_.FullName }
  $testClasses = $files |
    ForEach-Object { $_.FullName.Substring($testDir.Length + 1) -replace '\\','.' -replace '\.java$','' }
}

# Compile.
$out = "test-bin"
if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null

$sourcepath = "src" + [IO.Path]::PathSeparator + "test"
& $javac -encoding ISO-8859-1 -nowarn -cp $cp -sourcepath $sourcepath -d $out @testSources
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed"; exit $LASTEXITCODE }

# Run.
$runCp = (@($out) + @($libJars) + @($testJars)) -join [IO.Path]::PathSeparator
& $java -cp $runCp org.junit.runner.JUnitCore @testClasses
exit $LASTEXITCODE
