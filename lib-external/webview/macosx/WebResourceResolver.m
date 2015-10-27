/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "WebResourceResolver.h"
#import <JavaVM/jni.h> // Java JNI header.

/*
    Version $Id: WebResourceResolver.m 1171 2013-02-11 21:45:02Z dcollins $
 */
@implementation WebResourceResolver

/*
    Specifying a nil base URL causes WebViews to use a default base URL of "applewebdata://ID/", where "ID" is a
    generated unique ID. This fake base URL is used to resolve relative resource paths. WebResourceResolver identifies
    resource paths starting with this schema and host, interprets them as relative paths, and attempts to resolve them
    by calling the Java method WebResourceResolver.resolve(String).
*/
static NSString *LOCAL_REFERENCE_SCHEME = @"applewebdata";
/* The default resource resolver instance. Created in WebResourceResolver's static initializer. */
static WebResourceResolver *defaultInstance;

/* JNI class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(Object_class, "java/lang/Object");
static JNF_MEMBER_CACHE(Object_toString, Object_class, "toString", "()Ljava/lang/String;");
static jclass WebResourceResolver_class = NULL;
static jmethodID WebResourceResolver_resolve = NULL;

/*
    Initializes the WebResourceResolver's shared default instance. Called exactly once just before WebResourceResolver
    receives its first message. May also be called explicitly, or by a subclass that overrides this message.
 */
+ (void)initialize
{
    static BOOL initialized = NO;

    if (!initialized)
    {
        initialized = YES;
        defaultInstance = [[WebResourceResolver alloc] init];
    }
}

- (id)init
{
    [super init];
    return self;
}

- (id)initWithJObject:(jobject)resourceResolver withEnv:(JNIEnv *)env
{
    [super initWithJObject:resourceResolver withEnv:env];

    if (WebResourceResolver_class == NULL)
    {
        // Initialize the WebResourceResolver class and methodID pointers when the first WebResourceResolver is created.
        // We do this here rather than after obtaining an environment in resolve because the World Wind Java classes
        // cannot be found from the AppKit thread. By finding them here and retaining a reference to them, we make them
        // accessible by calls to resolve from the AppKit thread. Note that this stores the class and method references
        // in static variables, and therefore assumes that there is one class definition for
        // gov.nasa.worldwind.util.webview.WebResourceResolver in the JNI environment.
        WebResourceResolver_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "gov/nasa/worldwind/util/webview/WebResourceResolver"));
        WebResourceResolver_resolve = (*env)->GetMethodID(env, WebResourceResolver_class, "resolve", "(Ljava/lang/String;)Ljava/net/URL;");
    }

    return self;
}

+ (WebResourceResolver *)defaultResourceResolver
{
    return defaultInstance;
}

- (NSURL *)resolve:(NSURL *)url
{
    // There's nothing to resolve if the specified URL is nil.
    if (url == nil)
        return nil;

    // Don't attempt to resolve non-relative URLs; return nil indicating that the URL cannot be resolved.
    if (![self isRelative:url])
        return nil;

    // Allocate a mutable string to hold the relative parts of the resource's URL. The NSMutableString returned by
    // stringWithCapacity is autoreleased, but we do not retain it because this function does not own it.
    NSMutableString *address = [NSMutableString stringWithCapacity:8];

    if ([url relativePath] != nil)
    {
        // Cross-platform Java code consuming these URLS expects the address to start with a relative folder name or a
        // relative file name without any leading slash. Since we know the address represents a relative path, we strip
        // the leading slash to ensure that the caller recognizes the address as a relative path. The NSString returned
        // by substringFromIndex is autoreleased. We do not retain it here because it is not used outside of this scope.
        NSString *path = [url relativePath];
        if ([path hasPrefix:@"/"])
            path = [path substringFromIndex:1];

        [address appendString:path];
    }

    if ([url query] != nil)
    {
        [address appendString:@"?"];
        [address appendString:[url query]];
    }

    if ([url fragment] != nil)
    {
        [address appendString:@"#"];
        [address appendString:[url fragment]];
    }

    if ([address length] == 0)
        return nil;

    return [self resolveAddress:address];
}

- (BOOL)isRelative:(NSURL *)url
{
    if (url == nil || [url scheme] == nil)
        return NO;

    return [LOCAL_REFERENCE_SCHEME caseInsensitiveCompare:[url scheme]] == NSOrderedSame;
}

- (NSURL *)resolveAddress:(NSString *)address
{
    if (address == nil)
        return nil;

    // If the Java resource resolver is able to resolve this address, then return the resolved URL.
    NSURL *url = [self resolveAddressWithJObject:address];
    if (url != nil)
        return url;

    // The Java resource resolver is not able to resolve this address. We return the address as a local URL. The NSURL
    // returned by URLWithString is autoreleased. We do not retain it here because this function does not own it.
    return [NSURL URLWithString:address];
}

- (NSURL *)resolveAddressWithJObject:(NSString *)address
{
    if ([self jObject] == nil)
        return nil;

    NSURL *nsURL = nil;

    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the
    // AppKit thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);

        // Convert the Cocoa address NSString to a Java String. JNFNSToJavaString below returns a JNI local references.
        // We free this reference by calling DeleteLocalRef after the call to WebResourceResolver.resolve executes.
        jobject jAddress = JNFNSToJavaString(env, address);

        // Call the Java interface method WebResourceResolver.resolve with the resource address. If the returned Java
        // URL is not NULL, we convert is to a Cocoa NSURL.
        jobject jURL = (*env)->CallObjectMethod(env, [self jObject], WebResourceResolver_resolve, jAddress);
        if (jURL != NULL)
        {
            // Get the Java URL's String representation and convert it to a Cocoa NSURL. The NSString returned by
            // JNFJavaToNSString and the NSURL returned by URLWithString are autoreleased. We do not retain them here
            // because this function does not own them.
            jobject jURLString = JNFCallObjectMethod(env, jURL, Object_toString);
            NSString *nsURLString = JNFJavaToNSString(env, jURLString);
            nsURL = [NSURL URLWithString:nsURLString];
        }

        (*env)->DeleteLocalRef(env, jAddress);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }

    return nsURL;
}

@end
