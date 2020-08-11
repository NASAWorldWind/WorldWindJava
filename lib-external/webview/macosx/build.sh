#!/bin/sh

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

# Build script for the MacWebView JNI bindings on Mac OS X.
#
# Version $Id: build.sh 1948 2014-04-19 20:02:38Z dcollins $

# Declare variables used during compiling and linking. Since Java for Mac OSX 10.6 Update 3, you must install the Java
# Developer Package to compile against Apple's JNI headers. The Java Developer package is available from
# http://connect.apple.com
# For details, see
# http://developer.apple.com/library/mac/#releasenotes/Java/JavaSnowLeopardUpdate3LeopardUpdate8RN
HEADER_PATHS=-I/System/Library/Frameworks/JavaVM.framework/Headers
FRAMEWORK_PATHS=-F/System/Library/Frameworks/JavaVM.framework/Versions/A/Frameworks
FRAMEWORKS="-framework Cocoa -framework WebKit -framework JavaNativeFoundation -framework OpenGL"
COMPILE_PARAMS="-ObjC -Os"
LINK_PARAMS="-shared -ObjC -lobjc -Os"

# Remove the native object files and the native shared library.
rm -f *.o
rm -f *.jnilib

# Compile the Objective-C sources into native object files.
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c JNIUtil.m -o JNIUtil.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c MacWebViewJNI.m -o MacWebViewJNI.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c OGLUtil.m -o OGLUtil.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c PropertyChangeListener.m -o PropertyChangeListener.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c ThreadSupport.m -o ThreadSupport.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c WebDownloadController.m -o WebDownloadController.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c WebDownloadView.m -o WebDownloadView.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c WebResourceResolver.m -o WebResourceResolver.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c WebViewWindow.m -o WebViewWindow.o
gcc $COMPILE_PARAMS $FRAMEWORK_PATHS $HEADER_PATHS -c WebViewWindowController.m -o WebViewWindowController.o

# Link the native object files into a native shared library. The pattern "lib[name].jnilib" is expected by the Java
# method System.loadLibrary(). For details, see
# http://developer.apple.com/library/mac/#documentation/Java/Conceptual/Java14Development/05-CoreJavaAPIs/CoreJavaAPIs.html
gcc -o libwebview.jnilib $LINK_PARAMS $FRAMEWORK_PATHS $FRAMEWORKS \
    JNIUtil.o \
    MacWebViewJNI.o \
    OGLUtil.o \
    PropertyChangeListener.o \
    ThreadSupport.o \
    WebDownloadController.o \
    WebDownloadView.o \
    WebResourceResolver.o \
    WebViewWindow.o \
    WebViewWindowController.o
