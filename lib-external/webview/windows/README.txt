$Id: README.txt 682 2012-07-07 16:31:36Z pabercrombie $

WebView is a JNI library that allows Java code to use the operating system's web browser to render an HTML page into
an image. The library also provides support for translating Java AWT events into native Windows events to can be sent
to the native browser. This directory contains the Windows implementation of WebView, which uses Internet Explorer to
render web pages.

Requirements
------------

- Microsoft Visual C++ compiler and Windows Platform SDK
- Microsoft Active Template Library (ATL). Note that this library is not included with the free Visual Studio Express.
- Microsoft NMake
- Internet Explorer 7 or later

Building the library
--------------------

The library is built using Microsoft NMake. The build process can be launched from ant using the ant task
native.webview.libraries, defined in the World Wind build.xml file.

To build using ant:

> cd world-wind
> ant native.webview.libraries

To build using NMake directly:

> cd world-wind/lib-external/webview/windows
> nmake

Note that 32 and 64 bit versions of the library must be built separately. The easiest way to do this is (for building on
a 32 bit machine, switch 32 and 64 bit steps to build on x64):

1) Open Visual Studio Tools/Visual Studio Command Prompt
2) Run ant native.webview.libraries to build the 32 bit library.
3) Open Visual Studio Tools/Visual Studio x64 Cross Tools Command Prompt
4) Run ant native.webview.libraries again to build the 64 bit library.

See http://msdn.microsoft.com/en-us/library/x4d2c09s for more info.

Project overview
----------------

WebView.sln                - Visual Studio project file.
WindowsWebViewJNI.cpp      - JNI bindings for the Web View library.
WebViewWindow.cpp          - Main logic of the library. This file includes code to create the native web browser, and
                             render pages.
WebViewControl.cpp         - Life cycle management functions for managing Web View windows.
AWTEventSupport.cpp        - Support for mapping AWT events into Windows events.
HTMLMoniker.cpp            - COM moniker to load a web page from HTML content in memory.
WebViewProtocol.cpp        - A pluggable protocol for the "webview:" scheme, which is used to intercept URL load events
                             and resolve them using a WebResourceResolver.
WebViewProtocolFactory.cpp - Factory to create instances of the WebViewProtocol
stdafx.h                   - Precompiled header file.

Recommended reading
-------------------

A good book for learning the basics of COM programming is "Inside COM" by Dale Rogerson.
"Essential COM" by Don Box is helpful for understanding advanced topics.
