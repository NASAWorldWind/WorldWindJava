/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: Logging.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "../stdafx.h"
#include "Logging.h"

static jclass Logging_cls;
static jclass Logger;
static jmethodID Logging_logger;
static jmethodID Logging_getMessage;
static jmethodID Logging_getMessage_arg;
static jmethodID Logger_warning;
static jmethodID Logger_severe;

Logging *Logging::logger_instance;

void Logging_initializeNative(JNIEnv *env)
{
    Logging_cls = (jclass)env->NewGlobalRef(env->FindClass("gov/nasa/worldwind/util/Logging"));
    Logger = (jclass)env->NewGlobalRef(env->FindClass("java/util/logging/Logger"));

    Logging_logger = env->GetStaticMethodID(Logging_cls, "logger", "()Ljava/util/logging/Logger;");
    assert(Logging_logger);
    Logger_warning = env->GetMethodID(Logger, "warning", "(Ljava/lang/String;)V");
    assert(Logger_warning);
    Logger_severe = env->GetMethodID(Logger, "severe", "(Ljava/lang/String;)V");
    assert(Logger_severe);
    Logging_getMessage = env->GetStaticMethodID(Logging_cls, "getMessage", "(Ljava/lang/String;)Ljava/lang/String;");
    assert(Logging_getMessage);
    Logging_getMessage_arg = env->GetStaticMethodID(Logging_cls, "getMessage", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    assert(Logging_getMessage_arg);
}

Logging::Logging(JNIEnv *env)
    : javaVM(NULL)
{
    assert(env);

    // Store a pointer to the Java VM so that we can get a JNIEnv reference later
    jint ret = env->GetJavaVM(&javaVM);
    if (ret != JNI_OK)
    {
        ATLTRACE("Failed to get Java VM from JNIEnv");
        assert(false && "Failed to get Java VM from JNIEnv");
    }
}

void Logging::initialize(JNIEnv *env)
{
    Logging::logger_instance = new Logging(env);
    Logging_initializeNative(env);
}

Logging* Logging::logger()
{
    return Logging::logger_instance;
}

jstring Logging::getMessage(JNIEnv *env, const wchar_t *msgKey, HRESULT errorCode)
{
    jstring jErrorMsgString = NULL;
    if (errorCode != ERROR_SUCCESS)
    {
        wchar_t *errorMsgBuffer;
        FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            errorCode,
            0, // Default language
            (LPTSTR) &errorMsgBuffer,
            0,
            NULL 
        );

        // Create a new Java String
        jErrorMsgString = env->NewString(reinterpret_cast<jchar*>(errorMsgBuffer), static_cast<jsize>(wcslen(errorMsgBuffer)));

        LocalFree(errorMsgBuffer);
    }

    // Get the exeption message from the resources bundle
    jstring jMsg = env->NewString(reinterpret_cast<const jchar*>(msgKey), static_cast<jsize>(wcslen(msgKey)));
    assert(jMsg);
    jstring msgJString = reinterpret_cast<jstring>(env->CallStaticObjectMethod(Logging_cls, Logging_getMessage_arg, jMsg, jErrorMsgString));
    assert(msgJString);

    return msgJString;
}

jstring Logging::getMessage(JNIEnv *env, const wchar_t *msgKey, const wchar_t *arg)
{
    jstring jArg = env->NewString(reinterpret_cast<const jchar*>(arg), static_cast<jsize>(wcslen(arg)));

    // Get the exeption message from the resources bundle
    jstring jMsg = env->NewString(reinterpret_cast<const jchar*>(msgKey), static_cast<jsize>(wcslen(msgKey)));
    assert(jMsg);
    jstring msgJString = reinterpret_cast<jstring>(env->CallStaticObjectMethod(Logging_cls, Logging_getMessage_arg, jMsg, jArg));
    assert(msgJString);

    return msgJString;
}

void Logging::warning(const wchar_t *msg)
{
    this->warning(msg, ERROR_SUCCESS);
}

void Logging::warning(const wchar_t *msg, HRESULT errorCode)
{
    JNIEnv *env = NULL;
    jstring jErrorMsgString = NULL;

    // Attach to the Java thread
    jint ret = this->javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        ATLTRACE("Failed to attach thread to Java VM: %d", ret);
        return;
    }

    jstring jMsg = getMessage(env, msg, errorCode);

    // Log the message
    jobject logger = env->CallStaticObjectMethod(Logging_cls, Logging_logger, NULL);
    env->CallVoidMethod(logger, Logger_warning, jMsg);

    this->javaVM->DetachCurrentThread();
}

void Logging::warning(const wchar_t *msg, const wchar_t *arg)
{
    JNIEnv *env = NULL;
    jstring jErrorMsgString = NULL;

    // Attach to the Java thread
    jint ret = this->javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        ATLTRACE("Failed to attach thread to Java VM: %d", ret);
        return;
    }

    jstring jMsg = getMessage(env, msg, arg);

    // Log the message
    jobject logger = env->CallStaticObjectMethod(Logging_cls, Logging_logger, NULL);
    env->CallVoidMethod(logger, Logger_warning, jMsg);

    this->javaVM->DetachCurrentThread();
}

void Logging::severe(const wchar_t *msg)
{
    this->severe(msg, ERROR_SUCCESS);
}

void Logging::severe(const wchar_t *msg, HRESULT errorCode)
{
    JNIEnv *env = NULL;

    // Attach to the Java thread
    jint ret = this->javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        ATLTRACE("Failed to attach thread to Java VM: %d", ret);
        return;
    }

    jstring jMsg = getMessage(env, msg, errorCode);

    // Log the message
    jobject logger = env->CallStaticObjectMethod(Logging_cls, Logging_logger, NULL);
    env->CallVoidMethod(logger, Logger_severe, jMsg);

    this->javaVM->DetachCurrentThread();
}

void Logging::severe(const wchar_t *msg, const wchar_t *arg)
{
    JNIEnv *env = NULL;

    // Attach to the Java thread
    jint ret = this->javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        ATLTRACE("Failed to attach thread to Java VM: %d", ret);
        return;
    }

    jstring jMsg = getMessage(env, msg, arg);

    // Log the message
    jobject logger = env->CallStaticObjectMethod(Logging_cls, Logging_logger, NULL);
    env->CallVoidMethod(logger, Logger_severe, jMsg);

    this->javaVM->DetachCurrentThread();
}
