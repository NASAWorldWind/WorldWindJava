/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
