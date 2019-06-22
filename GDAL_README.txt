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
    - Starting with GDAL 3.0.0, the PROJ.6 projection library is used.  It 
      requires the PROJ_LIB environment variable to be defined with location of 
      the "proj.db" file.  A commit was made to GDAL on Jun 18, 2019 that will 
      allow the location to be set programmatically. 
      See https://github.com/OSGeo/gdal/pull/1658/

    - Unless the GDAL_DATA environment variable is set, the GDAL
      data directory will be searched for, using the property
      "user.dir", and then in some standard locations (see GDALUtils.java)

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
    'java.library.path' or 'LD_LIBRARY_PATH'.  GDAL versions later
    than 2.3.2 have all the JNI interfaces in a single shared library
    (gdalalljni.lib/libgdalalljni.so).

    The GISInternals binary package for Windows uses this
    directory structure:

    C:\Program Files\GDAL                   utilities, shared libraries, 
                                            including JNI shared library
    C:\Program Files\GDAL\java\gdal.jar     GDAL Java interface
    C:\Program Files\GDAL\gdal-data         GDAL data directory
    C:\Program Files\GDAL\gdalplugins       GDAL plugin directory

    Note that GISInternals has multiple versions of the ERDAS ECW
    software.  Choose one according to your needs.

    If you want to include the native libraries as part of your
    deployment, copy
    
        C:\Program Files\GDAL*.dll
        C:\Program Files\GDAL\gdal-data
        C:\Program Files\GDAL\gdalplugins
        C:\Program Files\GDAL\java\gdal.jar
                
    to the directory from which your application is launched.  Include
    the 'gdal.jar' file in your application's classpath.

    Alternatively, one can leave all the native libraries from
    GISInternals in their installation location, and

        set PATH to include C:\Program Files\GDAL
        add -Djava.library.path="C:\Program Files\GDAL" JVM option
        add -classpath="C:\Program Files\GDAL\java\gdal.jar" JVM option


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
                
    When using the Gradle build script, note that the version number
    defined by 'gdalVersion' must match the binary libraries that are
    installed.
