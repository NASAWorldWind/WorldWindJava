#ifndef WEB_VIEW_WINDOW_CONTROLLER_H
#define WEB_VIEW_WINDOW_CONTROLLER_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import <CoreFoundation/CoreFoundation.h>
#import <Cocoa/Cocoa.h>
#import "WebViewWindow.h"

/*
    Version $Id: WebViewWindowController.h 1171 2013-02-11 21:45:02Z dcollins $
 */

static void onRunLoop(CFRunLoopObserverRef observer, CFRunLoopActivity activity, void* info);

@interface WebViewWindowController : NSObject
{
@protected
    NSMutableArray *windows;
    CFRunLoopObserverRef runLoopObserver;
}

+ (WebViewWindowController *)sharedInstance;

+ (void)initialize;

- (id)init;

- (void)dealloc;

- (void)addWindow:(WebViewWindow *)window;

- (void)removeWindow:(WebViewWindow *)window;

- (void)updateWindows;

- (void)displayWindows;

- (void)displayWindowsAfterDelay:(NSTimeInterval)delay;

- (void)attachObservers;

- (void)detachObservers;

- (void)onAppWillUpdate:(NSNotification *)notification;

@end

#endif /* WEB_VIEW_WINDOW_CONTROLLER_H */
