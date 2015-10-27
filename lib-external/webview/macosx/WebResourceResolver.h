#ifndef WEB_RESOURCE_RESOLVER_H
#define WEB_RESOURCE_RESOLVER_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h> // Helper framework for Cocoa and JNI development.

/*
    Version $Id: WebResourceResolver.h 1171 2013-02-11 21:45:02Z dcollins $
 */
@interface WebResourceResolver : JNFJObjectWrapper
{
}

+ (void)initialize;

- (id)init;

- (id)initWithJObject:(jobject)resourceResolver withEnv:(JNIEnv *)env;

+ (WebResourceResolver *)defaultResourceResolver;

- (NSURL *)resolve:(NSURL *)url;

- (BOOL)isRelative:(NSURL *)url;

- (NSURL *)resolveAddress:(NSString *)address;

- (NSURL *)resolveAddressWithJObject:(NSString *)address;

@end

#endif /* WEB_RESOURCE_RESOLVER_H */
