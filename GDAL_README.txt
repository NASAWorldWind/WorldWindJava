#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
#

# $Id: GDAL_README.txt 1171 2013-02-11 21:45:02Z dcollins $

This document provides guidance on deploying applications that use the
WorldWind GDAL libraries.

Building
 ------------------------------------------------------------
If building with 'ant', using the 'build.xml' file, change the
'gdal.win.properties' or 'gdal.unix.properties' files to
reflect the location of the GDAL library files on your system.

Deploying applications
------------------------------------------------------------
    Worldwind users should install a binary edition of GDAL,
    including the Java interface (gdal.jar, gdalalljni.lib/libgdalalljni.so).

    - The classpath used to build/execute Worldwind must include
      the location of the gdal.jar file.
    - On Windows, the 'java.libary.path' property must be set to
      the location of the JNI shared library.  In addition, if
      the DLLs are not in the same directory as the launched
      application, the PATH environment variable should be set to
      include the location of the shared libraries.  Note that if
      'java.library.path' is not explicitly set, the JVM's default
      includes PATH plus the current directory.
    - On Linux, the LD_LIBRARY_PATH environment variable should be
      set to include the location of the JNI shared library.  The
      JVM will include the paths in LD_LIBRARY_PATH in the
      'java.library.path' property.

    - Unless the GDAL_DATA environment variable is set, the GDAL
      data directory will be searched for, using the property
      "user.dir", and then in some standard locations.

    - Unless the GDAL_DRIVER_PATH environment variable is set, the
      GDAL plugins direoctory will be searched for, using the property
      "user.dir", and then in some standard locations.


    Binary distributions are available for both Windows and
    Linux.  See
    https://trac.osgeo.org/gdal/wiki/DownloadingGdalBinaries.

    For Ubuntu, the package "libgdal-java" contains the 'gdal.jar'
    and JNI shared library.

    GDAL versions earlier that 2.3.2 split the JNI library into
    five separate files.  They all need to be in paths listed in
    'java.library.path' or 'LD_LIBRARY_PATH'.

    The GISInternals binary package for Windows uses this
    directory structure:

    C:\Program Files\GDAL                   shared libraries, including JNI shared library
    C:\Program Files\GDAL\java\gdal.jar     GDAL Java interface
    C:\Program Files\GDAL\gdal-data         GDAL data directory
    C:\Program Files\GDAL\gdalplugins       GDAL plugin directory


    The Ubuntu Linux distribution uses these locations:

    /usr/lib:                    shared libraries
    /usr/share/java/gdal.jar     GDAL Java interface
    /usr/lib/jni                 GDAL JNI shared library
    /usr/share/gdal/2.2          GDAL data directory
    /usr/lib/gdalplugins         GDAL plugin directory

    There's a bug in the Ubuntu Bionic 18.04.2 LTS that prevents
    the Grass plugin from loading properly (see
    https://trac.osgeo.org/osgeolive/ticket/2068).  The workaround
    is to 

       setenv LD_LIBRARY_PATH=/usr/lib/jni:/usr/lib/grass74/lib

    Pre-built binaries for the MrSID and ERDAS ECW formats are not
    available on Ubuntu.  Instructions for building the plugins is
    available here:

              https://trac.osgeo.org/gdal/wiki/ECW
              https://trac.osgeo.org/gdal/wiki/MrSID
    

    MrSID SDK
    https://www.extensis.com/support/developers

    ERDAS ECW SDK
    https://www.hexagongeospatial.com/products/power-portfolio/compression-products/erdas-ecw-jp2-sdk
                

Deploying with Java Web Start
------------------------------------------------------------

Instructions for using the WorldWind GDAL libraries with a Java Web Start application are available at
https://goworldwind.org/getting-started/
