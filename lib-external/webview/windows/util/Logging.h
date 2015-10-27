/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: Logging.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#ifndef JAVA_LOGGER_H
#define JAVA_LOGGER_H

#include "../stdafx.h"
#include <jni.h>

class Logging
{
public:
    static void initialize(JNIEnv *env);

    static Logging *logger();

    static jstring getMessage(JNIEnv *env, const wchar_t *msgKey, HRESULT errorCode);

    static jstring getMessage(JNIEnv *env, const wchar_t *msgKey, const wchar_t *arg);

    void warning(const wchar_t *msg);

    void warning(const wchar_t *msg, HRESULT errorCode);

    void warning(const wchar_t *msg, const wchar_t *arg);

    void severe(const wchar_t *msg);

    void severe(const wchar_t *msg, HRESULT errorCode);

    void severe(const wchar_t *msg, const wchar_t *arg);

protected:

    Logging(JNIEnv *env);

    virtual ~Logging() { }

protected:
    static Logging *logger_instance;

    JavaVM *javaVM;
};

#endif
