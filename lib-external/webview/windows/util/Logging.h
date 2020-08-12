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
