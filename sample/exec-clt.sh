# build commandline tool
set -e
cd ..
./gradlew clt:shadowJar
cd ./sample
./gradlew clean app:assembleProDebug -g ./build/gradle-cache
chmod +x ../clt/build/libs/clt-1.1-SNAPSHOT-all.jar
java -jar ../clt/build/libs/clt-1.1-SNAPSHOT-all.jar --setting-file "./app-size-config/app-size-settings.yml" --device-name "universal" --tag "tag" --modules --apk
