# PrivacyCentralApp
An app to let you control and protect your privacy.

# Features
The following features are currently part of PrivacyCentral app.
1. Centralized overview dashboard.
2. Fake location.
3. Hiding IP address.
4. Manage granular permissions.
5. Control trackers across apps.

# Technologies
- Kotlin as main language
- Kotlin coroutines and flow for asynchronous code
- AndroidX (core-ktx, fragment-ktx, and lifecycle etc.)
- Google Material Design component for UI elements
- Timber for logging
- Room for database (may use datastore too)
- Hilt for DI
- MapBox for map support
- LeakCanary for keeping an eye on memory leaks.
- Junit for unit testing
- Espresso for integration testing.
- 

# Flavours
This app comes in two flavours, one for /e/OS and second one for other android (where app doesn't have system access). For more information refer to [Architecture Guide](DEVELOPMENT.md)

# Testing
Need to write test and add code coverage.

# Development

## Setup
You can use any latest stable version of android studio to be able to build this app.

## Supported Versions
- Minimum SDK: 24 (Android N)
- Compile SDK: 30 (Android R)
- Target SDK: 30 (Android R)

## API Keys
This project uses [Mapbox](https://docs.mapbox.com/android/maps/guides/install/) sdk for displaying maps. To download and use the mapbox sdk, you need to supply API key and secret and set them as follows:

### For local build
You can set them in local.properties
```
MAPBOX_KEY=<insert mapbox public key>
MAPBOX_SECRET_KEY=<insert mapbox secret key>
```
**IMP: Never add this file to version control.**

### For CI build
When building in CI environment, we don't have local.properties file. So the following environment variables must be set:
```
export MAPBOX_KEY=<insert mapbox public key>
export MAPBOX_SECRET_KEY=<insert mapbox secret key>
```

## Code Style and Quality
This project uses [ktlint](https://github.com/pinterest/ktlint), provided via the [spotless](https://github.com/diffplug/spotless) gradle plugin. To maintain the same code style, all the codestyle configuration has been bundled into the project.

To check for any codestyle related error, `./gradlew spotlessCheck`. Use `./gradlew spotlessApply` to autoformat in order to fix any code quality related issues.

### Setting up pre-commit hooks
To strictly enforce the code quality, this project has a pre-commit hook which is triggered everytime before you commit any changes (only applies to *.kt, *.gradle, *.md and *.gitignore). You must setup the pre-commit hook before doing any changes to the project. For that, this project has a script which can executed as follows:
```bash
hooks/pre-commit
```

## Build
If you'd like to build PrivacyCentral locally, you should be able to just clone and build with no issues.

For building from CLI, you can execute this command:
```bash
./gradlew build
```

## How to use PrivacyCentral apk
You can build the apk locally by using above instructions or you can download the latest stable apk from `master` branch pipeline.

### To run apk on /e/OS devices
PrivacyCentral needs to be installed as system app and whitelisting in order to grant some system specific permissions. Follow these steps to make it work properly on /e/OS

1. From `Developer options`, enable `Android debugging` and `Rooted debugging`
1. Sign the apk with platform certificate. You can use this command to do that

    ```shell
    apksigner sign --key platform.pk8 --cert platform.x509.pem PrivacyCentral.apk app-e-release-unsigned.apk
    ```

    If you are running your tests on an `/test` build, you can find keys at https://gitlab.e.foundation/e/os/android_build/-/tree/v1-q/target/product/security
1. Install apk as system app and push permissions whitelist with following commands:
    ```shell
    adb root && adb remount
    adb shell mkdir system/priv-app/PrivacyCentral
    adb push PrivacyCentral.apk system/priv-app/PrivacyCentral
    ```

1. Push permissions whitelist.
    - it requires the whitelisting [privapp-permissions-foundation.e.privacycentralapp.xml](privapp-permissions-foundation.e.privacycentralapp.xml) file that can be found in the project repository.
    - then use the following command
        ```bash
        adb push privapp-permissions-foundation.e.privacycentralapp.xml /system/etc/permissions/
        ```
1. Reboot the device
    ```shell
    adb reboot
    ```

### To run apk on stock android devices
You can simply install the apk. Keep in that mind all features won't be available on stock android devices.

> Volla!!!, PrivacyCentral is installed successfully in your device.

# Distribution
This project can be distributed as prebuilt apk with /e/OS or it can be published on other app stores for non /e/OS devices.

# For developers and contributers
Please refer to [Development Guide](DEVELOPMENT.md) for detailed instructions about project structure, architecture, design patterns and testing guide.

# License
```
Copyright (C) 2021 E FOUNDATION

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program.  If not, see https://www.gnu.org/licenses/.
```
