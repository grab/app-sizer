# build commandline tool
set -e
cd ..
./gradlew clt:shadowJar
cd ./sample
./gradlew app:bundleProDebug -g ./build/gradle-cache
chmod +x ../clt/build/libs/clt-SNAPSHOT-all.jar
java -jar ../clt/build/libs/clt-SNAPSHOT-all.jar --config-file "./app-size-config/app-size-settings.yml"

