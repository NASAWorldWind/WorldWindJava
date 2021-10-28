<img src="https://worldwind.arc.nasa.gov/img/nasa-logo.svg" height="100"/>

# WorldWind Java - Community Edition (WWJ-CE)

[![Build Status](https://travis-ci.com/WorldWindEarth/WorldWindJava.svg?branch=develop)](https://travis-ci.com/WorldWindEarth/WorldWindJava)

WWJ-CE is community supported fork of the ubiquitous [WorldWind Java SDK](https://github.com/NASAWorldWind/WorldWindJava)
from NASA. 

WorldWind Java is a 3D virtual globe API for desktop Java, developed by NASA. It provides a geographic 
context with high-resolution terrain, for visualizing geographic or geo-located information in 3D and 2D. 
Developers can customize the globe's terrain and imagery. The SDK provides a collection of shapes for 
displaying and interacting with geographic data and representing a range of geometric objects.

- [WorldWindJava Wiki](https://github.com/WorldWindEarth/WorldWindJava/wiki) has setup instructions, developers guides and more
- [API Documentation](https://worldwind.earth/WorldWindJava/) provides an extensive collection of useful JavaDocs for the WWJ-CE SDK
- [WorldWind Forum](https://forum.worldwindcentral.com/forum/world-wind-java-forums) provides help from the WorldWind community
- [IntelliJ IDEA](https://www.jetbrains.com/idea) is used by the NASA WorldWind development team

## Notice from NASA
On March 8, 2019, NASA announced a suspension of the WorldWind project.  

> WorldWind team would like to inform you that starting May 3, 2019, NASA WorldWind project will be
> suspended. All the WorldWind servers providing elevation and imagery will be unavailable. While you
> can still download the SDKs from GitHub, there will be no technical support. If you have questions
> and/or concerns, please feel free to email at:
> 
> worldwind-info@lists.nasa.gov

WWJ-CE seeks to mitigate the effects of the suspension and continue the development of WorldWind Java.

## Contributing to the Community Edition
Contributions to WWJ-CE are welcomed in the form of issues and pull requests.

### Before Submitting an Issue
Please fulfill the following requirements before submitting an issue to this repository:

- Check the [Common Problems](https://github.com/WorldWindEarth/WorldWindJava/wiki/Common-Problems) in the [Tutorials](https://github.com/WorldWindEarth/WorldWindJava/wiki/Tutorials) wiki
- Check that your issue isn't already filed in the [open issues and pull requests](https://github.com/WorldWindEarth/WorldWindJava/issues?q=is%3Aopen)
- Check the [WorldWind Java Forums](https://forum.worldwindcentral.com/forum/world-wind-java-forums) for common solutions

### Before Submitting a Pull Request
Please read [Contributing to WorldWind Java](https://github.com/WorldWindEarth/WorldWindJava/blob/develop/CONTRIBUTING.md) and adhere to the WorldWind Java [Design and Coding Guidelines](https://github.com/WorldWindEarth/WorldWindJava/blob/develop/CONTRIBUTING.md#design-and-coding-guidelines).

## Releases and Roadmap

Official WorldWind Java releases have the latest stable features, enhancements and bug fixes ready for production use.

- [GitHub Releases](https://github.com/WorldWindEarth/WorldWindJava/releases/) documents official releases
- [GitHub Milestones](https://github.com/WorldWindEarth/WorldWindJava/milestones) documents upcoming releases and the development roadmap
- [Travis CI](https://travis-ci.com/WorldWindEarth/WorldWindJava) provides continuous integration and build automation

## Run a Demo

With the deprecation of WebStart/JNLP, the only option for running a Java demo is to clone the SDK, open it with Apache Netbeans and run it via the Netbeans interface. 

###### From a Windows Development Environment

- Download and extract the [Latest Release](https://github.com/WorldWindEarth/WorldWindJava/releases/latest)
- Open the Command Prompt
```bash
cd [WorldWind release]
run-demo.bat
```

###### From a Linux or macOS Development Environment

- Download and extract the [Latest Release](https://github.com/WorldWindEarth/WorldWindJava/releases/latest)
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

## GDAL

GDAL is a translator library for raster and vector geospatial data formats provided by the Open Source Geospatial Foundation.  GDAL libraries and native binaries are not provided by WorldWindEarth/WorldWind Java, but are needed to build.  The Gradle build will pull the necessary libraries from Maven.  Please see the file GDAL_README.txt for details on where to obtain the native binaries and other details.

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
