# build commandline tool
#../gradlew clt:shadowJar
set -e
./gradlew app:assembleDebug -g ./build/gradle-cache
chmod +x ../clt/build/libs/clt-1.1-SNAPSHOT-all.jar
java -jar ../clt/build/libs/clt-1.1-SNAPSHOT-all.jar --setting-file "./app-size-settings.yml" --device-name "universal" --tag "tag" --modules
