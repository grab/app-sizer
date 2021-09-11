# App Sizer Sample Project

This sample project demonstrates the integration and usage of the App Sizer tool, which analyzes the download size of Android applications. By providing detailed insights into the composition of your app's binary, App Sizer helps developers identify areas for size reduction, ultimately improving user acquisition and retention rates.

## Project Structure

```
sample/
├── app/
├── android-module-level1/
├── android-module-level2/
├── kotlin-module/
```

## Using App Sizer

This sample project demonstrates two ways to use App Sizer: via the Gradle plugin and via the CLI tool.

### Gradle Plugin Integration

The App Sizer tool is configured in the `build.gradle` file of the `app` module. Look for the `appSizer` block:

```groovy
appSizer {
    projectInput {
        // Configuration options
    }
    metrics {
        // Output configuration
    }
}
```

To run the App Sizer analysis using the Gradle plugin:

1. Open a terminal in the sample project directory.
2. Execute the following command:
   ```
   ./gradlew app:appSizeAnalysisProRelease --no-configure-on-demand
   ```

Refer to the [App Sizer Plugin documentation](../docs/plugin.md) for detailed configuration options.

### CLI Tool Integration

This sample project also includes a configuration for the App Sizer CLI tool.

1. The configuration file for the CLI tool is located at:
   ```
   ./app-size-config/app-size-settings.yml
   ```
   You can modify this file to adjust the CLI tool's settings. Refer to the [App Sizer CLI documentation](../docs/cli.md) for detailed configuration options.

2. To execute the CLI analysis, run the following command from the project root:
   ```
   sh exec-clt.sh
   ```

This script will build & run the App Sizer CLI tool using the configuration specified in `app-size-settings.yml`.


## Understanding the Results

After running the analysis, you can find the results in:

For the Gradle plugin:
- InfluxDB (if configured)
- Markdown report: `app/build/sizer/reports/[option]-report.md`
- JSON report: `app/build/sizer/reports/[option]-metrics.json`

For the CLI tool:
- Markdown report: `[root-project]/build/app-sizer/[option]-report.md`
- JSON report: `[root-project]/build/app-sizer/[option]-metrics.json`

## Module Ownership

The `module-owner.yml` file in the project root defines the ownership of different modules. This is used by App Sizer to attribute size contributions to different teams or components.
```yaml
Platform:
  - app
Team1:
  - android-module-level1
Team2:
  - android-module-level2
  - kotlin-module

```

## Additional Resources

- [App Sizer Documentation](../docs/index.md)
- [Configuring the Gradle Plugin](../docs/plugin.md)
- [Understanding the Reports](../docs/report.md)
- [App Sizer Limitations](../docs/limitation.md)