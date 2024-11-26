#!/bin/bash

#
# MIT License
#
# Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
#
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE
#

# Exit immediately if a command exits with a non-zero status
set -e

# Print informative messages
echo "Starting App Sizer CLI execution script..."

# Change to the parent directory (assuming we're in the sample project directory)
echo "Changing to parent directory..."
cd ..

# Build the command-line tool
echo "Building App Sizer command-line tool..."
./gradlew clean cli:shadowJar

# Change back to the sample project directory
echo "Changing back to sample project directory..."
cd ./sample

# Build the Android app bundle
echo "Building Android app bundle..."
./gradlew app:bundleProRelease -g ./build/gradle-cache

# Make the CLI tool executable
echo "Making CLI tool executable..."
chmod +x ../cli/build/libs/cli-app-sizer.jar

# Run the App Sizer CLI tool
echo "Running App Sizer CLI tool..."
java -jar ../cli/build/libs/cli-app-sizer.jar --config-file "./app-size-config/app-size-settings.yml"

# Print completion message
echo "App Sizer CLI execution completed successfully!"
echo "You can find the analysis results in the ./build/app-sizer directory."