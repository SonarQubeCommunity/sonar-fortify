Fortify SCA Plugin for SonarQube
================================

Download and Versions information: http://update.sonarsource.org/plugins/fortify-confluence.html

## Note
The current version of the plugin does not allow the import of rule definitions any longer. As a consequence, it is *no longer usable*. A new version will be released soon to fix this issue.

## Description / Features
This plugin imports Fortify SSC rules descriptions and SCA reports into SonarQube:
* Import vulnerability issues as SonarQube issues. Supported languages are ABAP, C#, C++, Cobol, Java, JavaScript, Python and VB.
* Compute the Fortify Security Rating, value between 1 and 5
* Compute the number of issues marked as critical, high, medium and low priority in Fortify

### The plugin does not trigger Fortify scans
As stated in the description above, this plugin imports audit reports. As a consequence, *Fortify scans must have been run before executing this plugin on SonarQube*.
The plugin has been developed and tested with *Fortify 2.50*. Older versions might also work (feel free to tell us on the user mailing list if you managed to make it work in this case).

## Usage
### Configure and run analysis
The SCA command-line, named "sourceanalyzer", must be executed before SonarQube analyzer. The generated report (FPR or VFDL file) is parsed to convert Fortify vulnerabilities to SonarQube issues. By nature SonarQube issues relate to rules that are activated in Quality profiles. For this reason don't forget to activate the Fortify rules in the selected Quality Profiles. Note that severity of rules are taken from Fortify report so the severity configured in quality profile is ignored.

The path to the Fortify report is set by the property "sonar.fortify.reportPath". Path is absolute or relative to the module base directory. If the property is missing then the plugin is disabled.

#### Example
    sonar-runner -Dsonar.fortify.reportPath=/path/to/project.fpr
Something like the following should appear in the log:
```
10:20:44 10:20:35.588 INFO  - Sensor Fortify sensor...
10:20:44 10:20:35.589 INFO  - Process Fortify report...
10:20:45 10:20:37.318 INFO  - Process Fortify report done: 1729 ms
10:20:45 10:20:37.319 INFO  - Sensor Fortify sensor done: 1731 ms
```
