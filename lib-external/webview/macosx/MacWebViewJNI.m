/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "JNIUtil.h"
#import "PropertyChangeListener.h"
#import "ThreadSupport.h"
#import "WebResourceResolver.h"
#import "WebViewWindow.h"
#import "WebViewWindowController.h"
#import <JavaVM/jni.h> // Java JNI header.
#import <JavaNativeFoundation/JavaNativeFoundation.h> // Helper framework for Cocoa and JNI development.

/*
 * Version $Id: MacWebViewJNI.m 1948 2014-04-19 20:02:38Z dcollins $
 */

/* AVKey class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(AVKey_class, "gov/nasa/worldwind/avlist/AVKey");
static JNF_STATIC_MEMBER_CACHE(AVKey_BOUNDS, AVKey_class, "BOUNDS", "Ljava/lang/String;");
static JNF_STATIC_MEMBER_CACHE(AVKey_MIME_TYPE, AVKey_class, "MIME_TYPE", "Ljava/lang/String;");
static JNF_STATIC_MEMBER_CACHE(AVKey_RECTANGLES, AVKey_class, "RECTANGLES", "Ljava/lang/String;");
static JNF_STATIC_MEMBER_CACHE(AVKey_TARGET, AVKey_class, "TARGET", "Ljava/lang/String;");
static JNF_STATIC_MEMBER_CACHE(AVKey_URL, AVKey_class, "URL", "Ljava/lang/String;");

/* AVList class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(AVList_class, "gov/nasa/worldwind/avlist/AVList");
static JNF_MEMBER_CACHE(AVList_setValue, AVList_class, "setValue", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");

/* AVListImpl class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(AVListImpl_class, "gov/nasa/worldwind/avlist/AVListImpl");
static JNF_CTOR_CACHE(AVListImpl_init, AVListImpl_class, "()V");

JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_allocWebViewWindow(JNIEnv *env, jclass clazz,
    jobject frameSize)
{
    InitLoggerJNI(env);

	WebViewWindow *window; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (frameSize == NULL)
    {
        // Throw an exception if the frame size is null. This exception is caught in JNF_COCOA_EXIT and sent to the JVM.
        // The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we do not own
        // it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"nullValue.SizeIsNull");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

	// Create a native WebViewWindow and return it to Java. The Java caller is responsible for releasing the returned
	// object by calling releaseWebViewWindow.
	window = [[WebViewWindow alloc] initWithFrameSize:NSSizeFromJavaDimension(env, frameSize)];

    // Initialize the window's WebView and schedule the global update timer to send the window an onUpdate
    // message at a regular interval. This function is called from the Java EDT or another Java thread, but access to
    // WebKit classes and the update timer must be synchronized on the AppKit thread. For this reason we send both
    // messages on the AppKit thread using the Grand Central Dispatch queue. Note that UpdateTimer retains its
    // listeners, so we release its reference to the WebViewWindow in releaseWebViewWindow.
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window initWebView];
            [[WebViewWindowController sharedInstance] addWindow:window];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionInitializingWebView", e));
        }
    }];

JNF_COCOA_EXIT(env);

	return ptr_to_jlong(window);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_releaseWebViewWindow(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // Stop the window from receiving onUpdate messages from the global update timer then closes and released the native
    // window allocated in allocWebViewWindow. This stops the global update timer if the window is the only update
    // listener (or is the last listener removed). This function is called from the Java EDT or another Java thread, but
    // access to WebKit classes and the update timer must be synchronized on the AppKit thread. For this reason we send
    // both messages on the AppKit thread using the Grand Central Dispatch queue. Note that The UpdateTimer retains its
    // listeners, so we release its reference to the WebViewWindow here.
    WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [[WebViewWindowController sharedInstance] removeWindow:window];
            [window close]; // Closes and automatically releases the window.
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionReleasingWebView", e));
        }
    }];
    
JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setHTMLString(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr, jstring htmlString)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

	NSString *nsHtmlString = nil;
	if (htmlString != NULL)
	{
        // Convert the Java htmlString to a Cocoa NSString. The NSString returned by JNFJavaToNSString is autoreleased.
        // We do not retain it here because this function does not own it.
	    nsHtmlString = JNFJavaToNSString(env, htmlString);
	}

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue. The window does not retain the htmlString, but the dispatch block retains it until after
    // the block executes.
    WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window setHTMLString:nsHtmlString];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingHTMLString", e));
        }
	}];

JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setHTMLStringWithBaseURL(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jstring htmlString, jobject baseURL)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    NSString *nsHtmlString = nil;
	if (htmlString != NULL)
	{
	    // Convert the Java htmlString to a Cocoa NSString. The NSString returned by JNFJavaToNSString is autoreleased.
        // We do not retain it here because this function does not own it.
	    nsHtmlString = JNFJavaToNSString(env, htmlString);
    }

    NSURL *nsBaseURL = nil;
    if (baseURL != NULL)
    {
        // Convert the Java URL to a Cocoa NSURL. The NSURL returned by NSURLFromJavaURL is autoreleased. We do not
        // retain it here because this function does not own it.
        nsBaseURL = NSURLFromJavaURL(env, baseURL);
    }

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue. The window does not retain the htmlString, but the dispatch block retains both the
    // htmlString and the delegate until after  the block executes. The window retains the delegate until it is
    // deallocated or another delegate is specified.
    WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window setHTMLString:nsHtmlString baseURL:nsBaseURL];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingHTMLString", e));
        }
	}];

JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setHTMLStringWithResourceResolver(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jstring htmlString, jobject resourceResolver)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    NSString *nsHtmlString = nil;
	if (htmlString != NULL)
	{
	    // Convert the Java htmlString to a Cocoa NSString. The NSString returned by JNFJavaToNSString is autoreleased.
        // We do not retain it here because this function does not own it.
	    nsHtmlString = JNFJavaToNSString(env, htmlString);
    }

    WebResourceResolver *nsResourceResolver = nil;
    if (resourceResolver != NULL)
    {
        // Wrap the Java WebResourceResolver in a Cocoa WebResourceResolver. The cocoa object adapts the WebView's
        // resource load delegate protocol to the Java WebResourceResolver interface. We autorelease the resolver
        // because we no longer own it when this method exits; it is retained by the window.
        nsResourceResolver = [[WebResourceResolver alloc] initWithJObject:resourceResolver withEnv:env];
        [nsResourceResolver autorelease];
    }
    
    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue. The window retains the resolver until the window is deallocated or a subsequent call to
    // setHtmlString* replaces it. The window does not retain the htmlString, but the dispatch block it until after the
    // block executes.
    WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window setHTMLString:nsHtmlString resourceResolver:nsResourceResolver];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingHTMLString", e));
        }
	}];

JNF_COCOA_EXIT(env);
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_getFrameSize(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
    jobject jsize = NULL; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

	// This function is called from the Java EDT. The update time must be returned to the current thread, so
	// WebViewWindow implements frameSize to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	jsize = JavaDimensionFromNSSize(env, [window frameSize]);

JNF_COCOA_EXIT(env);

    return jsize;
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setFrameSize(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr, jobject size)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    if (size == NULL)
    {
        // Throw an exception if the frame size is null. This exception is caught in JNF_COCOA_EXIT and sent to the JVM.
        // The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we do not own
        // it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"nullValue.SizeIsNull");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // Convert the Java Dimension to a Cocoa NSSize. We perform this step outside of the AppKit thread block to ensure
    // that the memory referenced by JNIEnv is valid.
    NSSize nsSize = NSSizeFromJavaDimension(env, size);

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
	        [window setFrameSize:nsSize];
	    }
	    @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingFrameSize", e));
        }
	}];

JNF_COCOA_EXIT(env);
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_getContentSize(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
    jobject jsize = NULL; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // This function is called from the Java EDT. The update time must be returned to the current thread, so
	// WebViewWindow implements contentSize to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	NSSize size = [window contentSize];
	if (size.width != 0.0 && size.height != 0.0)
	    jsize = JavaDimensionFromNSSize(env, size);

JNF_COCOA_EXIT(env);

    return jsize;
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_getMinContentSize(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr)
{
    jobject jsize = NULL; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

	// This function is called from the Java EDT. The update time must be returned to the current thread, so
	// WebViewWindow implements minContentSize to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	jsize = JavaDimensionFromNSSize(env, [window minContentSize]);

JNF_COCOA_EXIT(env);

    return jsize;
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setMinContentSize(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr, jobject size)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    NSSize nsSize;
    if (size != NULL)
    {
        // Convert the Java Dimension to a Cocoa NSSize. We perform this step outside of the AppKit thread block to
        // ensure that the memory referenced by JNIEnv is valid.
        nsSize = NSSizeFromJavaDimension(env, size);
    }
    else
    {
        // If the specified Java Dimension is NULL, then pass the size (0, 0), indicating that the WeViewWindow should
        // use the default minimum content size.
        nsSize = NSMakeSize(0, 0);
    }

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
	        [window setMinContentSize:nsSize];
	    }
	    @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingFrameSize", e));
        }
	}];

JNF_COCOA_EXIT(env);
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_getContentURL(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
    jobject jurl = NULL; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // This function is called from the Java EDT. The update time must be returned to the current thread, so
	// WebViewWindow implements updateTime to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	NSURL *url = [window contentURL];
	if (url != nil)
	{
	    // Convert the Cocoa URL NSString to a Java String. The call to JavaURLFromNSURL returns a JNI local reference.
	    // We don't free this reference by calling DeleteLocalRef because it's retained when it's passed back to Java.
	    jurl = JavaURLFromNSURL(env, url);
    }

JNF_COCOA_EXIT(env);

    return jurl;
}

JNIEXPORT jobjectArray JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_getLinks(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
    jobjectArray jlinkParams = NULL; // Return variable must be declared outside of try/catch block.

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // This function is called from the Java EDT. The current links must be returned to the current thread, so
    // WebViewWindow implements links to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	NSArray* linkParams = [window links];
	if (linkParams != nil && [linkParams count] > 0)
	{
	    jlinkParams = (*env)->NewObjectArray(env, [linkParams count],
	        (*env)->FindClass(env, "gov/nasa/worldwind/avlist/AVList"), NULL);

	    int i;
        for (i = 0; i < [linkParams count]; i++)
        {
            NSDictionary *params = (NSDictionary *)[linkParams objectAtIndex:i];

            // Convert the link parameters from a Cocoa NSDictionary to a Java AVList. The calls to JNFNSToJavaString
            // below return JNI local references. We don't free these references by calling DeleteLocalRef because the
            // references are retained by the AVList and passed back to Java.
            jobject jparams = JNFNewObject(env, AVListImpl_init);

            NSString *s = (NSString *)[params valueForKey:LinkHref];
            if (s != nil && [s length] > 0)
            {
                JNFCallObjectMethod(env, jparams, AVList_setValue, JNFGetStaticObjectField(env, AVKey_URL),
                    JNFNSToJavaString(env, s));
            }

            s = (NSString *)[params valueForKey:LinkType];
            if (s != nil && [s length] > 0)
            {
                JNFCallObjectMethod(env, jparams, AVList_setValue, JNFGetStaticObjectField(env, AVKey_MIME_TYPE),
                    JNFNSToJavaString(env, s));
            }

            s = (NSString *)[params valueForKey:LinkTarget];
            if (s != nil && [s length] > 0)
            {
                JNFCallObjectMethod(env, jparams, AVList_setValue, JNFGetStaticObjectField(env, AVKey_TARGET),
                    JNFNSToJavaString(env, s));
            }

            NSValue *value = (NSValue *)[params valueForKey:LinkBounds];
            if (value != nil)
            {
                jobject jrect = JavaRectangleFromNSRect(env, [value rectValue]);
                JNFCallObjectMethod(env, jparams, AVList_setValue, JNFGetStaticObjectField(env, AVKey_BOUNDS), jrect);
            }

            NSArray *array = (NSArray *)[params valueForKey:LinkRects];
            if (array != nil && [array count] > 0)
            {
                jobjectArray jrectArray = (*env)->NewObjectArray(env, [array count],
                    (*env)->FindClass(env, "java/awt/Rectangle"), NULL);

                int j;
                for (j = 0; j < [array count]; j++)
                {
                    jobject jrect = JavaRectangleFromNSRect(env, [(NSValue *)[array objectAtIndex:j] rectValue]);
                    (*env)->SetObjectArrayElement(env, jrectArray, j, jrect);
                }

                JNFCallObjectMethod(env, jparams, AVList_setValue, JNFGetStaticObjectField(env, AVKey_RECTANGLES),
                    jrectArray);
            }

            (*env)->SetObjectArrayElement(env, jlinkParams, i, jparams);
        }
    }

JNF_COCOA_EXIT(env);

    return jlinkParams;
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_goBack(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window goBack];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionExecutingGoBack", e));
        }
    }];

JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_goForward(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window goForward];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionExecutingGoForward", e));
        }
    }];

JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_sendEvent(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr, jobject event)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    if (event == NULL)
    {
        // Throw an exception if the Java InputEvent is NULL. This exception is caught in JNF_COCOA_EXIT and sent to the
        // JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we do not
        // own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"nullValue.EventIsNull");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }


    WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
    NSEvent *nsEvent = nil;

    @try
    {
        // Translate the AWTEvent to a native NSEvent on the Java EDT or another Java thread. The NSEvent returned by
        // NSEventFromJavaEvent is autoreleased. We do not retain it because this function does not own it.
        nsEvent = NSEventFromJavaEvent(env, event, window);
    }
    @catch (NSException *e)
    {
        // Catch and log any exception that occurred while attempting to convert the Java event to a native NSEvent. We
        // assign the converted NSEvent to nil afterward to indicate the event cannot be converted. The NSStrings
        // returned by GetLogMessageWithArgExceptionObtainEnv and NSStringFromJavaObject are autoreleased. We do not
        // retain them here because we do not own them; we let the autorelease pool release them instead.
        LogSevere(env, GetLogMessageWithArgException(env, @"WebView.NativeExceptionConvertingEvent",
            NSStringFromJavaObject(env, event), e));
        // Ensure that the reference representing the converted NSEvent is nil.
        nsEvent = nil;
    }

    if (nsEvent != nil)
    {
        // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
        // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
        // Central Dispatch queue. Note that the window does not retain the event, but the dispatch block retains it
        // until after the block executes.
        [[ThreadSupport sharedInstance] performBlockOnMainThread:
        ^{
            @try
            {
                [window sendEvent:nsEvent];
            }
            @catch (NSException *e)
            {
                // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to
                // the JVM, because they occur on another thread than the calling thread. Instead, we log a message that
                // includes the reason for the native exception. The NSString returned by
                // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not
                // own it; we let the autorelease pool release it instead.
                LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSendingEvent", e));
            }
        }];
    }

JNF_COCOA_EXIT(env);
}

JNIEXPORT jboolean JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_mustDisplayInTexture(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr)
{
    BOOL result;

// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

	// This function is called from the Java EDT. The determination of whether the texture must be updated must be
	// returned to the current thread, so WebViewWindow implements mustDisplayInTexture to correctly operate when called
	// from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	result = [window mustDisplayInTexture];

JNF_COCOA_EXIT(env);

    return (result == YES) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_displayInTexture(JNIEnv *env, jclass clazz,
    jlong webViewWindowPtr, jint target)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }
    
	// This function is called from the Java EDT. The OpenGL texture must be updated on the current thread, so
	// WebViewWindow implements displayInTexture to correctly operate when called from a non AppKit thread.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[window displayInTexture:(GLenum)target];

JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_MacWebViewJNI_setPropertyChangeListener(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jobject listener)
{
// Use JNF_COCOA_ENTER/EXIT block to catch and handle exceptions and set up an autorelease pool. We cannot assume that a
// function called outside of the main AppKit thread has an autorelease pool.
JNF_COCOA_ENTER(env);

    if (webViewWindowPtr == 0)
    {
        // Throw an exception if the WebViewWindow address is zero. This exception is caught in JNF_COCOA_EXIT and sent
        // to the JVM. The NSString returned by GetLogMessage is autoreleased. We do not retain the message because we
        // do not own it; we let the autorelease pool release it instead.
        NSString *message = GetLogMessage(env, @"WebView.WebViewAddressIsZero");
        LogSevere(env, message);
        @throw [NSException exceptionWithName:@"IllegalArgumentException" reason:message userInfo:nil];
    }

    // Wrap the Java listener in a Cocoa PropertyChangeListener. We autorelease the listener because this function does
    // not own it; it is retained by the window.
	PropertyChangeListener *nsListener = nil;
	if (listener != NULL)
	{
        nsListener = [[PropertyChangeListener alloc] initWithJObject:listener withEnv:env];
        [nsListener autorelease];
    }

    // This function is called from the Java EDT or another Java thread, but access to WebKit classes must be
    // synchronized on the AppKit thread. For this reason we send the message on the AppKit thread using the Grand
    // Central Dispatch queue. Note that the window retains the listener until the window is deallocated, and the
    // dispatch block retains it until after the block executes.
	WebViewWindow *window = (WebViewWindow *) jlong_to_ptr(webViewWindowPtr);
	[[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        @try
        {
            [window setPropertyChangeListener:nsListener];
        }
        @catch (NSException *e)
        {
            // Catch any native exceptions that occurred in the AppKit thread. We cannot send these exceptions to the
            // JVM, because they occur on another thread than the calling thread. Instead, we log a message that
            // includes the reason for the native exception. The NSString returned by
            // GetLogMessageWithExceptionObtainEnv is autoreleased. We do not retain the message because we do not own
            // it; we let the autorelease pool release it instead.
            LogSevereObtainEnv(GetLogMessageWithExceptionObtainEnv(@"WebView.NativeExceptionSettingUpdateListener", e));
        }
    }];

JNF_COCOA_EXIT(env);
}
