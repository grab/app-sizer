# build commandline tool
set -e
cd ..
./gradlew clean clt:shadowJar
cd ./sample
./gradlew app:bundleProDebug -g ./build/gradle-cache
chmod +x ../clt/build/libs/clt-all.jar
java -jar ../clt/build/libs/clt-all.jar --config-file "./app-size-config/app-size-settings.yml"

