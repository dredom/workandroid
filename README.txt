Notes on Android dev.

To install app on phone:

- on phone Applications:
 + Allow Unknown sources for apps
 + Development / USB debugging
Connect USB to phone, select Disk Drive.

On workstation,
 androidsdk> ./platform-tools/adb devices
Should list the handset as a device.

Then can go to eclipse and simply

 Run as Adroid application

This packages it to apk, installs it on handset and launches it.

Logging:
 ./platform-tools/adb logcat

Notes on Package name:
 Maybe need unique pkg name per app? See > adb uninstall <packagename>
 Maybe combo of Android Project + Library Projects can share pkg name.
