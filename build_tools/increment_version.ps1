$versionFile = "$PSScriptRoot\..\version.properties"

# ------------------------------------------------------------
# Update or Insert version in Version.java
# ------------------------------------------------------------


# Read properties
$props = @{}
Get-Content $versionFile | ForEach-Object {
    if ($_ -match "(.+)=(.+)") {
        $props[$matches[1]] = $matches[2]
    }
}

# Increment build
$props["build"] = [int]$props["build"] + 1

# Write back
$props.GetEnumerator() | ForEach-Object {
    "$($_.Key)=$($_.Value)"
} | Set-Content $versionFile

# Construct version string
$version = "$($props.major).$($props.minor).$($props.patch).$($props.build)"

Write-Host "New Version: $version"

# Generate Version.java
$javaOut = "$PSScriptRoot\..\src\com\campaignworkbench\ide\Version.java"

@"
package com.campaignworkbench.ide;

public final class Version {
    public static final int MAJOR = $($props.major);
    public static final int MINOR = $($props.minor);
    public static final int PATCH = $($props.patch);
    public static final int BUILD = $($props.build);

    public static final String VERSION =
            MAJOR + "." + MINOR + "." + PATCH + "." + BUILD;
}
"@ | Set-Content $javaOut

# ------------------------------------------------------------
# Update or Insert Implementation-Version in MANIFEST.MF
# ------------------------------------------------------------
$manifestPath = "$PSScriptRoot\..\resources\META-INF\MANIFEST.MF"

if (Test-Path $manifestPath) {

    $content = Get-Content $manifestPath -Raw

    if ($content -match "(?m)^Implementation-Version:\s*.+$") {
        # Replace existing
        $content = [regex]::Replace(
                $content,
                "(?m)^Implementation-Version:\s*.+$",
                "Implementation-Version: $version"
        )
    }
    else {
        # Remove trailing whitespace/newlines
        $content = $content.TrimEnd()

        # Append directly (NO blank line)
        $content += "`r`nImplementation-Version: $version"
    }

    # Ensure file ends with CRLF
    if (-not $content.EndsWith("`r`n")) {
        $content += "`r`n"
    }

    $encoding = New-Object System.Text.ASCIIEncoding
    [System.IO.File]::WriteAllText($manifestPath, $content, $encoding)

    Write-Host "MANIFEST.MF updated."
}