#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Print informative messages
echo "Starting App Sizer CLI execution script..."

# Change to the parent directory (assuming we're in the sample project directory)
echo "Changing to parent directory..."
cd ..

# Build the command-line tool
echo "Building App Sizer command-line tool..."
./gradlew clean clt:shadowJar

# Change back to the sample project directory
echo "Changing back to sample project directory..."
cd ./sample

# Build the Android app bundle
echo "Building Android app bundle..."
./gradlew app:bundleProRelease -g ./build/gradle-cache

# Make the CLI tool executable
echo "Making CLI tool executable..."
chmod +x ../clt/build/libs/clt-SNAPSHOT-08-all.jar

# Run the App Sizer CLI tool
echo "Running App Sizer CLI tool..."
java -jar ../clt/build/libs/clt-SNAPSHOT-08-all.jar --config-file "./app-size-config/app-size-settings.yml"

# Print completion message
echo "App Sizer CLI execution completed successfully!"
echo "You can find the analysis results in the ./build/app-sizer directory."