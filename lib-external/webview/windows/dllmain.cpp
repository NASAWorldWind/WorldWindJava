/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

// dllmain.cpp : Defines the entry point for the DLL application.
#include "stdafx.h"
#include "dllmain.h"
#include "WebViewControl.h"
#include "util/Logging.h"

CWebViewModule _AtlModule;

extern LRESULT CALLBACK MessageWndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);

void InitializeDLL(HINSTANCE hInstance)
{
    WNDCLASSEX wc;
    ZeroMemory(&wc, sizeof(WNDCLASSEX));

    wc.cbSize = sizeof(WNDCLASSEX);
    wc.hInstance = hInstance;
    wc.lpszClassName = MESSAGE_ONLY_WINDOW_CLASS;
    wc.lpfnWndProc = reinterpret_cast<WNDPROC>(MessageWndProc);

    RegisterClassEx(&wc);
}

// DLL Entry Point
extern "C" BOOL WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	InitializeDLL(hInstance);
	return _AtlModule.DllMain(dwReason, lpReserved); 
}
