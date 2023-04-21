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

#include <jni.h>

/*
 * Version $Id: WindowsWebViewJNI.h 1171 2013-02-11 21:45:02Z dcollins $
 */

/* Header for class gov_nasa_worldwind_util_webview_WindowsWebViewJNI */

#ifndef _Included_gov_nasa_worldwind_util_webview_WindowsWebView
#define _Included_gov_nasa_worldwind_util_webview_WindowsWebView
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    initNativeCache
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_initialize
  (JNIEnv *env, jclass jclz);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    createMessageLoop
 * Signature: ()V
 */
JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newMessageLoop
  (JNIEnv *env, jclass jclz);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    createMessageLoop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseMessageLoop
  (JNIEnv *env, jclass jclz, jlong messageLoop);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    runMessageLoop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_runMessageLoop
  (JNIEnv *env, jclass jclz, jlong messageLoop);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    releaseBrowser
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseWebView
  (JNIEnv *, jclass, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    releaseBrowser
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseComObject
  (JNIEnv *env, jclass jclz, jlong unknownPtr);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    newWebViewWindow
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newWebViewWindow
  (JNIEnv *, jclass, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setActive
 * Signature: (JB)J
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setActive
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    newNotificationAdapter
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newNotificationAdapter(JNIEnv *env,
    jclass clazz, jobject listener);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    addWindowUpdateObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_addWindowUpdateObserver(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jlong observerPtr);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    removeWindowUpdateObserver
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_removeWindowUpdateObserver(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jlong observerPtr);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    getLinks
 * Signature: (J)[Lgov/nasa/worldwind/avlist/AVList;
 */
JNIEXPORT jobjectArray JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getLinks
  (JNIEnv *, jobject, jlong webViewWindowPtr);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setHTMLString
 * Signature: (JLjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setHTMLString
  (JNIEnv *, jobject, jlong, jstring, jstring);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setHTMLStringWithResourceResolver
 * Signature: (JLjava/lang/String;JLgov/nasa/worldwind/util/webview/WebResourceResolver;)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setHTMLStringWithResourceResolver
  (JNIEnv *, jobject, jlong, jstring, jobject);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setBackgroundColor
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setBackgroundColor
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jstring);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setFrameSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setFrameSize
  (JNIEnv *, jobject, jlong, jint, jint);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    sendEvent
 * Signature: (JLjava/awt/event/InputEvent;)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_sendEvent
  (JNIEnv *, jobject, jlong, jobject);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    getUpdateTime
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getUpdateTime
  (JNIEnv *, jobject, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    loadDisplayInGLTexture
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_loadDisplayInGLTexture
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    getContentSize
 * Signature: (J)Ljava/awt/Dimension;
 */
JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getContentSize
  (JNIEnv *, jobject, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    getMinContentSize
 * Signature: (J)Ljava/awt/Dimension;
 */
JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getMinContentSize
  (JNIEnv *, jobject, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setMinContentSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setMinContentSize
  (JNIEnv *, jobject, jlong, jint, jint);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    getURL
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getContentURL
  (JNIEnv *, jobject, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    goBack
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_goBack
  (JNIEnv *, jobject, jlong);

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    goForward
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_goForward
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
