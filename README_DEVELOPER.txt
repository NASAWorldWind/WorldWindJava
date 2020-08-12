#
# Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
# Administrator of the National Aeronautics and Space Administration.
# All rights reserved.
# 
# The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
# 
# NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
# software:
# 
#     Jackson Parser – Licensed under Apache 2.0
#     GDAL – Licensed under MIT
#     JOGL – Licensed under  Berkeley Software Distribution (BSD)
#     Gluegen – Licensed under Berkeley Software Distribution (BSD)
# 
# A complete listing of 3rd Party software notices and licenses included in
# NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
# notices and licenses PDF found in code directory.
#

# $Id: README_DEVELOPER.txt 1934 2014-04-15 19:24:07Z dcollins $

This document provides notes and instructions for WorldWind Java development team members in one place.


JOGL Library Overview
------------------------------------------------------------

The JOGL library provides WorldWind Java with (1) a Java binding to the OpenGL API, and (2) OpenGL contexts compatible
with Java's AWT and Swing windowing toolkits:
http://jogamp.org/jogl/www/

WorldWind Java uses JOGL v2.4, released on March 7th, 2020 and downloaded from:
https://jogamp.org/deployment/v2.4.0-rc-20200307/archive/jogamp-all-platforms.7z

The JOGL library compiled JAR files and README files are checked into the WorldWind Java source, distributed with all
WorldWind Java builds and included in the WorldWind Java Web Start deployment. This is necessary in order ensure
correct operation of WorldWind Java, as changes in JOGL are occasionally unstable or incompatible with previous
versions. Additionally, WorldWind Java's copy of the JOGL JAR files are modified to enable Web Start deployment outside
of the jogamp.org domain.


Updating the JOGL Library
------------------------------------------------------------

1) Download the JOGL deployment package archive/jogamp-all-platforms.7z for the new JOGL version. JOGL deployment
packages are organized by version at the following URL:
http://jogamp.org/deployment/

2) Extract the archive, then copy the following 15 files to the WorldWind Java project root:
gluegen-rt-natives-linux-amd64.jar
gluegen-rt-natives-linux-i586.jar
gluegen-rt-natives-macosx-universal.jar
gluegen-rt-natives-windows-amd64.jar
gluegen-rt-natives-windows-i586.jar
gluegen-rt.jar
gluegen.LICENSE.txt
jogl-all-natives-linux-amd64.jar
jogl-all-natives-linux-i586.jar
jogl-all-natives-macosx-universal.jar
jogl-all-natives-windows-amd64.jar
jogl-all-natives-windows-i586.jar
jogl-all.jar
jogl.LICENSE.txt
jogl.README.txt

3) Update the JOGL version in the following files:
README.txt
README_DEVELOPER.txt (this document)
release-build.xml (JOGL link in javadocs target)
