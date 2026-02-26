$versionFile = "E:\Dev\AdobeCampaignWorkbench\version.properties"
$props = @{}
Get-Content $versionFile | ForEach-Object {
    if ($_ -match "(.+)=(.+)") {
        $props[$matches[1]] = $matches[2]
    }
}
$version = "$($props.major).$($props.minor).$($props.patch).$($props.build)"
# $version = "$($props.major).$($props.minor).$($props.build)"
jpackage --name "Campaign Workbench" --app-version $version --vendor "Specsavers" --description "Develop, execute, and test Adobe Campaign email templates, ETM modules, and personalisation blocks" --input "E:\Dev\AdobeCampaignWorkbench\out\artifacts\AdobeCampaignWorkbench_jar" --main-jar AdobeCampaignWorkbench.jar --main-class com.campaignworkbench.ide.CampaignWorkbenchIDE --icon "E:\Dev\AdobeCampaignWorkbench\resources\app.ico" --type msi --java-options "--module-path E:\Dev\Java\javafx-sdk-25.0.2\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics" --dest "E:\Dev\AdobeCampaignWorkbench\installer" --win-dir-chooser --win-menu --win-shortcut --win-shortcut-prompt --win-console --win-per-user-install --win-upgrade-uuid "dfb8d89d-c5ac-4cd3-810e-e73b857e1a51"