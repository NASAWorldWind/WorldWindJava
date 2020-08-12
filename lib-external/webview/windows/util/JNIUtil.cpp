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
