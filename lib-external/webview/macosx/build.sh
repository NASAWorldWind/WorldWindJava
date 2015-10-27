#!/bin/sh

#
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
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
