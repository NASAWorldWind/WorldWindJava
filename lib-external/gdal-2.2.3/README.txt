

        GDAL JNI shared libraries based on
             - gdal-2.2.3       http://www.gdal.org/
             - CFITSIO 3.420    https://heasarc.gsfc.nasa.gov/fitsio/
             - openjpeg 2.3.0   http://www.openjpeg.org/
             - PROJ.4 4.9.3     http://proj4.org/
             - MrSID 9.5.4.4703 https://www.lizardtech.com/developer/home

        Only 64 bit code was built for Linux and Windows.


        Build instructions for GDAL JNI libraries

        **** Windows ****
 
        Prerequisites:
            - Microsoft Visual Studio 2013
            - swig 1.3.40 (versions 2 and 3 do not work
            - cmake
            - msys2
            - Java JDK
            - Apache ant
            - 7zip

        #- Unpack archives and apply patches (msys2 shell):

        unzip -q archive/cfit3420.zip -d cfitsio
        cd cfitsio
        patch -b --binary < ../patch.cfitsio
        cd ..
        tar xzf archive/proj-4.9.3.tar.gz
        cd proj-4.9.3
        patch -b --binary < ../patch.proj
        cd ..
        tar xzf archive/openjpeg-2.3.0.tar.gz
        unzip -q archive/MrSID/MrSID_DSDK-9.5.4.4703-win64-vc12.zip
        tar xJf archive/gdal-2.2.3.tar.xz
        cd gdal-2.2.3
        patch -b --binary -p0 < ../patch.gdal
        cd ..

        #- In a MSVS command window
        
        Execute: 
        C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC\vcvarsall.bat" amd64
        set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_152
        set JAVADOC=%JAVA_HOME%\bin\javadoc
        set JAVAC=%JAVA_HOME%\bin\javac
        set JAVA=%JAVA_HOME%\bin\java
        set JAR=%JAVA_HOME%\bin\jar
        set JAVA_INCLUDE="-I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32"
        set ANT_HOME=C:\apache-ant-1.10.1
        set SWIG="C:\Program Files\swigwin-1.3.40\swig.exe"
        set PATH=%PATH%;C:\Program Files\CMake\bin;C:\Program Files\swigwin-1.3.40;C:\Program Files\Java\jdk1.8.0_152\bin
        
        #-- cfitsio build

        cd cfigsio
        mkdir build
        cd build
        cmake -G "Visual Studio 12 Win64" -DBUILD_SHARED_LIBS=OFF ..
        cmake --build . --config Release

        #-- openjpeg build

        cd openjpeg-2.3.0
        mkdir build
        cd build
        cmake -G "Visual Studio 12 Win64" -DBUILD_SHARED_LIBS:bool=off -DCMAKE_INSTALL_PREFIX=..\openjpeg -DBUILD_THIRDPARTY=YES -DCMAKE_BUILD_TYPE:string="Release" ..
        cmake --build . --config Release
        cmake --build . --target install --config Release

        #-- proj.4 build

        cd proj-4.9.3
        nmake -f makefile.vc

        #-- gdal build (see
          https://trac.osgeo.org/gdal/wiki/GdalOgrInJavaBuildInstructions)
          Note that one must use swig 1.3.40
               
          nmake /f makefile.vc

          cd swig
          nmake /f makefile.vc java

        #-- javadoc: switch to msys2 shell
          
          export PATH=$PATH:/c/Program\ Files/Java/jdk1.8.0_152/bin
          cd gdal-2.2.3/swig/java
          ./make_doc.sh

          # for some reason, Eclipse won't validate a zip javadoc file
          # made by the 'zip' program.

          cd java
          /c/Program\ Files/7-Zip/7z a -r ../gdal-javadoc.zip *


        **** Linux ****

        export LD_LIBRARY_PATH=$PWD/MrSID_DSDK-9.5.4.4703-rhel6.x86-64.gcc531/Raster_DSDK/lib:$PWD/MrSID_DSDK-9.5.4.4703-rhel6.x86-64.gcc531/Lidar_DSDK/lib

        -- cfitsio build

`       unzip -q archive/cfit3420.zip -d cfitsio
        cd cfitsio
        patch -b --binary < ../patch.cfitsio
        mkdir build
        cd build

        cmake -G "Unix Makefiles" -DBUILD_SHARED_LIBS=OFF -DCMAKE_INSTALL_PREFIX=../cfitsio -DCMAKE_C_FLAGS="-fPIC -O2" ..
        # use -DCMAKE_VERBOSE_MAKEFILE:BOOL=ON to see compile line
        cmake --build . --config Release
        cmake --build . --target install --config Release

        #-- proj.4 build
        
        cd ../..
        tar xzf archive/proj-4.9.3.tar.gz
        cd proj-4.9.3
        ./configure --disable-share
        make

        #-- openjpeg build
        
        tar xzf archive/openjpeg-2.3.0.tar.gz
        cd openjpeg-2.3.0
        mkdir build
        cd build
        cmake -G "Unix Makefiles" -DBUILD_SHARED_LIBS:bool=off -DCMAKE_INSTALL_PREFIX=../openjpeg -DBUILD_THIRDPARTY=YES -DCMAKE_BUILD_TYPE:string="Release" ..
        cmake --build . --config Release
        cmake --build . --target install --config Release

        #-- gdal build

        tar xf archive/MrSID_DSDK-9.5.4.4703-rhel6.x86-64.gcc531.tar.gz

        tar xJf archive/gdal-2.2.3.tar.xz
        cd gdal-2.2.3

        ./configure --without-libtool --with-cfitsio=$PWD/../cfitsio/cfitsio --with-png=internal --with-libz=internal --with-pcraster=internal --with-pcidsk=internal --with-libtiff=internal --with-geotiff=internal --with-jpeg=internal --with-gif=internal --with-qhull=internal --with-libjson-c=internal --with-mrsid=$PWD/../MrSID_DSDK-9.5.4.4703-rhel6.x86-64.gcc531/Raster_DSDK  --with-jp2mrsid=yes --with-mrsid_lidar=$PWD/../MrSID_DSDK-9.5.4.4703-rhel6.x86-64.gcc531/Lidar_DSDK --with-openjpeg=$PWD/../openjpeg-2.3.0/openjpeg --with-static-proj4=$PWD/../proj-4.9.3/src/.libs/libproj.a --with-java=/usr/lib/jvm/java-8-oracle
=
        make
        cd swig/java
        make
