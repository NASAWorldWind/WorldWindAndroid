# Install APKs on Linux/Android Hybrid OS

Friday, October 14, 2016

These instructions document how to install a World Wind APK from a USB drive that has been loaded with the Android Debug Bridge (adb) and Linux bash scripts.

## Sample folder hierarchy on the USB Drive:

	drwxr-xr-x 1 bds_000 bds_000       0 Dec 31  1979 .
	dr-xr-xr-x 1 bds_000 bds_000       0 Oct 20 10:00 ..
	-rw-r--r-- 1 bds_000 bds_000 2237804 Sep  5 10:41 adb
	-rwxr-xr-x 1 bds_000 bds_000     669 Oct 14 08:25 install-worldwind-examples.sh
	-rwxr-xr-x 1 bds_000 bds_000     826 Oct 14 08:25 install-worldwind-tutorials.sh
	drwxr-xr-x 1 bds_000 bds_000       0 Oct 14 06:22 nasa

## Update the USB Drive

1. Build the World Wind artifacts
   * worldwind
   * worldwind-examples
   * worldwind-tutorials
2. Copy the updated debug artifact(s) to the /nasa folder on the USB drive
   1. Copy the .apk(s) to the <USB media>nasa/ folder
      * For example copy <project root>/WorldWindAndroid/worldwind-examples/build/outputs/apk/worldwind-examples-debug.apk to <USB media>/nasa/
   2. Eject the USB media

## Update the Hybrid OS
1. Boot the Hybrid OS device (do not boot with the USB media attached)
   1. Power on the device
      * Booting with the USB media attached can generate a boot error: "error: no such partition".  If this occurs, power off the device, remove the media, and power on the device again.
   2. Wait for the device to boot to the Android environment
   3. Bring up a Linux Terminal
   4. Attach the USB media and note the device name in the Terminal, For example:
      * sdb: **sdb1**
   5. Login as root
      * \# login: **root**
   6. Mount the USB media using the previously noted device name
      * \# **setenforce 0**
      * \# **mount /dev/sdb1 /media**
    7. Run the desired installation script(s) from the root of the /media folder. The installation script will launch the .apk
      * \# **cd /media**
      * \# **./install-worldwind-examples.sh**
2. Validate the APK
    1. Return to the Android environment
    2. The APK should be running

## Reference
### Example Installation Script (install-worldwind-examples.sh):
    #!/bin/sh
    set -x #echo on
    #
    # Android Debug Bridge (adb) commands:
    # 	https://developer.android.com/studio/command-line/adb.html#commandsummary
    # Package Manager:
    # 	https://developer.android.com/studio/command-line/shell.html#pm
    # Activity manager:
    # 	https://developer.android.com/studio/command-line/shell.html#am
    ./adb connect <ip address>
    ./adb push nasa/worldwind-examples-debug.apk /data/local/tmp/gov.nasa.worldwindx.examples
    # -r: Reinstall an existing app, keeping its data.
    ./adb shell pm install -r "/data/local/tmp/gov.nasa.worldwindx.examples"
    # INTENT arguments: -n <COMPONENT>, -a <ACTION>, -c <CATEGORY>
    ./adb shell am start -n "gov.nasa.worldwindx.examples/gov.nasa.worldwindx.GeneralGlobeActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER\

### Example Installation Script (install-worldwind-tutorials.sh):
    #!/bin/sh
    set -x #echo on
    #
    # adb commands:
    # 	https://developer.android.com/studio/command-line/adb.html#commandsummary
    # Package Manager:
    # 	https://developer.android.com/studio/command-line/shell.html#pm
    # Activity manager:
    # 	https://developer.android.com/studio/command-line/shell.html#am
    ./adb connect <ip address>
    # Copy apk to device.
    ./adb push nasa/worldwind-tutorials-debug.apk /data/local/tmp/gov.nasa.worldwindx.tutorials
    # -r: Reinstall an existing app, keeping its data.
    ./adb shell pm install -r "/data/local/tmp/gov.nasa.worldwindx.tutorials"
    # INTENT arguments: -n <COMPONENT>, -a <ACTION>, -c <CATEGORY>
    ./adb shell am start -n "gov.nasa.worldwindx.tutorials/gov.nasa.worldwindx.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
