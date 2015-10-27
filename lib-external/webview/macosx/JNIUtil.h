#ifndef JAVA_UTIL_H
#define JAVA_UTIL_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
