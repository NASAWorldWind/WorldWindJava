/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * Version $Id: JNIUtil.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "../stdafx.h"
#include "JNIUtil.h"
#include "Logging.h"

const char *ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";
const char *WW_RUNTIME_EXCEPTION = "gov/nasa/worldwind/exception/WWRuntimeException";

// Based on example code in "The Java Native Interface Programmers Guide and Specification" page 75.
void JNU_ThrowByName(JNIEnv *env, const char *name, const wchar_t *msg)
{
    JNU_ThrowByName(env, name, msg, ERROR_SUCCESS);
}

void JNU_ThrowByName(JNIEnv *env, const char *name, const wchar_t *msg, HRESULT errorCode)
{
    jclass Logging = NULL;
    jclass exceptionCls = NULL;
    
    exceptionCls = env->FindClass(name);
    if (exceptionCls == NULL)
        goto done;

    Logging = env->FindClass("gov/nasa/worldwind/util/Logging");
    if (Logging == NULL)
        goto done;

    // Get the exeption message from the resources bundle
    jstring jMsg = Logging::getMessage(env, msg, errorCode);

    const char *msgStr = env->GetStringUTFChars(jMsg, NULL);

    // Throw the exception
    env->ThrowNew(exceptionCls, msgStr);

done:
    if (exceptionCls)
        env->DeleteLocalRef(exceptionCls);

    if (Logging)
        env->DeleteLocalRef(Logging);

    if (jMsg)
        env->ReleaseStringUTFChars(jMsg, msgStr);
}
