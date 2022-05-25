<img src="https://worldwind.arc.nasa.gov/img/nasa-logo.svg" height="100"/>

# WorldWind Java

## New version of WorldWind Java released
WorldWind Java 2.2.1 is now available on GitHub. This version of WorldWind Java is a housekeeping release that addresses small fixes (typos, bad references, etc.) to various areas of the code and removes references to services that are no longer supported by the WorldWind servers.

WorldWind's API remains unchanged in this release and we are committed to maintaining a consistent API in future releases.
More information on the release can be found at this link: [WorldWind Java 2.2.1](https://github.com/NASAWorldWind/WorldWindJava/releases).

Please direct questions to our new email address: arc-worldwind@mail.nasa.gov.

[![Build Status](https://travis-ci.com/NASAWorldWind/WorldWindJava.svg?branch=develop)](https://travis-ci.com/NASAWorldWind/WorldWindJava)

3D virtual globe API for desktop Java, developed by NASA. Provides a geographic context with high-resolution terrain, for visualizing geographic or geo-located information in 3D and 2D. Developers can customize the globe's terrain and imagery. Provides a collection of shapes for displaying and interacting with geographic data and representing a range of geometric objects.

- [worldwind.arc.nasa.gov](https://worldwind.arc.nasa.gov) has setup instructions, developers guides, API documentation and more
- [Apache NetBeans](https://netbeans.apache.org) is used by the NASA WorldWind development team

## Releases and Roadmap

Official WorldWind Java releases have the latest stable features, enhancements and bug fixes ready for production use.

- [GitHub Releases](https://github.com/NASAWorldWind/WorldWindJava/releases/) documents official releases
- [GitHub Milestones](https://github.com/NASAWorldWind/WorldWindJava/milestones) documents upcoming releases and the development roadmap
- [Travis CI](https://travis-ci.com/NASAWorldWind/WorldWindJava) provides continuous integration and build automation

## Run a Demo

The following options are available to run a WorldWind Java demo:

###### From the Apache NetBeans IDE

Clone the SDK with git, open the WorldWind Java project with Apache Netbeans and run demos via the Netbeans interface. 

###### From a Windows Development Environment

- Download and extract the [Latest Release](https://github.com/NASAWorldWind/WorldWindJava/releases/latest)
- Open the Command Prompt
```bash
cd [WorldWind release]
run-demo.bat
```

###### From a Linux or macOS Development Environment

- Download and extract the [Latest Release](https://github.com/NASAWorldWind/WorldWindJava/releases/latest)
- Open the Terminal app
```bash
cd [WorldWind release]
sh run-demo.bash
```

###### Troubleshooting

WorldWind requires a modern graphics card with a current driver. Most display problems are caused by out-of-date
graphics drivers. On Windows, visit your graphics card manufacturer's web site for the latest driver: NVIDIA, ATI or
Intel. The drivers are typically under a link named Downloads or Support. If you're using a laptop, the latest drivers
are found at the laptop manufacturer's web site.

## JOGL Native Binaries

JOGL performs runtime extraction of native binaries. Some deployment situations may not allow this because it extracts
the binaries to the application user’s temp directory. Runtime extraction can be avoided by by modifying WorldWind
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

Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
Administrator of the National Aeronautics and Space Administration.
All rights reserved.

The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
Version 2.0 (the "License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
software:

    Jackson Parser – Licensed under Apache 2.0
    GDAL – Licensed under MIT
    JOGL – Licensed under  Berkeley Software Distribution (BSD)
    Gluegen – Licensed under Berkeley Software Distribution (BSD)

A complete listing of 3rd Party software notices and licenses included in
NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
notices and licenses PDF found in code directory.
