#ifndef THREAD_SUPPORT_H
#define THREAD_SUPPORT_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import <Cocoa/Cocoa.h>

/*
    Version $Id: ThreadSupport.h 1948 2014-04-19 20:02:38Z dcollins $
 */
@interface ThreadSupport : NSObject

+ (ThreadSupport *)sharedInstance;

- (void)performBlockOnMainThread:(void(^)(void))block;

- (void)doPerformBlock:(void(^)(void))block;

@end

#endif /* THREAD_SUPPORT_H */