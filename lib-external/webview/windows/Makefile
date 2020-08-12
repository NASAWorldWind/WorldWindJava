############################################################################
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
# Microsoft NMAKE file for building WebView JNI bindings on Windows.
#
# Version $Id: Makefile 15506 2011-05-25 21:51:39Z pabercrombie $
#############################################################################

LIBNAME=WebView

SOURCES=WindowsWebViewJNI.cpp \
        WebViewControl.cpp \
        AWTEventSupport.cpp \
        WebViewWindow.cpp \
        HTMLMoniker.cpp \
        NotificationAdapter.cpp \
        WebResourceResolver.cpp \
        WebViewProtocol.cpp \
        WebViewProtocolFactory.cpp \
        dllmain.cpp \
        util\WinUtil.cpp \
        util\JNIUtil.cpp \
        util\Logging.cpp

LIBS=openGL32.lib gdi32.lib wininet.lib

!if "$(DEBUG)" == "1"
OUTPUTDIR=Debug
!else
OUTPUTDIR=Release
!endif

!if "$(PLATFORM)" == "x64"
TARGET=$(LIBNAME)64.dll
!else
TARGET=$(LIBNAME)32.dll
!endif

MANIFEST=$(TARGET).manifest

DEPLOY = ..\..\..

OBJECTS= $(SOURCES:.cpp=.obj)

INCLUDES=/I "%JAVA_HOME%\include" /I "%JAVA_HOME%\include\win32"

CPPFLAGS=/O2 $(INCLUDES) /D "WIN32" /D "_WINDOWS" /D "_USRDLL" /D "WEBVIEW_EXPORTS" /D "_WINDLL" /D "_ATL_STATIC_REGISTRY" \
	/D "_UNICODE" /D "UNICODE" /GS /EHsc /Fo"$(OUTPUTDIR)\\" /W3 /WX- /c /Zi /TP

LFLAGS=/DLL /MANIFEST /MANIFESTFILE:"$(OUTPUTDIR)\$(MANIFEST)" /MANIFESTUAC:"level='asInvoker' uiAccess='false'" \
	/SUBSYSTEM:WINDOWS /DYNAMICBASE /NXCOMPAT

!if "$(DEBUG)" == "1"
CPPFLAGS=$(CPPFLAGS) /D "_DEBUG" /MTd
LFLAGS=$(LFLAGS) /DEBUG /PDB:"$(OUTPUTDIR)\$(LIBNAME).pdb"
!else
CPPFLAGS=$(CPPFLAGS) /MT
!endif

all: $(TARGET)

dir:
	if not exist $(OUTPUTDIR) mkdir $(OUTPUTDIR)

# No need to define an explicit compilation rule. We use the predefined rule that compiles .cpp files using
# the options set in CPPFLAGS and the cl compiler. See http://msdn.microsoft.com/en-us/library/cx06ysxh%28v=vs.71%29.aspx
# for more info.

# Target to create the DLL. Link the object files, and invoke the manifest tool (mt.exe) to embed a manifest in the
# DLL. Windows requires a manifest to load the library. See http://msdn.microsoft.com/en-us/library/ms235542%28v=vs.80%29.aspx
# for more information on manifests.
$(TARGET): dir $(OBJECTS)
	link /nologo $(OUTPUTDIR)\*.obj /out:$(OUTPUTDIR)\$(TARGET) $(LFLAGS) $(LIBS)
	mt.exe /outputresource:"$(OUTPUTDIR)\$(TARGET);#2" /nologo /manifest "$(OUTPUTDIR)\$(MANIFEST)"
	copy $(OUTPUTDIR)\$(TARGET) $(DEPLOY)

clean:
	del /q $(OUTPUTDIR)\*
