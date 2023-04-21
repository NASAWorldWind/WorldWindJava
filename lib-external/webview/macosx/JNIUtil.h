#ifndef JAVA_UTIL_H
#define JAVA_UTIL_H

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
#import <Cocoa/Cocoa.h>
#import <Carbon/Carbon.h>
#import <JavaVM/jni.h> // Java JNI header.

/*
 * Version $Id: JNIUtil.h 1171 2013-02-11 21:45:02Z dcollins $
 */

extern NSString *NSStringFromJavaObject(JNIEnv *env, jobject object);

extern NSRect NSRectFromJavaRectangle(JNIEnv *env, jobject rect);

extern jobject JavaRectangleFromNSRect(JNIEnv *env, NSRect rect);

extern NSSize NSSizeFromJavaDimension(JNIEnv *env, jobject size);

extern jobject JavaDimensionFromNSSize(JNIEnv *env, NSSize size);

extern NSURL *NSURLFromJavaURL(JNIEnv *env, jobject url);

extern jobject JavaURLFromNSURL(JNIEnv *env, NSURL *url);

extern NSEvent *NSEventFromJavaEvent(JNIEnv* env, jobject event, NSWindow *sourceWindow);

extern NSEvent *NSEventFromJavaKeyEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow);

extern NSEvent *NSEventFromJavaMouseEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow);

extern NSEvent *NSEventFromJavaMouseWheelEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow);

extern CGEventType CGEventTypeFromJavaEvent(JNIEnv *env, jobject event);

extern CGKeyCode CGKeyCodeFromJavaEvent(JNIEnv *env, jobject event);

extern CGMouseButton CGMouseButtonFromJavaEvent(JNIEnv *env, jobject event);

extern CGPoint CGMousePointFromJavaEvent(JNIEnv *env, jobject event);

extern void CGScrollAmountFromJavaEvent(JNIEnv *env, jobject event, NSWindow *sourceWindow, int32_t *values);

extern void InitLoggerJNI(JNIEnv *env);

extern void LogSevere(JNIEnv *env, NSString *message);

extern void LogSevereObtainEnv(NSString *message);

extern NSString *GetLogMessage(JNIEnv *env, NSString *property);

extern NSString *GetLogMessageObtainEnv(NSString *property);

extern NSString *GetLogMessageWithException(JNIEnv *env, NSString *property, NSException *exception);

extern NSString *GetLogMessageWithExceptionObtainEnv(NSString *property, NSException *exception);

extern NSString *GetLogMessageWithArgException(JNIEnv *env, NSString *property, NSString *arg, NSException *exception);

extern NSString *GetLogMessageWithArgExceptionObtainEnv(NSString *property, NSString *arg, NSException *exception);

extern NSString *GetDescriptionForException(NSException *exception);

#endif /* JAVA_UTIL_H */
