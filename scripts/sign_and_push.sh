#!/bin/bash

## This script is used for signing the apk with the platform keys and pushing it into System partition.
## Used for testing purposes.

./gradlew assembleDebug
rm PrivacyCentral.apk
wait ${!}
apksigner sign --key lineage_keys/platform.pk8 --cert lineage_keys/target_product_security_platform.x509.pem --out PrivacyCentral.apk app/build/outputs/apk/e/debug/app-e-debug.apk
wait ${!}
adb root
wait ${!}
adb devices
wait ${!}
adb install -r PrivacyCentral.apk
wait ${!}
adb remount && adb push privapp-permissions-foundation.e.privacycentralapp.xml system/etc/permissions
wait ${!}