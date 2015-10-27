/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

#ifndef WIN_UTIL_H
#define WIN_UTIL_H

HWND FindChildWindow(HWND parentWnd, wchar_t *windowClass);

HWND FindThreadWindow(DWORD threadId, wchar_t *windowClass);

#endif