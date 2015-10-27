############################################################################
# Copyright (C) 2012 United States Government as represented by the Administrator of the
# National Aeronautics and Space Administration.
# All Rights Reserved.
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
