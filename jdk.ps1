param (
    [string]$version,
    [string]$jar,
    [string]$jvmParams,
    [string]$jarParams
)

$scriptDir = $PSScriptRoot
$jarAbsolute = (Resolve-Path $jar).Path
$command = "java"

if ($jvmParams) {
    $command += " $jvmParams"
}

$command += " -jar $scriptDir\jdk.jar -v $version -j `"$jarAbsolute`""

if ($jarParams) {
    $command += " $jarParams"
}

Write-Output $command
Invoke-Expression $command