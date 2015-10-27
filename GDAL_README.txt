#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

# $Id: GDAL_README.txt 1171 2013-02-11 21:45:02Z dcollins $

This document provides guidance on deploying applications that use the World Wind GDAL libraries.


Version
------------------------------------------------------------

World Wind uses GDAL version 1.7.2, and LizardTech's Decode SDK version 7.0.0.2167.


Supported Platforms
------------------------------------------------------------

World Wind supports Windows 32/64-bit, Linux 32/64-bit, and Mac OSX. The World Wind SDK therefore contains
GDAL, PROJ4, and MRSiD binaries for the following platforms:
 - MacOSX universal
 - Windows 32-bit and Windows 64-bit
 - Linux 32-bit and Linux 64-bit


World Wind GDAL Libraries
------------------------------------------------------------

To simplify deployment, our GDAL+PRO4+MRSID bundles are compiled as a single dynamic library which has all dependent
libraries statically compiled. As a result we have one dynamic library "gdalall" per OS / per platform. Each platfor
library is located under the "lib-external/gdal/" folder.

GDAL and PROJ4 libraries require data tables located in the lib-external/gdal/data folder. We recommend placing these
tables in the "data" sub-folder.

WWJ attempts to locate GDAL bundles during the startup. By default WWJ will first look for GDAL native binaries in the
current path, then in to the lib-external/gdal/ folder. If no GDAL bundle was found World Wind attempts to locate the
GDAL bundle in the sub-folders. Therefore we recommend one of two options:
1) Place the GDAL libraries in the folder "lib-external/gdal".
2) Place the GDAL libraries in the application's root folder.


Deploying with Java Web Start
------------------------------------------------------------

Instructions for using the World Wind GDAL libraries with a Java Web Start application are available at
http://goworldwind.org/getting-started/
