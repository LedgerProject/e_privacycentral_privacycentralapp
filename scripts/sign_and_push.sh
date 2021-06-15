#!/bin/bash

## This script is used for signing the apk with the platform keys and pushing it into System partition.
## Used for testing purposes.

## Note: It is used only to build /e/ OS variant build.

./gradlew assembleEDebug
adb root
wait ${!}
adb devices
wait ${!}
adb install -r build/outputs/apk/e/debug/PrivacyCentral-e-debug-1.0.0-alpha.apk
wait ${!}
adb remount && adb push privapp-permissions-foundation.e.privacycentralapp.xml system/etc/permissions
wait ${!}
adb shell am start -n "foundation.e.privacycentralapp.e/foundation.e.privacycentralapp.main.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
