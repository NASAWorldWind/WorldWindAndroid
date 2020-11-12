<img src="https://worldwind.arc.nasa.gov/img/nasa-logo.svg" height="100"/>

# WorldWind Android

## NASA Renews Commitment to WorldWind
WorldWind has received renewed commitment from NASA for continued development and support.  NASA will continue to release updates to the WorldWind SDKs, and will continue to make the WorldWind Servers accessible for all Users.  We plan to make an announcement soon regarding a path forward for improved support of the WorldWind User & Developer communities.

If you have questions and/or concerns, please feel free to email at:

worldwind-info@lists.nasa.gov

[![Build Status](https://travis-ci.com/NASAWorldWind/WorldWindAndroid.svg?branch=develop)](https://travis-ci.com/NASAWorldWind/WorldWindAndroid)
[![Download](https://api.bintray.com/packages/nasaworldwind/maven/WorldWindAndroid/images/download.svg)](https://bintray.com/nasaworldwind/maven/WorldWindAndroid/_latestVersion)

3D virtual globe API for Android, developed by NASA. Provides a geographic context with high-resolution terrain, for
visualizing geographic or geo-located information in 3D and 2D. Developers can customize the globe's terrain and
imagery. Provides a collection of shapes for displaying and interacting with geographic data and representing a range of
geometric objects.

- [worldwind.arc.nasa.gov](https://worldwind.arc.nasa.gov) has setup instructions, developers guides, API documentation and more
- [WorldWind Forum](https://forum.worldwindcentral.com) provides help from the WorldWind community
- [Android Studio](https://developer.android.com/sdk/) is used by the NASA WorldWind development team

## Download

Download the [latest release](https://bintray.com/nasaworldwind/maven/WorldWindAndroid/_latestVersion) or grab via Gradle:
```groovy
compile 'gov.nasa.worldwind.android:worldwind:0.8.0'
```

## Snapshots

Get development build snapshots with the latest features and bug fixes from [oss.jfrog.org](https://oss.jfrog.org/):
```groovy
repositories {
    maven {
        url 'https://oss.jfrog.org/artifactory/oss-snapshot-local'
    }
}

...

compile 'gov.nasa.worldwind.android:worldwind:0.9.0-SNAPSHOT'
```

## Releases and Roadmap

Official WorldWind Android releases have the latest stable features, enhancements and bug fixes ready for production use.

- [GitHub Releases](https://github.com/NASAWorldWind/WorldWindAndroid/releases/) documents official releases
- [GitHub Milestones](https://github.com/NASAWorldWind/WorldWindAndroid/milestones) documents upcoming releases and the development roadmap
- [Bintray](https://bintray.com/nasaworldwind/maven/WorldWindAndroid) contains official release binaries and Maven artifacts
- [Travis CI](https://travis-ci.com/NASAWorldWind/WorldWindAndroid) provides continuous integration and release automation

## License

    NASA WORLDWIND

    Copyright (C) 2001 United States Government
    as represented by the Administrator of the
    National Aeronautics and Space Administration.
    All Rights Reserved.

    NASA OPEN SOURCE AGREEMENT VERSION 1.3

    This open source agreement ("agreement") defines the rights of use, reproduction,
    distribution, modification and redistribution of certain computer software originally
    released by the United States Government as represented by the Government Agency
    listed below ("Government Agency"). The United States Government, as represented by
    Government Agency, is an intended third-party beneficiary of all subsequent
    distributions or redistributions of the subject software. Anyone who uses, reproduces,
    distributes, modifies or redistributes the subject software, as defined herein, or any
    part thereof, is, by that action, accepting in full the responsibilities and obligations
    contained in this agreement.

    Government Agency: National Aeronautics and Space Administration (NASA)
    Government Agency Original Software Designation: ARC-15166-1
    Government Agency Original Software Title: NASA WorldWind
    User Registration Requested. Please send email with your contact information to Patrick.Hogan@nasa.gov
    Government Agency Point of Contact for Original Software: Patrick.Hogan@nasa.gov

    You may obtain a full copy of the license at:

        https://worldwind.arc.nasa.gov/LICENSE.html
