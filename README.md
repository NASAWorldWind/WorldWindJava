<img src="https://worldwind.arc.nasa.gov/css/images/nasa-logo.svg" height="100"/>

# World Wind Java

[![Build Status](https://travis-ci.org/NASAWorldWind/WorldWindJava.svg?branch=develop)](https://travis-ci.org/NASAWorldWind/WorldWindJava)

3D virtual globe API for desktop Java, developed by NASA. Provides a geographic context, complete with terrain, for 
visualizing geographic or geo-located information in 3D and 2D. World Wind Java provides high-resolution terrain and 
imagery, retrieved from remote servers automatically as needed. Developers can provide custom terrain and imagery. 
Contains a rich set of features for displaying and interacting with geographic data and representing a wide range of 
geometric objects. More information at [worldwind.arc.nasa.gov](https://worldwind.arc.nasa.gov).       

## Get Started

Develop a world-class World Wind application for personal computers. Setup instructions, developers guides,
API documentation and more are available at [worldwind.arc.nasa.gov](https://worldwind.arc.nasa.gov). This GitHub
repository contains the library source, examples and tutorials.

- [worldwind.arc.nasa.gov](https://worldwind.arc.nasa.gov) has all things World Wind in one place
- [World Wind Forum](http://forum.worldwindcentral.com) provides help from the World Wind community
- [GitHub Issues](https://github.com/NASAWorldWind/WorldWindJava/issues) provides requirements and issue tracking
- [Travis CI](https://travis-ci.org/NASAWorldWind/WorldWindJava) provides continuous integration and build automation
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) is used by the NASA World Wind development team

## Releases and Roadmap

Official World Wind Java releases from the [master](https://github.com/NASAWorldWind/WorldWindJava/tree/master) branch. 
Releases have the latest stable features, enhancements and bug fixes ready for production use.

- [Latest Release](https://github.com/NASAWorldWind/WorldWindJava/releases/latest) has the current release changelogs, binary assets, and associated GitHub tag
- [Releases](https://github.com/NASAWorldWind/WorldWindJava/releases/) lists all official releases
- [Milestones](https://github.com/NASAWorldWind/WorldWindJava/milestones) documents upcoming releases and the development roadmap
- [API Documentation](https://worldwind.arc.nasa.gov/assets/java/latest/javadoc) for the latest release

## Daily Builds

World Wind Java builds from the [develop](https://github.com/NASAWorldWind/WorldWindJava/tree/develop) branch. Daily 
builds have the newest, bleeding-edge World Wind Java features. Intended for developers and early adopters.

- [Releases](https://github.com/NASAWorldWind/WorldWindJava/releases) hosts the daily builds
- [API Documentation](https://worldwind.arc.nasa.gov/assets/java/daily/javadoc) for the current daily build

## Run a Demo 
   
###### From a Web Browser
   
- [World Wind Demo App](https://worldwind.arc.nasa.gov/java/latest/webstart/ApplicationTemplate.jnlp) shows World Wind's basic capabilities
- [Java Demos](http://goworldwind.org/demos) has a complete list of example apps
   
###### From a Windows Development Environment

- Download and extract the [Latest Release](https://github.com/NASAWorldWind/WorldWindJava/releases/latest)
- Open the Command Prompt
```bash
cd [World Wind release]
run-demo.bat
```

###### From a Linux or macOS Development Environment

- Download and extract the [Latest Release](https://github.com/NASAWorldWind/WorldWindJava/releases/latest)
- Open the Terminal app
```bash
cd [World Wind release]
sh run-demo.bash
```

###### Troubleshooting
   
World Wind requires a modern graphics card with a current driver. Most display problems are caused by out-of-date 
graphics drivers. On Windows, visit your graphics card manufacturer's web site for the latest driver: NVIDIA, ATI or 
Intel. The drivers are typically under a link named Downloads or Support. If you're using a laptop, the latest drivers 
are found at the laptop manufacturer's web site.

## JOGL Native Binaries

JOGL performs runtime extraction of native binaries. Some deployment situations may not allow this because it extracts 
the binaries to the application user’s temp directory. Runtime extraction can be avoided by by modifying World Wind 
Java's JOGL distribution to load native binaries directly from the library path instead of dynamically using the native 
binary JAR files as follows:
                                                                                                     
1. Extract the GlueGen and JOGL native binary JAR files for the desired platform.
   These JAR files follow the naming pattern gluegen-rt-natives-PLATFORM.jar and jogl-all-natives-PLATFORM.jar
2. Place the extracted native binaries either in the program's working directory or in a location specified as the
   library path. The following JOGL user's guide page outlines supported library path variables:
   https://jogamp.org/jogl/doc/userguide/index.html#traditionallibraryloading
3. Remove the GlueGen and JOGL native binary JAR files from your application's workspace.
   JOGL attempts to use the native binary JAR files before loading from the library path, so these files must not be
   deployed with the application.
4. When running, specify the JVM argument -Djogamp.gluegen.UseTempJarCache=false

## License

    NASA WORLD WIND

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
    Government Agency Original Software Title: NASA World Wind
    User Registration Requested. Please send email with your contact information to Patrick.Hogan@nasa.gov
    Government Agency Point of Contact for Original Software: Patrick.Hogan@nasa.gov

    You may obtain a full copy of the license at:

        https://worldwind.arc.nasa.gov/LICENSE.html
