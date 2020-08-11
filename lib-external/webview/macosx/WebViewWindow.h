#ifndef WEB_VIEW_WINDOW_H
#define WEB_VIEW_WINDOW_H

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
#import <Cocoa/Cocoa.h>
#import <OpenGL/gl.h>
#import <WebKit/WebKit.h>

/*
    WebViewWindow provides a display of a WebView as an OpenGL texture. However, NSWindows cannot be displayed directly
    into a texture, and cannot be accessed from the Java Event Dispatch Thread (EDT). WebViewWindow resolves this by
    displaying to a backing buffer in the Apple AppKit thread, then loading the backing buffer in an OpenGL texture on
    the Java Event Dispatch Thread.

    WebViewWindow synchronizes access between the AppKit and Java EDT using an NSLock. According to the Apple JNI
    documentation, blocking the AppKit thread against the EDT or vice-versa causes deadlock because the AppKit thread
    is responsible for running the EDT. For details, see http://developer.apple.com/library/mac/#technotes/tn2005/tn2147.html
    WebViewWindow places locks around critical native code that does not wait for the Java EDT, thereby avoiding the
    possibility of a deadlock.

    Version $Id: WebViewWindow.h 1171 2013-02-11 21:45:02Z dcollins $
 */

extern NSString *LinkBounds;
extern NSString *LinkHref;
extern NSString *LinkRects;
extern NSString *LinkTarget;
extern NSString *LinkType;

@interface WebViewWindow : NSWindow
{
@protected
	// WebView properties.
	BOOL webViewInitialized;
    NSString *htmlString;
    NSURL *baseURL;
    id resourceResolver;
    WebHistoryItem *htmlStringHistoryItem;
	NSLock *edtLock;
    // Display properties.
	long long displayTime; // 64-bit integer.
	long long textureDisplayTime; // 64-bit integer.
	NSBitmapImageRep *displayBuffer;
	NSMutableArray *linksBuffer;
	NSMutableArray *links;
	// Content info properties.
	long long contentUpdateTime; // 64-bit integer.
	long long contentInfoUpdateTime; // 64-bit integer.
	NSSize contentSize;
	NSSize minContentSize;
	NSURL *contentURL;
	// Event handling support.
	char *consumedKeyDownEvents;
	NSScroller *activeScroller;
	double scrollerLastPosition;
	double scrollerOffset;
    // Property change event handling.
	id propertyChangeListener;
}

- (id)initWithFrameSize:(NSSize)frameSize;

- (void)initWebView;

- (void)setHTMLString:(NSString *)htmlString;

- (void)setHTMLString:(NSString *)htmlString baseURL:(NSURL *)url;

- (void)setHTMLString:(NSString *)htmlString resourceResolver:(id)resolver;

- (void)loadHTMLString:(NSString *)string baseURL:(NSURL *)url resourceResolver:(id)resolver;

- (void)reloadHTMLString;

- (NSURL *)resolve:(NSURL *)url;

- (NSSize)frameSize;

- (void)setFrameSize:(NSSize)size;

- (NSSize)contentSize;

- (NSSize)minContentSize;

- (void)setMinContentSize:(NSSize)size;

- (NSURL *)contentURL;

- (NSArray *)links;

- (void)goBack;

- (void)goForward;

- (void)sendEvent:(NSEvent *)event;

- (void)makeDisplay;

- (void)doMakeDisplay;

- (BOOL)mustRegenerateDisplayBuffer;

- (void)makeDisplayBuffer;

- (BOOL)mustDisplayInTexture;

- (void)displayInTexture:(GLenum)target;

- (void)makeContentInfo;

- (void)determineContentSize;

- (void)determineContentURL;

- (void)setPropertyChangeListener:(id)listener;

- (void)firePropertyChange;

// WebFrameLoadDelegate protocol.

- (void)webView:(WebView *)sender didFinishLoadForFrame:(WebFrame *)frame;

- (void)webView:(WebView *)sender didFailLoadWithError:(NSError *)error forFrame:(WebFrame *)frame;

- (void)webView:(WebView *)sender didFailProvisionalLoadWithError:(NSError *)error forFrame:(WebFrame *)frame;

// WebPolicyDelegate protocol.

- (void)webView:(WebView *)webView decidePolicyForMIMEType:(NSString *)type request:(NSURLRequest *)request
    frame:(WebFrame *)frame decisionListener:(id < WebPolicyDecisionListener >)listener;

- (void)webView:(WebView *)webView decidePolicyForNewWindowAction:(NSDictionary *)actionInformation
    request:(NSURLRequest *)request newFrameName:(NSString *)frameName
    decisionListener:(id < WebPolicyDecisionListener >)listener;

// WebUIDelegate protocol.

- (NSArray *)webView:(WebView *)sender contextMenuItemsForElement:(NSDictionary *)element
    defaultMenuItems:(NSArray *)defaultMenuItems;

- (WebView *)webView:(WebView *)sender createWebViewModalDialogWithRequest:(NSURLRequest *)request;

- (WebView *)webView:(WebView *)sender createWebViewWithRequest:(NSURLRequest *)request;

- (void)webView:(WebView *)sender printFrameView:(WebFrameView *)frameView;

//  WebResourceLoadDelegate protocol.

- (NSURLRequest *)webView:(WebView *)sender resource:(id)identifier willSendRequest:(NSURLRequest *)request
    redirectResponse:(NSURLResponse *)redirectResponse fromDataSource:(WebDataSource *)dataSource;

@end

#endif /* WEB_VIEW_WINDOW_H */
