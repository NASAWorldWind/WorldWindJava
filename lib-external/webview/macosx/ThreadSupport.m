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