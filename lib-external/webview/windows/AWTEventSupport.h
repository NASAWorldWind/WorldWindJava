/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Functions to translate Java InputEvents into Windows messages.
 *
 * Version: $Id: AWTEventSupport.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#ifndef AWT_EVENT_SUPPORT_H
#define AWT_EVENT_SUPPORT_H

#include <jni.h>

/**
 * Initialize the library. This function must be called before any other AWTEventSupport functions.
 *
 * @param env JNI environment to use to access Java objects.
 */
void AWTEvent_Initialize(JNIEnv *env);

/**
 * Translate a Java InputEvent into a native Windows event.
 *
 * @param env          JNI environment for accessing Java objects
 * @param event        java.awt.event.InputEvent to translate
 * @param targetWindow HWND of the window to send the event to
 */
void PostMsgFromAWTEvent(JNIEnv *env, jobject event, HWND targetWindow);

#endif