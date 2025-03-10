#!/bin/bash
set -e
cd ${0%/*}

# bash ./build.sh :module:flashDebug ./src/gradle/qq.gradle        Riru
# bash ./build.sh :module:flashDebug ./src/gradle/alipay.gradle    Riru
# bash ./build.sh :module:flashDebug ./src/gradle/taobao.gradle    Riru
# bash ./build.sh :module:flashDebug ./src/gradle/wechat.gradle    Riru
# bash ./build.sh :module:flashDebug ./src/gradle/unionpay.gradle  Riru
bash ./build.sh :module:flashDebug ./src/gradle/qq.gradle        Zygisk
bash ./build.sh :module:flashDebug ./src/gradle/alipay.gradle    Zygisk
bash ./build.sh :module:flashDebug ./src/gradle/taobao.gradle    Zygisk
bash ./build.sh :module:flashDebug ./src/gradle/wechat.gradle    Zygisk
bash ./build.sh :module:flashDebug ./src/gradle/unionpay.gradle  Zygisk