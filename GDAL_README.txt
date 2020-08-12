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

# $Id: GDAL_README.txt 1171 2013-02-11 21:45:02Z dcollins $

This document provides guidance on deploying applications that use the WorldWind GDAL libraries.


Version
------------------------------------------------------------

WorldWind uses GDAL version 1.7.2, and LizardTech's Decode SDK version 7.0.0.2167.


Supported Platforms
------------------------------------------------------------

WorldWind supports Windows 32/64-bit, Linux 32/64-bit, and Mac OSX. The WorldWind SDK therefore contains
GDAL, PROJ4, and MRSiD binaries for the following platforms:
 - MacOSX universal
 - Windows 32-bit and Windows 64-bit
 - Linux 32-bit and Linux 64-bit


WorldWind GDAL Libraries
------------------------------------------------------------

To simplify deployment, our GDAL+PRO4+MRSID bundles are compiled as a single dynamic library which has all dependent
libraries statically compiled. As a result we have one dynamic library "gdalall" per OS / per platform. Each platfor
library is located under the "lib-external/gdal/" folder.

GDAL and PROJ4 libraries require data tables located in the lib-external/gdal/data folder. We recommend placing these
tables in the "data" sub-folder.

WWJ attempts to locate GDAL bundles during the startup. By default WWJ will first look for GDAL native binaries in the
current path, then in to the lib-external/gdal/ folder. If no GDAL bundle was found WorldWind attempts to locate the
GDAL bundle in the sub-folders. Therefore we recommend one of two options:
1) Place the GDAL libraries in the folder "lib-external/gdal".
2) Place the GDAL libraries in the application's root folder.

