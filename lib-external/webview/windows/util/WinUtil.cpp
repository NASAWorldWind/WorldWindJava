/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

#include "../stdafx.h"
#include "WinUtil.h"

struct EnumWindowsArg {
    wchar_t *windowClass;
    HWND foundWindow;
};

/**
 * Callback function invoked by EnumWindows.
 */
BOOL CALLBACK findChildWindowProc(HWND hWnd, LPARAM lparam)
{
    const int BUFFER_SIZE = 256;
    wchar_t windowClass[BUFFER_SIZE];

    struct EnumWindowsArg *arg = (struct EnumWindowsArg*)lparam;
    assert(arg != NULL && "Invalid argument to findChildWindowProc");

    wchar_t *targetWindow = (wchar_t*)arg->windowClass;
    assert(targetWindow != NULL && "Target window class is NULL");
    
    int ret = GetClassName(hWnd, windowClass, BUFFER_SIZE);
    assert(ret != 0 && "Failed to get class name of window");

    // Search for the target string at the start of the window title
    //wchar_t *match = wcsstr(arg->windowClass, targetWindow);
    if (wcscmp(arg->windowClass, windowClass) == 0)
    {
        arg->foundWindow = hWnd;
        return FALSE;
    }

    return TRUE;
}

HWND FindChildWindow(HWND parentWnd, wchar_t *windowClass)
{ 
    struct EnumWindowsArg arg;
    arg.windowClass = windowClass;
    arg.foundWindow = NULL;

    EnumChildWindows(parentWnd, findChildWindowProc, reinterpret_cast<LPARAM>(&arg)); 
    return arg.foundWindow;
}

HWND FindThreadWindow(DWORD threadId, wchar_t *windowClass)
{ 
    struct EnumWindowsArg arg;
    arg.windowClass = windowClass;
    arg.foundWindow = NULL;

    EnumThreadWindows(threadId, findChildWindowProc, reinterpret_cast<LPARAM>(&arg));
    return arg.foundWindow;
}
