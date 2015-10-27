#
# Copyright (C) 2013 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

# $Id: README_DEVELOPER.txt 1934 2014-04-15 19:24:07Z dcollins $

This document provides notes and instructions for World Wind Java development team members in one place.


JOGL Library Overview
------------------------------------------------------------

The JOGL library provides World Wind Java with (1) a Java binding to the OpenGL API, and (2) OpenGL contexts compatible
with Java's AWT and Swing windowing toolkits:
http://jogamp.org/jogl/www/

World Wind Java uses JOGL v2.1.5, released on 11 March 2014 and downloaded from:
http://jogamp.org/deployment/v2.1.5/archive/jogamp-all-platforms.7z

The JOGL library compiled JAR files and README files are checked into the World Wind Java source, distributed with all
World Wind Java builds and included in the World Wind Java Web Start deployment. This is necessary in order ensure
correct operation of World Wind Java, as changes in JOGL are occasionally unstable or incompatible with previous
versions. Additionally, World Wind Java's copy of the JOGL JAR files are modified to enable Web Start deployment outside
of the jogamp.org domain.


Updating the JOGL Library
------------------------------------------------------------

1) Download the JOGL deployment package archive/jogamp-all-platforms.7z for the new JOGL version. JOGL deployment
packages are organized by version at the following URL:
http://jogamp.org/deployment/

2) Extract the archive, then copy the following 15 files to the World Wind Java project root:
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

3) Remove the Codebase manifest attribute from all JOGL JAR files. This step enables the JOGL JAR files to be
deployed via the World Wind Java Web Start site:
- Run the ANT task jogl.jarfiles.unpack. This task extracts the contents of all 12 GlueGen and JOGL JAR files into
individual folders under jogl-jarfiles.
- For each JAR file folder under jogl-jarfiles open the file META-INF/MANIFEST.MF, delete the line
'Codebase: *.jogamp.org', then save the file.
- Run the ANT task jogl.jarfiles.pack. This task builds new copies of all 12 GlueGen and JOGL JAR files under
jogl-jarfiles, using the same JAR settings as the JOGL build script.
- Copy the new 12 GlueGen and JOGL JAR files from jogl-jarfiles to the project root, overwriting the existing GlueGen
and JOGL JAR files.
- Delete the folder jogl-jarfiles.

4) Update the JOGL version in the following files:
webstart/gluegen-rt.jnlp
webstart/jogl-all.jnlp
README.txt
README_DEVELOPER.txt (this document)
build.xml (JOGL link in javadocs target)
