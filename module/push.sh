#!/bin/bash
set -e
cd ${0%/*}
VERSION=$(cat ../app/build.gradle| grep versionName | sed -E 's/.+"(.+)".*/\1/g')
echo VERSION: $VERSION
bash ./build.sh ./src/gradle/qq.gradle
./MagiskModuleTemplate/gradlew -p ./MagiskModuleTemplate clean :module:pushRelease -PVERSION=$VERSION

bash ./build.sh ./src/gradle/alipay.gradle
./MagiskModuleTemplate/gradlew -p ./MagiskModuleTemplate clean :module:pushRelease -PVERSION=$VERSION

bash ./build.sh ./src/gradle/taobao.gradle
./MagiskModuleTemplate/gradlew -p ./MagiskModuleTemplate clean :module:pushRelease -PVERSION=$VERSION

bash ./build.sh ./src/gradle/wechat.gradle
./MagiskModuleTemplate/gradlew -p ./MagiskModuleTemplate clean :module:pushRelease -PVERSION=$VERSION