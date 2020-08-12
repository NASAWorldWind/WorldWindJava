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
#import "WebViewWindowController.h"

/*
    Version $Id: WebViewWindowController.m 1171 2013-02-11 21:45:02Z dcollins $
 */

/* The default initial capacity of the update timer's array. */
static const NSUInteger DEFAULT_ARRAY_CAPACITY = 8;
/* The singleton WebViewController instance. Created in WebViewWindowController's static initializer.*/
static WebViewWindowController *sharedInstance;

static void onRunLoop(CFRunLoopObserverRef observer, CFRunLoopActivity activity, void* info)
{
    WebViewWindowController *controller = (WebViewWindowController *)info;
    [controller displayWindows];
}

@implementation WebViewWindowController

+ (WebViewWindowController *)sharedInstance
{
    return sharedInstance;
}

/*
    Initializes the WindowController's shared singleton instance. Called exactly once just before WindowController
    receives its first message. May also be called explicitly, or by a subclass that overrides this message.
 */
+ (void)initialize
{
    static BOOL initialized = NO;

    if (!initialized)
    {
        initialized = YES;
        sharedInstance = [[WebViewWindowController alloc] init];
    }
}

- (id)init
{
    [super init];

    windows = [[NSMutableArray alloc] initWithCapacity:DEFAULT_ARRAY_CAPACITY];
    runLoopObserver = NULL;

    return self;
}

- (void)dealloc
{
    if (windows != nil)
    {
        [windows release];
        windows = nil;
    }

    [super dealloc];
}

- (void)addWindow:(WebViewWindow *)window
{
    [windows addObject:window];

    [self attachObservers];
}

- (void)removeWindow:(WebViewWindow *)window
{
    [windows removeObject:window];

    if ([windows count] == 0)
    {
        [self detachObservers];
    }
}

- (void)updateWindows
{
    for (WebViewWindow *window in windows)
    {
        if (window != nil) // This should never happen, but we check anyway.
        {
            [window update];
        }
    }
}

- (void)displayWindows
{
    for (WebViewWindow *window in windows)
    {
        if (window != nil) // This should never happen, but we check anyway.
        {
            [window makeDisplay];
        }
    }
}

- (void)displayWindowsAfterDelay:(NSTimeInterval)delay
{
    SEL selector = @selector(displayWindows);
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:selector object:nil];
    [self performSelector:selector withObject:nil afterDelay:delay];
}

- (void)attachObservers
{
    if (runLoopObserver != NULL)
        return;

    CFRunLoopObserverContext context =
    {
        0,    // Version of this structure. Must be zero.
        self, // Info pointer: a reference to this UpdateTimer.
        NULL, // Retain callback for info pointer.
        NULL, // Release callback for info pointer.
        NULL  // Copy description.
    };

    runLoopObserver = CFRunLoopObserverCreate(
        NULL, // Use the default allocator.
        kCFRunLoopBeforeWaiting | kCFRunLoopExit, // Observe when the run loop is waiting and just before it exits.
        true, // Repeats.
        0, // Priority index. Use zero because there's currently no reason to do otherwise.
        onRunLoop,
        &context); // Copied by CFRunLoopObserverCreate, no need to pass a heap pointer.

    CFRunLoopAddObserver(CFRunLoopGetCurrent(), runLoopObserver, kCFRunLoopCommonModes);

    [[NSNotificationCenter defaultCenter] addObserver:self
        selector:@selector(onAppWillUpdate:)
        name:NSApplicationWillUpdateNotification // Observe when the NSApplication will update its windows.
        object:nil]; // Observe all notifications for the specified name.
}

- (void)detachObservers
{
    if (runLoopObserver == NULL)
        return;

    // Removes the observer from all run loop modes in which it has been added.
    CFRunLoopObserverInvalidate(runLoopObserver);
    CFRelease(runLoopObserver);
    runLoopObserver = NULL;

    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)onAppWillUpdate:(NSNotification *)notification
{
    [self updateWindows];
}

@end
