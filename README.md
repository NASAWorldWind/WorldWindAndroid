# World Wind Android #

World Wind Android is a 3D virtual globe API for [Android](http://developer.android.com). You can use it to provide a
geographic context, complete with terrain, for visualizing geographic or geo-located information. World Wind Android
provides high-resolution terrain and imagery, retrieved from remote servers automatically as needed. You can also
provide your own terrain and imagery. World Wind Android additionally provides a rich collection of shapes that you can
use to represent information on the globe or in space.

# Important Sites

- [World Wind Examples](http://worldwindserver.net/android/latest/worldwind-examples.apk) runs on devices with Android
  4.4 (KitKat) and newer
- [World Wind Library](http://worldwindserver.net/android/latest/worldwind.aar) provides the World Wind Library as an Android Archive (AAR)
- [API Docs](http://worldwindserver.net/android/latest/doc) gives class level details for developers
- [JIRA](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/) provides requirement and issue tracking
- [World Wind Forum](http://forum.worldwindcentral.com) provides help from the World Wind community
- [Android Studio](http://developer.android.com/sdk/) is used for World Wind Android development

# Release 0.3.0, June 30, 2016

World Wind Android v0.3.0 adds new functionality and improvements designed to support large numbers placemarks.
Additionally, this release increases the memory available to application-defined placemark images, and reduces the
memory used by World Wind's built-in map background layers and WMS layers.

- [World Wind Examples 0.3.0](http://worldwindserver.net/android/0.3.0/worldwind-examples.apk) - Example App for 0.3.0; runs on Android 4.4 and newer
- [World Wind Library 0.3.0](http://worldwindserver.net/android/0.3.0/worldwind.aar) - Android Archive (AAR) for 0.3.0
- [API Docs 0.3.0](http://worldwindserver.net/android/0.3.0/doc) - Developer documentation for 0.3.0
- [JIRA 0.3.0](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/fixforversion/10913) - Detailed release notes for 0.3.0

#### Release Notes

###### Requirement
- [WWA-8](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-8) - Atom WMS server testing and support
- [WWA-22](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-22) - Android x86 support

###### Task
[WWA-78](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-78) - MIL-STD-2525 level of detail example
[WWA-88](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-88) - Update to Android Studio 2.x
[WWA-89](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-89) - Bitmap factory image source
[WWA-92](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-92) - Placemark level of detail selector
[WWA-93](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-93) - Example for placemark shared attributes and resource managment
[WWA-97](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-97) - Increase memory available to bitmap image sources
[WWA-98](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-98) - Reduce memory used by background images and WMS layer images

###### Bug
[WWA-90](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-90) - Crash on Nexus 9 when GL buffer object evicted from render cache
[WWA-95](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-95) - WmsGetMapUrlFactory URL query delimiter
[WWA-96](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-96) - WmsLayer ignores Globe object argument

# Hotfix 0.2.6, June 16, 2016

World Wind Android v0.2.6 contains critical bug fixes for v0.2.5. See the release notes for details.

- [World Wind Examples 0.2.6](http://worldwindserver.net/android/0.2.6/worldwind-examples.apk) - Example App for 0.2.6; runs on Android 4.4 and newer
- [World Wind Library 0.2.6](http://worldwindserver.net/android/0.2.6/worldwind.aar) - Android Archive (AAR) for 0.2.6
- [API Docs 0.2.6](http://worldwindserver.net/android/0.2.6/doc) - Developer documentation for 0.2.6
- [JIRA 0.2.6](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/fixforversion/10917) - Detailed release notes for 0.2.6

#### Release Notes

###### Bug
- [WWA-82](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-82) - Cancelled MotionEvents cause erratic navigation and exceptions
- [WWA-83](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-83) - FrustumTest unit test fails to compile
- [WWA-84](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-84) - Rhumb line distance and location calculations occasionally wrong on E/W courses
- [WWA-85](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-85) - NavigatorEvents are not detectable for all gestures

# Release 0.2.5, May 31, 2016

World Wind Android v0.2.5 provides support for 'picking', a feature that enables applications to determine the World
Wind objects displayed at a screen point. This capability is accessible via the method [WorldWindow.pick](http://worldwindserver.net/android/0.2.5/doc/gov/nasa/worldwind/WorldWindow.html#pick-float-float-).
Along with the core capability, two examples demonstrate how developers can use picking in their own apps:
PlacemarksPickingActivity and PlacemarksSelectDragActivity.

- [World Wind Examples 0.2.5](http://worldwindserver.net/android/0.2.5/worldwind-examples.apk) - Example App for 0.2.5; runs on Android 4.4 and newer
- [World Wind Library 0.2.5](http://worldwindserver.net/android/0.2.5/worldwind.aar) - Android Archive (AAR) for 0.2.5
- [API Docs 0.2.5](http://worldwindserver.net/android/0.2.5/doc) - Developer documentation for 0.2.5
- [JIRA 0.2.5](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/fixforversion/10916) - Detailed release notes for 0.2.5

#### Release Notes

###### Requirement
- [WWA-16](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-16) - Picking

###### Bug
- [WWA-76](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-76) - Blank screen when EGL context lost

# Release 0.2.0, May 13, 2016

World Wind Android v0.2.0 adds support for screen placemarks and navigation events. The World Wind Android v0.2.0
examples demonstrate support for MIL-STD-2525C tactical icons by using NASA World Wind's point placemark to display
images generated by the US Army Mission Command's [MIL-STD-2525 symbol rendering library](https://github.com/missioncommand/mil-sym-android).

This release establishes World Wind support for threaded rendering. Applications are now able to access World Wind
components from the Activity thread without any synchronization, while World Wind executes OpenGL commands on Android's
OpenGL rendering thread.

- [World Wind Examples 0.2.0](http://worldwindserver.net/android/0.2.0/worldwind-examples.apk) - Example App for 0.2.0; runs on Android 4.4 and newer
- [World Wind Library 0.2.0](http://worldwindserver.net/android/0.2.0/worldwind.aar) - Android Archive (AAR) for 0.2.0
- [API Docs 0.2.0](http://worldwindserver.net/android/0.2.0/doc) - Developer documentation for 0.2.0
- [JIRA 0.2.0](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/fixforversion/10912) - Detailed release notes for 0.2.0

#### Release Notes

###### Requirement
- [WWA-4](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-4) - Navigation Events
- [WWA-11](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-11) - Screen placemark
- [WWA-33](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-33) - MIL-STD-2525 support

###### Task
- [WWA-68](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-68) - Bitmap image source
- [WWA-71](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-71) - Threaded rendering
- [WWA-72](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-72) - Performance metrics

###### Bug
- [WWA-70](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-70) - WMS layer makes requests outside layer bounds

# Release 0.1.0, March 30, 2016

World Wind Android's initial prototype release. This release establishes the baseline for World Wind Android
development. It provides a basic navigable 3D globe capable of displaying imagery from OGC Web Map Service (WMS) layers,
has support for displaying local and remote images on the globe, and provides a rich interface for manipulating the
virtual camera.

- [World Wind Examples 0.1.0](http://worldwindserver.net/android/0.1.0/worldwind-examples.apk) - Example App for 0.1.0; runs on Android 4.4 and newer
- [World Wind Library 0.1.0](http://worldwindserver.net/android/0.1.0/worldwind.aar) - Android Archive (AAR) for 0.1.0
- [API Docs 0.1.0](http://worldwindserver.net/android/0.1.0/doc) - Developer documentation for 0.1.0
- [JIRA 0.1.0](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA/fixforversion/10911) - Detailed release notes for 0.1.0

#### Release Notes

###### Requirement
- [WWA-2](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-2) - Surface image
- [WWA-3](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-3) - Navigation
- [WWA-6](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-6) - WMS image layer
- [WWA-20](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-20) - Android example apps

###### Task
- [WWA-57](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-57) - Android project structure
- [WWA-59](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-59) - Rendering infrastructure
- [WWA-60](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-60) - Vector and geographic primitives
- [WWA-61](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-61) - Navigation gesture detectors
- [WWA-62](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-62) - WorldWindow
- [WWA-63](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-63) - Global tessellation
- [WWA-64](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-64) - Units tests for version 0.1.0
- [WWA-65](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-65) - Stub classes for version 0.1.0
- [WWA-66](http://issues.worldwind.arc.nasa.gov/jira/browse/WWA-66) - Logging infrastructure
