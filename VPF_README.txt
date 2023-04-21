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

# $Id: VPF_README.txt 1171 2013-02-11 21:45:02Z dcollins $

This document describes (1) purpose of the JAR file "vpf-symbols.jar", (2) when it is necessary to use it, and (3) how
to build it.


1) PURPOSE OF VPF-SYMBOLS.JAR

The JAR file vpf-symbols.jar found in the WorldWind Java SDK contains style definitions and PNG icons for Vector
Product Format (VPF) shapes. vpf-symbols.jar is 1.8 MB in size, and is therefore distributed separately to avoid
increasing the size of worldwind.jar for the sake of supporting VPF, an uncommonly used feature.

For details on the VPF format specification and the corresponding GeoSym style specification, see the following
National Geospatial-Intelligence Agency (NGA) websites:
VPF: http://earth-info.nga.mil/publications/specs/
GeoSym: http://www.gwg.nga.mil/pfg_documents.php


2) USING VPF-SYMBOLS.JAR

The JAR file vpf-symbols.jar must be distributed and included in the runtime class-path by applications using the World
Wind class gov.nasa.worldwind.formats.vpf.VPFLayer. When added to an application's class-path, WorldWind VPF shapes
automatically find and locate style and icon resources contained within this JAR file.

If vpf-symbols.jar is not in the Java class-path, VPFLayer outputs the following message in the WorldWind log:
"WARNING: GeoSym style support is disabled". In this case, VPF shapes are displayed as gray outlines, and icons are
displayed as a gray question mark.


3) BUILDING VPF-SYMBOLS.JAR

To build or reconstruct the VPF symbols JAR file for the WorldWind Java SDK, follow these six steps:

- Download and extract the GeoSym Second Edition package.
Download the GeoSym archive from the National Geospatial-Intelligence Agency (NGA) at the following page, then extract
it to a folder named "GeoSymEd2Final":
http://www.gwg.nga.mil/pfg_documents.php

- Create a new directory structure to hold the contents of the VPF symbols JAR file as follows:
geosym/
geosym/graphics/bin
geosym/symasgn/ascii

- Convert all Geosym images from CGW format to PNG format.
Convert all images under the folder GeoSymEd2Final/GRAPHICS/BIN from CGM to PNG format. Pad the PNG files to the next
largest power-of-two, adding extra transparent pixels out from the image's center. Place the PNG files under the
following directory created earlier: geosym/graphics/bin

The PNG images in the current version of vpf-symbols.jar were created by (a) converting the original CGM images to TIFF
images using CorelDRAW Graphics Suite X4, (b) converting the TIFF images to PNG images using the open source application
tiff2png version 0.91, and (c) padding the images to the next largest power-of-two using a simple Java application built 
on ImageIO.

- Copy all GeoSym style tables.
Copy all files under the directory GeoSymEd2Final/SYMASGN/ASCII to the directory geosym/symasgn/ascii.

- Convert GeoSym line and area styles to WorldWind shape attributes.
Run the Java application gov.nasa.worldwind.formats.vpf.GeoSymAttributeConverter from the shell, passing the full path
to GeoSymEd2Final/GRAPHICS/TEXT as the application's only parameter. Copy the output PNG files under
gsac-out/geosym/graphics/bin to the following directory created earlier: geosym/graphics/bin. Copy the output CSV files 
under gsac-out/geosym/symasgn/ascii to the following directory created earlier: geosym/symasgn/ascii.

- Create the VPF symbols JAR file:
Execute the following shell command from the parent directory of the "geosym" directory structure created earlier,
making sure that a Java SDK is on the path:
jar -cf vpf-symbols.jar geosym
