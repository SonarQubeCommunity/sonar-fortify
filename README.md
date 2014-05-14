Fortify SCA Plugin for SonarQube
================================

A plugin to integrate the vulnerabilities detected by Fortify SCA into SonarQube.

## Install

SonarQube server loads rule definitions from Fortify rulepacks. Rulepacks are :

* XML files implemented by end-users to define custom rules.
* BIN files provided by HP. They are encrypted XML files.  

The SonarQube plugin is able to load the XML files, so BIN files must be beforehand manually uncompressed. Paths to the XML files (or to their parent directory) must be set in the property "sonar.fortify.rulepackPaths" of conf/sonar.properties. Value is a comma-separated list of absolute paths to XML files or to directories containing XML files.
As a consequence SonarQube server must be restarted each time a rulepack is updated in Fortify.

Example
```
sonar.fortify.rulepackPaths=/path/to/fortify/rulepacks,/path/to/rulepack.xml
```

When server is restarted, the Fortify rules are listed in the "Quality Profiles" page.

### Uncompress Rulepacks

The following command extracts XML files from BIN files : 

```
java -cp rulepack-uncompress-2.0.jar:/path/to/Fortify/Core/lib/fortify-crypto-1.0.jar org.sonar.fortify.uncompress.CLI /path/to/rulepacks
```

Note that the parameter is the path to the directory containing BIN files. Related XML files are extracted in the same directory by default. The output directory can be customized by setting a second parameter on the command-line : 

```
java -cp rulepack-uncompress-2.0.jar:/path/to/Fortify/Core/lib/fortify-crypto-1.0.jar org.sonar.fortify.uncompress.CLI /path/to/rulepacks/dir /path/to/output/dir
```

## Analyze

The SCA command-line, named "sourceanalyzer", must be executed before SonarQube analyzer. The generated report (FPR or VFDL file) is parsed to convert Fortify vulnerabilities to SonarQube issues. 
By nature SonarQube issues relate to rules that are activated in Quality profiles. For this reason don't forget to activate the Fortify rules in the selected Quality profiles.

The path to the Fortify report is set by the property "sonar.fortify.reportPath". Path is absolute or relative to the module base directory. If the property is missing then the plugin is disabled.

Example
```
  sonar-runner -Dsonar.fortify.reportPath=/path/to/project.fpr
```
