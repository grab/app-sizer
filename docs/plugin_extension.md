# App Sizer Plugin
App Sizer provide the app sizer gradle plugin as the interface to seamlessly integrates with your Android Gradle project. 
This option is the recommendation to use the app-sizer tool.
Bellow are the instruction to config the plugin properly via the gradle plugin extension.

## App Sizer Plugin Extension

Use the registered `appSizeAnalysis` extension block in root `build.gradle` to configure App Sizer Plugin

```groovy
appSizeAnalysis {
    enabled = true 
    projectInput {
        // config the input for the plugin
    }
    metrics {
        // config the output for the plugin
    }
}
```
* **enabled flag**: Given the App Sizer Plugin has not supported configuration on demand & configuration catching. We provide you an option to turned of the plugin just in case it impact your gradle configuration performance.
* **projectInput**: This block to provide the input for the app-sizer tool 
* **metrics**: This block to provide the output configuration for the app-sizer tool

Let go to each block for further detail.

### Project Input
App Sizer request inputs to function
```groovy
appSizeAnalysis {
    projectInput {
        largeFileThreshold = 10
        teamMappingFile = file("${rootProject.rootDir}/module-owner.yml")
        enableMatchDebugVariant = true
        variantFilter { variant ->
            variant.setIgnore(variant.flavors.contains("ignore-flavor"))
        }
        apk {
            // configuration to generate the apk file
        }
    }
    ...
}
```
Included:
* **largeFileThreshold**: Any file in the project is larger than this threshold will be considered as a large file. Expecting the threshold set by bytes
* **teamMappingFile**: A simple yml file to map the project modules to the owner, the team name follow with the module name. Below is an example
```yaml
Platform:
  - app
Team1:
  - android-module-level1
  - kotlin-module
Team2:
  - android-module-level2
```
* **enableMatchDebugVariant**: Default value is false. Given the App Sizer use the AAR files built from your project module as the input. 
Building the module's AAR file under release build type usually take more time, or some time it's problematic. 
Such as the verifyResourceRelease will be executed and might fail your build, while building the AAR file under debug build type or build the app bundle file will not involve similar task. 
From the App Sizer viewpoint there is not much different between the AAR file was built under Debug or Release build type, unless you have special configuration. Enable this flag will let the plugin build the debug AAR whether you build a production build or debug build. The main target is improve performance + prevent the build failure due to verifyResourceRelease task. 
* **Variant Filter**: `variantFilter {}` can be used to specify the variants that should be excluded from configuration. It's for performance improvement on large scale project.

### Config to generate Apks
To function, the tool has to generate the Apks from the app bundle file, the apk block provide the necessary input to generate the APK Set archive.
They are the [bundletool][bundletool] and the device configurations. 
```groovy
appSizeAnalysis {
    projectInput {
        apk {
            deviceSpecs = [
                    file("${rootProject.rootDir}/app-size-config/device-1.json"),
                    file("${rootProject.rootDir}/app-size-config/device-2.json")
            ]
            bundleToolFile = file("${rootProject.rootDir}/binary/bundletool-all-1.15.4.jar")
        }
    }
    ...
}
```
* **deviceSpecs**: A list of device specs that you want to run the analysis on
* **bundleToolFile**: The [bundletool][bundletool] file


## Output configuration
The `metrics {}` block is to config the output.  

```groovy
grazel {
    ...
    metrics {
        influxDB {
            dbName = "sizer"
            reportTableName = "app_size"
            url = "http://localhost:8086"
            username = "root"
            password = "root"
        }
        local{
            outputDirectory = project.layout.buildDirectory.dir("app-sizer")
        }
        customAttributes.putAll(
                ["pipeline_id": "1001"]
        )
    }
}
```
### InfluxDB 
The `influxDB{}` block to provide the 

## A sample of full configuration 

```groovy
appSizeAnalysis {
    enabled = true
    projectInput {
        apk {
            bundleToolFile = file("${rootProject.rootDir}/binary/bundletool-all-1.15.4.jar")
            deviceSpecs = [
                    file("${rootProject.rootDir}/app-size-config/device-1.json"),
                    file("${rootProject.rootDir}/app-size-config/device-2.json")
            ]
        }
        variantFilter { variant ->
            variant.setIgnore(variant.flavors.contains("gea"))
        }
        enableMatchDebugVariant = true
        largeFileThreshold = 10
        teamMappingFile = file("${rootProject.rootDir}/module-owner.yml")
    }
    metrics {
        influxDB {
            dbName = "sizer"
            reportTableName = "app_size"
            url = "http://localhost:8086"
            username = "root"
            password = "root"
        }
        local{
            outputDirectory = project.layout.buildDirectory.dir("app-sizer")
        }
        customAttributes.putAll(
                ["pipeline_id": "1001"]
        )
    }
}
```
[bundletool]: https://github.com/google/bundletool
