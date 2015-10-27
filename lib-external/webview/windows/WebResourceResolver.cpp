/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * Version $Id: WebResourceResolver.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "WebResourceResolver.h"
#include "util/Logging.h"

static jclass WebResourceResolver;
static jmethodID WebResourceResolver_resolve;
static jclass Object;
static jmethodID Object_toString;

void WebResourceResolver_initializeNative(JNIEnv *env)
{
    WebResourceResolver = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("gov/nasa/worldwind/util/webview/WebResourceResolver")));
    WebResourceResolver_resolve = env->GetMethodID(WebResourceResolver, "resolve", "(Ljava/lang/String;)Ljava/net/URL;");

    Object = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Object")));
    Object_toString = env->GetMethodID(Object, "toString", "()Ljava/lang/String;");
}

WebResourceResolver::WebResourceResolver(JNIEnv *env, jobject jobj)
    : refCount(1)
{
    assert(env);
    assert(jobj);

    jObject = reinterpret_cast<jobject>(env->NewGlobalRef(jobj));

    WebResourceResolver_initializeNative(env);

    // Store a pointer to the Java VM so that we can get a JNIEnv reference later
    jint ret = env->GetJavaVM(&javaVM);
    if (ret != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        assert(false && "Failed to get Java VM from JNIEnv");
    }
}

WebResourceResolver::~WebResourceResolver()
{
    JNIEnv *env;
    jint ret = javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.FailedToAttachToVM");
        return;
    }

    env->DeleteGlobalRef(this->jObject);

    javaVM->DetachCurrentThread();
}

HRESULT WebResourceResolver::resolve(const wchar_t *address, wchar_t *result, DWORD *chResult) const
{
    HRESULT ret = S_OK;
    JNIEnv *env = NULL;

    if (address == NULL || result == NULL || chResult == NULL)
        return E_POINTER;

    *result = NULL;

    // Attach to the Java thread
    jint err = javaVM->AttachCurrentThread((void**)&env, NULL);
    if (err != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.FailedToAttachToVM");
        ret = E_UNEXPECTED;
        goto cleanup;
    }

    jstring jAddress = env->NewString(reinterpret_cast<const jchar*>(address), static_cast<jsize>(wcslen(address)));
    if (jAddress == NULL)
    {
        ret = E_OUTOFMEMORY;
        goto cleanup;
    }

    // Invoke resolve
    jobject url = env->CallObjectMethod(this->jObject, WebResourceResolver_resolve, jAddress);

    jstring urlStr = NULL;
    const jchar *urlChars;
    if (url != NULL)
    {
        // Convert URL to string
        urlStr = reinterpret_cast<jstring>(env->CallObjectMethod(url, Object_toString));

        urlChars = env->GetStringChars(urlStr, NULL);
        jsize len = env->GetStringLength(urlStr);

        if (static_cast<DWORD>(len) + 1 <= *chResult) // Cast to DWORD to avoid signed/unsigned comparison. len is string length, which will never be negative
        {
            wcscpy_s(result, *chResult, reinterpret_cast<const wchar_t*>(urlChars));
        }
        else
        {
            ret = S_FALSE;
            *chResult = static_cast<DWORD>(len) + 1;
        }
    }

cleanup:
    if (urlStr)
        env->ReleaseStringChars(urlStr, urlChars);

    javaVM->DetachCurrentThread();

    return ret;
}

////////////////////////////////////////
// IUnknown
////////////////////////////////////////

STDMETHODIMP WebResourceResolver::QueryInterface(REFIID riid, void **ppvObject)
{
    *ppvObject = NULL;

    if (riid == IID_IUnknown)
	    *ppvObject = static_cast<IUnknown*>(this);

    if (*ppvObject)
    {
	    static_cast<IUnknown*>(*ppvObject)->AddRef();
	    return S_OK;
    }
    else return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE WebResourceResolver::AddRef()
{
    return this->refCount++;
}

ULONG STDMETHODCALLTYPE WebResourceResolver::Release()
{
    this->refCount--;

    if (this->refCount == 0)
    {
        delete this;
        return 0;
    }

    return this->refCount;
}
