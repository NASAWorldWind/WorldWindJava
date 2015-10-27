/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "ThreadSupport.h"

/*
    Version $Id: ThreadSupport.m 1948 2014-04-19 20:02:38Z dcollins $
 */

/* The singleton ThreadSupport instance. Created in ThreadSupport's static initializer.*/
static ThreadSupport *sharedInstance;

@implementation ThreadSupport

+ (ThreadSupport *)sharedInstance
{
    return sharedInstance;
}

/*
    Initializes the ThreadSupport's shared singleton instance. Called exactly once just before ThreadSupport receives
    its first message. May also be called explicitly, or by a subclass that overrides this message.
 */
+ (void)initialize
{
    static BOOL initialized = NO;

    if (!initialized)
    {
        initialized = YES;
        sharedInstance = [[ThreadSupport alloc] init];
    }
}

- (void)performBlockOnMainThread:(void(^)(void))block
{
    if (block == NULL)
    {
        return;
    }

    [self performSelectorOnMainThread:@selector(doPerformBlock:) withObject:Block_copy(block) waitUntilDone:NO];
}

- (void)doPerformBlock:(void(^)(void))block
{
    block();
    Block_release(block);
}

@end