/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "WebViewWindow.h"
#import "WebViewWindowController.h"
#import "OGLUtil.h"
#import "ThreadSupport.h"
#import "WebDownloadController.h"
#import "WebResourceResolver.h"

/*
    Version $Id: WebViewWindow.m 1948 2014-04-19 20:02:38Z dcollins $
 */
@implementation WebViewWindow

NSString *LinkBounds = @"LinkBounds";
NSString *LinkHref = @"LinkHref";
NSString *LinkRects = @"LinkRects";
NSString *LinkTarget = @"LinkTarget";
NSString *LinkType = @"LinkType";
NSString *WebHistoryHTMLStringTitle = @"";
NSString *WebHistoryHTMLStringURL = @"about:blank";

/* The default minimum content width in pixels: 300. Configured to avoid shrinking variable width content to a narrow
   and tall content size. */
static const CGFloat DEFAULT_MIN_CONTENT_WIDTH = 300;
/* The default minimum content height in pixels: 100. Configured to compute a reasonable content size for both short and
   tall content. */
static const CGFloat DEFAULT_MIN_CONTENT_HEIGHT = 100;
/* The delay to use when scheduling the WebViewWindowController to call displayWindows: 10 milliseconds. */
static const NSTimeInterval DISPLAY_WINDOWS_DELAY = 0.01;
/* The default initial capacity of the link params dictionary. */
static const NSUInteger LINK_PARAMS_INITIAL_CAPACITY = 8;
/* The default initial capacity of the link rects array. */
static const NSUInteger LINK_RECTS_INITIAL_CAPACITY = 2;
/* The default initial capacity of the links array. */
static const NSUInteger LINKS_INITIAL_CAPACITY = 8;
/* The minimum window position in pixels. The window server limits window position coordinates to +/-16,000 and sizes
   to 10,000. */
static const CGFloat MIN_WINDOW_POS = -16000;

//**************************************************************//
//********************  Private Interface  *********************//
//**************************************************************//

- (BOOL)hasCoreAnimationLayers:(NSView *)view
{
    if ([view layer] != nil)
        return YES;

    if ([view subviews] != nil)
    {
        for (NSView *subview in [view subviews])
        {
            if ([self hasCoreAnimationLayers:subview])
                return YES;
        }
    }

    return NO;
}

+ (long long)currentTimeMillis
{
    return 1000.0 * [NSDate timeIntervalSinceReferenceDate];
}

//**************************************************************//
//********************  Event Handling  ************************//
//**************************************************************//

- (void)performActionForKeyEvent:(NSEvent *)event
{
    NSString *s = [event charactersIgnoringModifiers];
    if (s == nil)
        return;

    if (([event modifierFlags] & NSDeviceIndependentModifierFlagsMask) == NSCommandKeyMask)
    {
        if ([s caseInsensitiveCompare:@"a"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(selectAll:)])
                [NSApp sendAction:@selector(selectAll:) to:[self firstResponder] from:self];
        }
        else if ([s caseInsensitiveCompare:@"c"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(copy:)])
                [NSApp sendAction:@selector(copy:) to:[self firstResponder] from:self];
        }
        else if ([s caseInsensitiveCompare:@"v"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(paste:)])
                [NSApp sendAction:@selector(paste:) to:[self firstResponder] from:self];
        }
        else if ([s caseInsensitiveCompare:@"x"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(cut:)])
                [NSApp sendAction:@selector(cut:) to:[self firstResponder] from:self];
        }
        else if ([s caseInsensitiveCompare:@"z"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(undo:)])
                [NSApp sendAction:@selector(undo:) to:[self firstResponder] from:self];
        }
        else if ([s caseInsensitiveCompare:@"["] == NSOrderedSame)
        {
            [self goBack];
        }
        else if ([s caseInsensitiveCompare:@"]"] == NSOrderedSame)
        {
            [self goForward];
        }
    }
    else if (([event modifierFlags] & NSDeviceIndependentModifierFlagsMask) == (NSCommandKeyMask|NSShiftKeyMask))
    {
        if ([s caseInsensitiveCompare:@"z"] == NSOrderedSame)
        {
            if ([[self firstResponder] respondsToSelector:@selector(redo:)])
                [NSApp sendAction:@selector(redo:) to:[self firstResponder] from:self];
        }
    }
}

- (BOOL)mustTrackScrollerKnob:(NSScroller *)scroller withEvent:(NSEvent *)event
{
    if ([event type] != NSLeftMouseDown)
        return NO;

    if (![scroller isEnabled])
        return NO;

    return [scroller testPart:[event locationInWindow]] == NSScrollerKnob;
}

- (void)trackScrollerKnob:(NSEvent *)event
{
    if (activeScroller == nil)
        return;

    if ([event type] == NSLeftMouseDown)
    {
        NSRect knobRect = [activeScroller rectForPart:NSScrollerKnob];
        NSPoint point = [activeScroller convertPoint:[event locationInWindow] fromView:nil];

        if (knobRect.size.width > knobRect.size.height) // Scroller is horizontal.
        {
            scrollerLastPosition = NSMidX(knobRect);
            scrollerOffset = scrollerLastPosition - point.x;
        }
        else
        {
            scrollerLastPosition = NSMidY(knobRect);
            scrollerOffset = scrollerLastPosition - point.y;
        }
    }
    else if ([event type] == NSLeftMouseDragged)
    {
        NSRect knobRect = [activeScroller rectForPart:NSScrollerKnob];
        NSRect slotRect = [activeScroller rectForPart:NSScrollerKnobSlot];
        NSPoint point = [activeScroller convertPoint:[event locationInWindow] fromView:nil];

        NSScrollView *scrollView = (NSScrollView *)[activeScroller target];
        NSView *clipView = [scrollView contentView];
        NSView *docView = [scrollView documentView];
        NSRect docRect = [docView bounds];
        NSRect docVisibleRect = [scrollView documentVisibleRect];

        double newPosition;

        if (knobRect.size.width > knobRect.size.height) // Scroller is horizontal.
        {
            newPosition = point.x + scrollerOffset;
            double min = NSMinX(slotRect) + NSWidth(knobRect) / 2.0;
            double max = NSMaxX(slotRect) - NSWidth(knobRect) / 2.0;
            double scrollAmount = (newPosition < min) ? 0.0 :
                (newPosition > max ? 1.0 : (newPosition - min) / (max - min));
            double scrollX = scrollAmount * (docRect.size.width - docVisibleRect.size.width);
            [docView scrollPoint:NSMakePoint(scrollX, docVisibleRect.origin.y)];
        }
        else
        {
            newPosition = point.y + scrollerOffset;
            double min = NSMinY(slotRect) + NSHeight(knobRect) / 2.0;
            double max = NSMaxY(slotRect) - NSHeight(knobRect) / 2.0;
            double scrollAmount = (newPosition <= min) ? 0.0 :
                (newPosition >= max ? 1.0 : (newPosition - min) / (max - min));
            double scrollY = scrollAmount * (docRect.size.height - docVisibleRect.size.height);
            [docView scrollPoint:NSMakePoint(docVisibleRect.origin.x, scrollY)];
        }

        scrollerLastPosition = newPosition;
    }
    else if ([event type] == NSLeftMouseUp)
    {
        activeScroller = nil;
    }
}

//**************************************************************//
//********************  Link Parameters  ***********************//
//**************************************************************//

- (BOOL)isStyleDisplayed:(DOMCSSStyleDeclaration *)style
{
    WebView *webView = (WebView *)[self contentView];

    // The element is not visible if its CSS "display" attribute is set to "none".
    if ([@"none" caseInsensitiveCompare:[style display]] == NSOrderedSame)
        return NO;

    // The element is not visible if its CSS "visibility" attribute is set to "hidden".
    if ([@"hidden" caseInsensitiveCompare:[style visibility]] == NSOrderedSame)
        return NO;

    return YES;
}

- (void)addLineBoxRects:(DOMElement *)element visibleRect:(NSRect)visibleRect withView:(NSView *)view
    toArray:(NSMutableArray *)array
{
    for (NSValue *value in [element lineBoxRects])
    {
        NSRect lineRect = [value rectValue];
        NSRect lineVisibleRect = NSIntersectionRect([view convertRectToBase:lineRect], visibleRect);

        // Ignore the element if it is not in the document view's visible rectangle.
        if (NSIsEmptyRect(lineVisibleRect) == YES)
            continue;

        // The NSValue returned by valueWithRect is autoreleased, but is retained and owned by the array.
        [array addObject:[NSValue valueWithRect:lineVisibleRect]];
    }
}

- (void)addImageRects:(DOMNode *)node linkStyle:(DOMCSSStyleDeclaration *)linkStyle linkRect:(NSRect)linkRect
    visibleRect:(NSRect)visibleRect withView:(NSView *)view toArray:(NSMutableArray *)array
{
    WebView *webView = (WebView *)[self contentView];

    DOMNodeList *childNodes = [node childNodes];
    if (childNodes == nil)
        return;

    int i = 0;
    for (i = 0; i < [childNodes length]; i++)
    {
        DOMNode *child = [childNodes item:i];
        if (child == nil) // This should never happen, but we check anyway.
            continue;

        // Ignore the node if it is not displayed by this window's WebView. This ensures that links hidden by a CSS
        // style are not added to the list.
        if ([child isKindOfClass:[DOMElement class]])
        {
            // The WebView does not display the specified element if the style returned by computedStyleForElement is
            // nil, if the display attribute is "none", or the visibility attriute is "hidden".
            DOMCSSStyleDeclaration *style = [webView computedStyleForElement:(DOMElement *)child pseudoElement:nil];
            if (style == nil || ![self isStyleDisplayed:style])
                continue;
        }

        if ([@"img" caseInsensitiveCompare:[child localName]] == NSOrderedSame)
        {
            // Compute the image's bounding box in the document view's coordinate system, and clip it by the document's
            // visible rectangle.
            NSRect imageRect = [child boundingBox];
            NSRect imageVisibleRect = NSIntersectionRect([view convertRectToBase:imageRect], visibleRect);

            // Clip the image's bounding box against the link's bounding box if the links's CSS overflow attribute is
            // either "hidden" or "scroll". This prevents invisible portions of the image from contributing to the
            // link's pickable area.
            if ([@"hidden" caseInsensitiveCompare:[linkStyle overflow]] == NSOrderedSame
                || [@"scroll" caseInsensitiveCompare:[linkStyle overflow]] == NSOrderedSame)
            {
                imageVisibleRect = NSIntersectionRect(imageVisibleRect, linkRect);
            }

            // Ignore the image if it is not in the document view's visible rectangle.
            if (NSIsEmptyRect(imageVisibleRect))
                continue;

            // The NSValue returned by valueWithRect is autoreleased, but is retained and owned by the array.
            [array addObject:[NSValue valueWithRect:imageVisibleRect]];
        }
        else if ([child hasChildNodes])
        {
            [self addImageRects:child linkStyle:linkStyle linkRect:linkRect visibleRect:visibleRect withView:view
                toArray:array];
        }
    }
}

- (NSDictionary *)createLinkParams:(DOMHTMLAnchorElement *)linkElement withBounds:(NSRect)linkBounds
    withRects:(NSArray *)linkRects;
{
    NSString *linkHref = [linkElement href];

    // Attempt to resolve relative URLs. Relative URLs are either resolved to an absolute URL or stripped of the
    // "applewebdata" scheme added by the WebView. If the URL is relative and can be resolved, we use the resolved href
    // instead of the link's relative href. The NSURL returned by URLWithString and resolve are autoreleased; we let the
    // current autorelease pool reclaim them.
    NSURL *resolvedURL = [self resolve:[NSURL URLWithString:linkHref]];
    if (resolvedURL != nil)
        linkHref = [resolvedURL relativeString];

    // The NSMutableDictionary returned by dictionaryWithCapacity is autoreleased. We do not retain it here because this
    // function does not own it. We let the caller retain it if necessary.
    NSMutableDictionary *params = [NSMutableDictionary dictionaryWithCapacity:LINK_PARAMS_INITIAL_CAPACITY];
    [params setValue:linkHref forKey:LinkHref];
    [params setValue:[linkElement type] forKey:LinkType];
    [params setValue:[linkElement target] forKey:LinkTarget];
    [params setValue:[NSValue valueWithRect:linkBounds] forKey:LinkBounds];
    [params setValue:linkRects forKey:LinkRects];

    return params;
}

- (void)addLinkParams:(DOMHTMLAnchorElement *)linkElement withView:(NSView *)view visibleRect:(NSRect)visibleRect 
    toArray:(NSMutableArray *)array
{
    WebView *webView = (WebView *)[self contentView];

    // The WebView does not display the specified element if the style returned by computedStyleForElement is nil,
    // if the display attribute is "none", or the visibility attriute is "hidden".
    DOMCSSStyleDeclaration *style = [webView computedStyleForElement:linkElement pseudoElement:nil];
    if (style == nil || ![self isStyleDisplayed:style])
        return;

    // Compute the link's bounding box in the document view's coordinate system, and clip it by the document's
    // visible rectangle.
    NSRect linkRect = [linkElement boundingBox];
    NSRect linkVisibleRect = NSIntersectionRect([view convertRectToBase:linkRect], visibleRect);

    // Create an array to hold the link's rectangles. The array created by arrayWithCapacity is autoreleased, but is
    // retained and owned by linkParams.
    NSMutableArray *linkRects = [NSMutableArray arrayWithCapacity:LINK_RECTS_INITIAL_CAPACITY];
    // Add any visible images to the link's rectangles. We must collect the link's images before testing the link's
    // visibility, because some images exceed the link's bounding box.
    [self addImageRects:linkElement linkStyle:style linkRect:linkVisibleRect visibleRect:visibleRect withView:view
        toArray:linkRects];

    // Ignore the node if it is not in the document view's visible rectangle, and it has no visible images. Some
    // link images exceed the link's bounding box, so we include them as a test of the link's true bounds.
    if (NSIsEmptyRect(linkVisibleRect) && [linkRects count] == 0)
        return;

    if ([[linkElement lineBoxRects] count] == 0 || [@"block" caseInsensitiveCompare:[style display]] == NSOrderedSame)
    {
        // Add the link's bounding rectangle if its CSS display attribute is "block", or if the link does not have
        // line bounding rectangles. In this case, the link's bounding rectangle represents its entire pickable
        // area.
        [linkRects addObject:[NSValue valueWithRect:linkVisibleRect]];
    }
    else
    {
        // Add the bounding rectangles for each line of text if the link has lines of text with separate pickable
        // areas, and its CSS display attribute is not "block". In this case, each line of text is separately
        // pickable.
        [self addLineBoxRects:linkElement visibleRect:visibleRect withView:view toArray:linkRects];
    }

    // Ignore the link if it has no visible pickable areas.
    if ([linkRects count] == 0)
        return;

    // Compute the link's bounding rectangle from the list of pickable areas.
    NSRect linkBounds = NSMakeRect(0, 0, 0, 0);
    for (NSValue *value in linkRects)
    {
        linkBounds = NSUnionRect(linkBounds, [value rectValue]);
    }

    // The dictionary and created by createLinkParams is autoreleased, but is retained and owned by the array.
    NSDictionary *linkParams = [self createLinkParams:linkElement withBounds:linkBounds withRects:linkRects];
    [array addObject:linkParams];
}

- (void)addLinks:(NSMutableArray *)array
{
    // Get this window's WebView and its corresponding document view. We do not test if mainFrame or frameView are nil,
    // because sending a message to a nil object in Objective-C is valid and always returns nil.
    WebView *webView = (WebView *)[self contentView];
    NSView *docView = [[[webView mainFrame] frameView] documentView];

    // If the WebView's document view is nil, then we cannot compute each link's visibility and must exit without adding
    // any links.
    if (docView == nil)
        return;

    // Get the collection of link elements from the WebView's DOM document. Each link is an anchor element that has an
    // href attribute. If this collection is null we return without adding any links. We do not test if
    // mainFrameDocument is nil, because sending a message to a nil object in Objective-C is valid and always returns
    // nil.
    DOMHTMLCollection *linkElements = [[webView mainFrameDocument] links];
    if (linkElements == nil)
        return;

    // Compute the document's visible rectangle in the document view's coordinate system. If the document view has an
    // enclosing scroll view, the visible rectangle is limited to the scroll view's visible rectangle.
    NSScrollView *scrollView = [docView enclosingScrollView];
    NSRect visibleRect = (scrollView != nil) ? [docView convertRectToBase:[scrollView documentVisibleRect]]
        : [webView convertRectToBase:[webView bounds]];

    int i;
    for (i = 0; i < [linkElements length]; i++)
    {
        DOMHTMLAnchorElement *linkElement = (DOMHTMLAnchorElement *)[linkElements item:i];
        if (linkElement != nil) // This should never happen, but we check anyway.
        {
            [self addLinkParams:linkElement withView:docView visibleRect:visibleRect toArray:array];
        }
    }
}

//**************************************************************//
//********************  Public Interface  **********************//
//**************************************************************//

- (id)initWithFrameSize:(NSSize)frameSize
{
    // Determine the window's frame and its content rect. The frame is set to the specified size and placed at the
	// origin. Since the NSWindow is used offscreen its origin can be any value. We use the minimum allowable value:
	// -16000. The window resizes its content view to fit precisely in its content area. The content rect is a function
	// of the window's style and its frame.
	NSUInteger windowStyle = NSBorderlessWindowMask;
	NSRect windowFrame = NSMakeRect(MIN_WINDOW_POS, MIN_WINDOW_POS, frameSize.width, frameSize.height);
	NSRect contentRect = [NSWindow contentRectForFrameRect:windowFrame styleMask:windowStyle];

    // Initialize this window as an offscreen NSWindow. We configure the NSWindow for offscreen use as follows:
	// - Use a borderless window. The window has no title, no buttons, and no resize control.
	// - Use a buffered backing store. This backing store type can be copied to a bitmap after drawing.
	// - Disable deferred window creation. This is necessary to ensure the offscreen window is actually created.
	// - Disable the window's automatic display of view's marked as needing it; we display the window explicitly.
	// - Disable window dragging.
	// - Disable the window's shadow.
	// - Use the default preferred backing store location. The default provides optimal update performance in OS X 10.6.
	[super initWithContentRect:contentRect
	    styleMask:windowStyle
	    backing:NSBackingStoreBuffered
	    defer:NO];
	[self setFrame:windowFrame display:NO]; // Don't display the window's views yet; we draw the window explicitly.
	[self setAutodisplay:NO];
	[self setMovable:NO];
	[self setHasShadow:NO];

    // Initialize the lock used to synchronize access between the AppKit and EDT threads.
    edtLock = [[NSLock alloc] init];

    // Initialize the arrays that hold the link rectangle information.
    linksBuffer = [[NSMutableArray alloc] initWithCapacity:LINKS_INITIAL_CAPACITY];
    links = [[NSMutableArray alloc] initWithCapacity:LINKS_INITIAL_CAPACITY];

    // Initialize the array that holds which keyDown events have been consumed.
    consumedKeyDownEvents = calloc(USHRT_MAX, sizeof(char));

	return self;
}

- (void)initWebView
{
    NSUInteger windowStyle = [self styleMask];
    NSRect windowFrame = [self frame];
    NSRect contentRect = [NSWindow contentRectForFrameRect:windowFrame styleMask:windowStyle];

    // Create a WebKit WebView and set it as the offscreen window's content view. The window resizes the WebView to fit
    // precisely in its content area. We autorelease the WebView, because the window retains it as its contentView and
    // owns it hereafter. We configure the WebView for offscreen use as follows:
    // - Configure the WebView to close when this window closes. This ensures that the WebView unloads its content and
    //   cancels any pending requests when this window closes.
	// - Configure the WebView to update while offscreen, since it's used entirely offscreen.
	// - Configure the WebView to draw no background unless the content defines one. This ensures that KML balloon
	//   styles with a bgColor other than white display as exptected.
	// - Set the frame load delegate to notify this window when the WebView completes loading its content. This is used
	//   to determine when this window's content size must be updated.
	// - Set the policy delegate to download files that the WebView cannot display, and to suppress opening any new
	//   windows.
	// - Set the UI delegate to suppress popup menus and printing. Popup menus don't display correctly when the WebView
	//   is offscreen, and the app should be in control of printing decisions.
	// - Set the resource load delegate to resolve relative URLs in the WebView's HTML content to absolute URLs whenever
	//   possible, and otherwise strip the "applewebdata" prefix added by WebView to relative URLs.
	// - Autorelease the WebView. The superclass NSWindow takes ownership of the WebView when we configure it as the
	//   window's content view.
	WebView *webView = [[WebView alloc] initWithFrame:contentRect];
	[webView setShouldCloseWithWindow:YES];
	[webView setShouldUpdateWhileOffscreen:YES];
	[webView setDrawsBackground:NO];
	[webView setFrameLoadDelegate:self];
    [webView setPolicyDelegate:self];
	[webView setUIDelegate:self];
	[webView setResourceLoadDelegate:self];
	[webView autorelease];
	[self setContentView:webView];

    // Configure this window's download controller as the WebView's download delegate. The download controller displays
    // the appropriate Cocoa user interface elements that enable the user to determine where to save a downloaded file,
    // view the current progress, and cancel a current download. This window owns the download delegate and releases it
    // in dealloc.
    WebDownloadController *downloadController = [[WebDownloadController alloc] init];
	[webView setDownloadDelegate:downloadController];

    // Mark this window's WebView as initialized.
	webViewInitialized = YES;
}

- (void)dealloc
{
    WebView *webView = (WebView *)[self contentView];

    // Clear the WebView's frame load, policy, UI, and resource load delegates. This window acts as these delegates for
    // the WebView. Since this window is being deallocated, we must clear the WebView's reference to these delegates to
    // prevent an illegal access exception if a resource retrieval causes the WebView to send a message to one of its
    // delegates after this window is deallocated.
    if ([webView frameLoadDelegate] != nil)
        [webView setFrameLoadDelegate:nil];
    if ([webView policyDelegate] != nil)
        [webView setPolicyDelegate:nil];
    if ([webView UIDelegate] != nil)
        [webView setUIDelegate:nil];
    if ([webView resourceLoadDelegate] != nil)
        [webView setResourceLoadDelegate:nil];

    // Release and clear the webView's download delegate. This window retains its download delegate and must release it
    // upon deallocation. This also cancels any active downloads and closes any panels displayed by the download
    // delegate. We clear the WebView's reference to this delegate to prevent an illegal access exception if an
    // asynchronous download event causes the WebView to send a message to this delegate after the controller is
    // deallocated.
    if ([webView downloadDelegate] != nil)
    {
        [[webView downloadDelegate] release];
        [webView setDownloadDelegate:nil];
    }

    // Release and clear this window's properties. This window owns these properties and must release them upon
    // deallocation.
    if (htmlString != nil)
        [htmlString release];
    if (baseURL != nil)
        [baseURL release];
    if (resourceResolver != nil)
        [resourceResolver release];
    if (htmlStringHistoryItem != nil)
        [htmlStringHistoryItem release];
    if (edtLock != nil)
        [edtLock release];
    if (displayBuffer != nil)
        [displayBuffer release];
    if (links != nil)
        [links release];
    if (linksBuffer != nil)
        [linksBuffer release];
    if (contentURL != nil)
        [contentURL release];
    if (consumedKeyDownEvents != 0)
        free(consumedKeyDownEvents);
    if (propertyChangeListener != nil)
        [propertyChangeListener release];

    [super dealloc];
}

/*
    Overridden to always return YES. This is necessary to correctly render MacWebView when it's offscreen.
*/
- (BOOL)isVisible
{
    return YES;
}

/*
    Overridden to always return YES. This is necessary to send input events to MacWebView when it's offscreen.
*/
- (BOOL)isKeyWindow
{
    return YES;
}

- (void)setHTMLString:(NSString *)string
{
    // Releases and clears the previous baseURL and resource load delegate, if any. Since new content is loaded, the
    // previous baseURL and resource load delegate are no longer used and must be cleared. We use the default resource
    // resolver to remove the "applewebdata" prefix that WebView adds to relative URLs.
    [self loadHTMLString:string baseURL:nil resourceResolver:[WebResourceResolver defaultResourceResolver]];
}

- (void)setHTMLString:(NSString *)string baseURL:(NSURL *)url
{
    // Release and clear the previous resource load delegate, if any. Since new content is loaded, the previous delegate
    // is no longer used and must be cleared. We use the default resource resolver to remove the "applewebdata" prefix
    // that WebView adds to relative URLs, if the baseURL is nil or cannot resolve any relative URLs.
    [self loadHTMLString:string baseURL:url resourceResolver:[WebResourceResolver defaultResourceResolver]];
}

- (void)setHTMLString:(NSString *)string resourceResolver:(id)resolver
{
    // If the specified resolver is nil, we use the default resource resolver to remove the "applewebdata" prefix that
    // WebView adds to relative URLs.
    if (resolver == nil)
        resolver = [WebResourceResolver defaultResourceResolver];

    // Configures the WebView to use the resource resolver to resolve relative paths in the HTML content. Specifying a
    // nil base URL causes the WebView to use a default base URL of "applewebdata://ID/", where "ID" is a generated
    // unique ID. The resource resolver identifies resource URLs starting with this schema and host, and interprets them
    // as relative paths. This also releases and clear the previous baseURL, if any. Since new content is loaded, the
    // previous baseURL is longer used and must be cleared.
    [self loadHTMLString:string baseURL:nil resourceResolver:resolver];
}

- (void)loadHTMLString:(NSString *)string baseURL:(NSURL *)url resourceResolver:(id)resolver
{
    WebView *webView = (WebView *)[self contentView];

    if (htmlString != nil)
        [htmlString release];
    if (baseURL != nil)
        [baseURL release];
    if (resourceResolver != nil)
        [resourceResolver release];

    // Keep track of the HTML string and the base URL used as this window's content. We use these properties to reload
    // this content when the application uses the WebView's history to navigate back to the original text.
    htmlString = string;
    baseURL = url;
    resourceResolver = resolver;

    if (htmlString != nil)
        [htmlString retain];
    if (baseURL != nil)
        [baseURL retain];
    if (resourceResolver != nil)
        [resourceResolver retain];

    // Clear the WebView's history when the application loads a new HTML string is loaded, and add a single web history
    // item with the URL "about:blank". We clear the WebView history to ensure that the window's relative URL resolution
    // is consistent. WebView is designed to display one HTML string with one base URL or resource resolver, therefore
    // it interprets a new HTML string as a signal to clear the WebView's history and start a new history. We add a
    // web history item with to indicate when the application has navigated back to the original HTML string. This dummy
    // history item is used in goBack as a signal to reload the window's current HTML string. We must use "about:blank"
    // as the item's URL to ensure that the WebView can actually navigate back to that item. The actual content is
    // irrelevant, since it's always replaced with the HTML string.
    int capacity = [[webView backForwardList] capacity];
    [[webView backForwardList] setCapacity:0];
    [[webView backForwardList] setCapacity:capacity];

    if (htmlStringHistoryItem != nil)
        [htmlStringHistoryItem release];
    htmlStringHistoryItem = [[WebHistoryItem alloc] initWithURLString:WebHistoryHTMLStringURL
        title:WebHistoryHTMLStringTitle lastVisitedTimeInterval:[NSDate timeIntervalSinceReferenceDate]];
    [[webView backForwardList] addItem:htmlStringHistoryItem];

    // [WebFrame loadHTMLString] does not accept a null string, so we use the empty string if the application specified
    // HTML string is nil.
    if (string == nil)
        string = [NSString string];

	[[webView mainFrame] loadHTMLString:string baseURL:url];
}

- (void)reloadHTMLString
{
    // [WebFrame loadHTMLString] does not accept a null string, so we use the empty string if the application specified
    // HTML string is nil.
    NSString *string = htmlString;
    if (string == nil)
        string = [NSString string];

    WebView *webView = (WebView *)[self contentView];
    [[webView mainFrame] loadHTMLString:string baseURL:baseURL];
}

/*
    Attempts to resolves the specified relative URL, and returns either the resolved URL or nil if the URL cannot be
    resolved. Relative URLs are either resolved to an absolute URL or stripped of the "applewebdata" scheme added by the
    WebView.
 */
- (NSURL *)resolve:(NSURL *)url
{
    SEL selector = @selector(resolve:);

    if (resourceResolver != nil && [resourceResolver respondsToSelector:selector])
        return [resourceResolver performSelector:selector withObject:url];

    return nil;
}

- (NSSize)frameSize
{
    return [self frame].size;
}

- (void)setFrameSize:(NSSize)size
{
	// Set the window's frame to the specified size and place it at the origin. Since the NSWindow is used offscreen
	// its origin can be any value. We use the minimum allowable value: -16000. The window resizes its content view to
	// fit precisely in its content area. Don't display the window's views yet; we draw the window explicitly.
	[self setFrame:NSMakeRect(MIN_WINDOW_POS, MIN_WINDOW_POS, size.width, size.height) display:NO];
}

- (NSSize)contentSize
{
    NSSize size;

    [edtLock lock];
    @try
    {
        size = contentSize;
    }
    @finally
    {
        [edtLock unlock];
    }

    return size;
}

- (NSSize)minContentSize
{
    return minContentSize;
}

- (void)setMinContentSize:(NSSize)size
{
    if (NSEqualSizes(minContentSize, size))
        return;

    minContentSize = size;

    // Set this window's content update time and schedule the WebViewWindowController to call makeDisplay after a delay
    // of 10 milliseconds. This ensures that this window's content info is updated after its layout is computed, and
    // that this change to minContentSize is reflected in this window's contentSize..
    contentUpdateTime = [WebViewWindow currentTimeMillis];
    [[WebViewWindowController sharedInstance] displayWindowsAfterDelay:DISPLAY_WINDOWS_DELAY];
}

- (NSURL *)contentURL
{
    NSURL *url;

    [edtLock lock];
    @try
    {
        url = contentURL;
    }
    @finally
    {
        [edtLock unlock];
    }

    return url;
}

- (NSArray *)links
{
    return links;
}

- (void)goBack
{
    // If the WebView's history has a previous item, then navigate to that item. Otherwise ignore this message.
    WebView *webView = (WebView *)[self contentView];
    if ([webView goBack])
    {
        // If we've gone back to the application's HTML string, then we reload it. We detect navigation to this HTML
        // string by looking for the dummy history item added to the the history when the HTML string was specified.
        // This item has the URL "about:blank". We must use "about:blank" as the item's URL to ensure that the WebView
        // can actually navigate back to that item. The actual content of the item's URL is irrelevant, since it's
        // replaced with the HTML string. We load the HTML string ourselves because the application may have specified a
        // base URL or resource resolver. In order to continue using that base URL or resource resolver, we must reload
        // the content using [WebFrame loadHTMLString].
        WebBackForwardList *history = [webView backForwardList];
        if ([history currentItem] == htmlStringHistoryItem)
        {
            [self reloadHTMLString];
        }
    }
}

- (void)goForward
{
    // If the WebView's history has a next item, then navigate to that item. Otherwise ignore this message. Navigating
    // forward works normally, because we've handled any navigation back to the application's HTML string with a dummy
    // item in the WebView's history. Therefore navigating forward always loads a normal history item, and requires
    // no intervention.
    WebView *webView = (WebView *)[self contentView];
    [webView goForward];
}

/*
    Overridden to correctly handle the following event types for offscreen NSWindows: mouse down, mouse dragged,
    mouse up, scroll wheel dragging, and key events that map to actions (e.g. Command-C).
 */
- (void)sendEvent:(NSEvent *)event
{
    NSEventType type = [event type];

    if (activeScroller != nil)
    {
        [self trackScrollerKnob:event];
    }
    else if (type == NSLeftMouseDown || type == NSLeftMouseDragged || type == NSLeftMouseUp || type == NSScrollWheel)
    {
        NSPoint location = [event locationInWindow];
        NSView *hitView = [[self contentView] hitTest:location];
        if (hitView != nil)
        {
            if (type == NSLeftMouseDown)
            {
                if ([hitView acceptsFirstResponder])
                    [self makeFirstResponder:hitView];

                if ([hitView isKindOfClass:[NSScroller class]]
                    && [self mustTrackScrollerKnob:(NSScroller *)hitView withEvent:event])
                {
                    activeScroller = (NSScroller *)hitView;
                    [self trackScrollerKnob:event];
                }
                else
                {
                    [hitView mouseDown:event];
                }
            }
            else if (type == NSLeftMouseDragged)
                [hitView mouseDragged:event];

            else if (type == NSLeftMouseUp)
                [hitView mouseUp:event];

            else if (type == NSScrollWheel)
                [hitView scrollWheel:event];
        }
    }
    else if (type == NSKeyDown)
    {
        if (([event modifierFlags] & NSCommandKeyMask) != 0)
        {
            // NSApplication normally interprets special key events as actions and dispatches those actions to the
            // appropriate NSView. Since this NSWindow is offscreen, we must perform that interpretation and dispatch
            // the action ourselves. Attempting to send Command+AnyKey events to this NSWindow have no effect, and
            // eventually generate an NSInternalInconsistencyException. If the Command key is down we perform the same
            // actions that NSApplication normally would:
            // - Send a performKeyEquivalent: message to the first responder.
            // - Interpret the key command, and send the appropriate action to the first responder.
            if ([self firstResponder] != nil)
                [[self firstResponder] performKeyEquivalent:event];

            [self performActionForKeyEvent:event];
            consumedKeyDownEvents[[event keyCode]] = 1; // Mark the key as consumed to suppress the corresponding keyUp.
        }
        else
        {
            // If the Command key is not down, we send the event normally and clear the consumed state to avoid
            // suppressing the corresponding keyUp.
            [super sendEvent:event];
            consumedKeyDownEvents[[event keyCode]] = 0;
        }
    }
    else if (type == NSKeyUp)
    {
        // Send the event normally unless the corresponding keyDown event was consumed.
        if (consumedKeyDownEvents[[event keyCode]] == 0)
            [super sendEvent:event];

        // Clear the consumed event state for the next keyDown event.
        consumedKeyDownEvents[[event keyCode]] = 0;
    }
    else
    {
        [super sendEvent:event];
    }
}

- (void)makeDisplay
{
    [edtLock lock];
    @try
    {
        [self doMakeDisplay];
    }
    @finally
    {
        [edtLock unlock];
    }
}

- (void)doMakeDisplay
{
    BOOL mustFirePropertyChange = NO;

    // Reuse the current display util this window changes.
    if ([self mustRegenerateDisplayBuffer])
    {
        // Don't regenerate the display buffer if the corresponding texture is out of date. This avoids constantly
        // regenerating the display buffer if the window's owner never uses the display buffer, and avoids updating the
        // display faster than the window's owner can load the display into a texture. For example, if a Java object
        // using a WebView is offscreen or not visible and therefore is not updating its texture, this avoids consuming
        // unnecessary CPU cycles by updating a display buffer that may never be used. We exit this method entirely to
        // ensure that the code below that depends on the display state does not run. This window schedules a call to
        // makeDisplay when its texture is updated in displayInTexture.
        if ([self mustDisplayInTexture])
            return;

        // This window needs to be displayed and the texture is up-to-date with the previous display state. Display the
        // window in its backing buffer, and notify any property change listeners that the window has been updated.
        [self makeDisplayBuffer];
        mustFirePropertyChange = YES;
    }

    // Reuse the current content info until this window's content changes.
    if (contentInfoUpdateTime != contentUpdateTime)
    {
        // Update this window's current content info: the content size and the frame URL. These fields are accessed from
        // the EDT, so they are written here and read from their respective getter methods. This must be done after this
        // window's display is updated, because the content size depends on the layout, which is computed in
        // makeDisplayBuffer.
        [self makeContentInfo];
        contentInfoUpdateTime = contentUpdateTime;
        mustFirePropertyChange = YES;
    }

    if (mustFirePropertyChange)
        [self firePropertyChange];
}

/*
    Returns YES if this window's display buffer must be regenerated to synchronize with the window contents, and NO
    otherwise. This window's display buffer must be regenerated if any view in its hierarchy is marked as needing
    display or contains Core Animation layers. We regenerate the window constantly when Core Animation layers are
    present because the views associated with these layers are never marked as needing display.

    TODO: detect when the content of a Core Animation layer changes.
 */
- (BOOL)mustRegenerateDisplayBuffer
{
    return [self viewsNeedDisplay] || [self hasCoreAnimationLayers:[self contentView]];
}

/*
    Displays this window's content in its display buffer.
 */
- (void)makeDisplayBuffer
{
    NSView *view = [self contentView];
    NSRect bounds = [view bounds];

    // Release the current display buffer before creating a new one. This should never happen, but we check anyway.
    if (displayBuffer != nil)
    {
        [displayBuffer release];
        displayBuffer = nil;
    }

    // Create a new display buffer to hold this window's current display content. The bitmap returned from
    // bitmapImageRepForCachingDisplayInRect is autoReleased, so we take ownership of it by retaining it here and
    // releasing it in displayInTexture.
    displayBuffer = [view bitmapImageRepForCachingDisplayInRect:bounds];
    [displayBuffer retain];
    // Updates this window's backing buffer. This expects a rectangle in the view's coordinate system: We use the
    // view's bounds, which are expressed in its own coordinate system. We call displayRectIgnoringOpacity instead
    // of displayIfNeeded for two reasons:
    // - displayIfNeeded does not update views with Adobe Flash plug-in content.
    // - displayIfNeeded draws an opaque background fill behind the WebView; we want pixels not covered by the
    //   WebView to remain transparent.
    [view displayRectIgnoringOpacity:bounds];
    // Capture the WebView's backing buffer to an NSBitmapImageRep. This expects a rectangle in the view's
    // coordinate system: We use the view's bounds, which are expressed in its own coordinate system.
    [view cacheDisplayInRect:bounds toBitmapImageRep:displayBuffer];

    // Clear any previously computed link information, and add the current link information to the linksBuffer. This
    // buffer is copied to the links array in displayInTexture. Computing and copying the link information in
    // this way ensures that it is synchronized with the display buffer.
    [linksBuffer removeAllObjects];
    [self addLinks:linksBuffer];

    displayTime = [WebViewWindow currentTimeMillis];
}

- (BOOL)mustDisplayInTexture
{
    return displayTime > textureDisplayTime;
}

- (void)displayInTexture:(GLenum)target
{
    [edtLock lock];
    @try
    {
        if (displayBuffer == nil) // This should never happen, but we check anyway.
            return;

        // Load the display buffer to the currently bound GL texture, then release the display buffer we took ownership
        // of in makeDisplayBuffer. This buffer is allocated again during subsequent calls to makeDisplayBuffer.
        loadBitmapInGLTexture(target, displayBuffer);
        [displayBuffer release];
        displayBuffer = nil;

        // Copy the list of link rectangles computed during makeDisplayBuffer to an array accessed by the caller, then
        // clear the list of link rectangles to be computed during subsequent calls to makeDisplayBuffer.
        [links removeAllObjects];
        [links addObjectsFromArray:linksBuffer];
        [linksBuffer removeAllObjects];

        textureDisplayTime = [WebViewWindow currentTimeMillis];
    }
    @finally
    {
        [edtLock unlock];
    }

    // The texture is now up-to-date with this window's display, so the display can be regenerated if necessary.
    // Schedule a call to makeDisplay display to ensure that this window gets a chance to regenerate its display buffer.
    // See the inline documentation in doMakeDisplay for details.
    [[ThreadSupport sharedInstance] performBlockOnMainThread:
    ^{
        [[WebViewWindowController sharedInstance] displayWindowsAfterDelay:DISPLAY_WINDOWS_DELAY];
    }];
}

- (void)makeContentInfo
{
    [self determineContentSize];
    [self determineContentURL];
}

- (void)determineContentSize
{
    WebView *webView = (WebView *)[self contentView];
    NSView<WebDocumentView> *docView = [[[webView mainFrame] frameView] documentView];

    if (docView == nil)
    {
        // We cannot compute a content size if this window's document view is null. In this case we assign the content
        // size (0, 0) indicating that the size is unknown, then return immediately.
        contentSize = NSMakeSize(0, 0);
        return;
    }

    NSSize frameSize = [webView frame].size;
    NSSize defaultMinContentSize = NSMakeSize(DEFAULT_MIN_CONTENT_WIDTH, DEFAULT_MIN_CONTENT_HEIGHT);
    @try
    {
        // Set this window's size to the minimum content size, or the default minimum size if the specified min size
        // contains a dimension of zero. The document view's frame size is computed as the larger of the window's frame
        // size and the scrollable area. We resize the window to a small minimum size when computing the content size to
        // ensure that the content size actually fits the content. If the window size is too large, the computed content
        // size captures the frame size instead. We send the document view a layout message to ensure its current frame
        // size is accurate.
        [webView setFrameSize:(minContentSize.width != 0.0 && minContentSize.height != 0.0 ?
            minContentSize : defaultMinContentSize)];
        [docView layout];

        // Update the content size according to whether the WebView's document view is in an NSScrollView.
        if ([docView enclosingScrollView] != nil)
        {
            NSScrollView *scrollView = [docView enclosingScrollView];
            contentSize = [NSScrollView frameSizeForContentSize:[docView frame].size
                hasHorizontalScroller:[scrollView hasHorizontalScroller]
                hasVerticalScroller:[scrollView hasVerticalScroller]
                borderType:[scrollView borderType]];
        }
        else
        {
            contentSize = [docView frame].size;
        }
    }
    @finally
    {
        // Restore this window's frame size to avoid any side effects from changing the window's size while computing
        // the content size.
        [webView setFrameSize:frameSize];
        [docView layout];
    }
}

- (void)determineContentURL
{
    WebView *webView = (WebView *)[self contentView];
    WebBackForwardList *history = [webView backForwardList];

    // Release and clear the current content URL.
    if (contentURL != nil)
    {
        [contentURL release];
        contentURL = nil;
    }

    // Update the current content URL with the current history item's URL, or null if the current history item is the
    // application's HTML string.
    if ([history currentItem] != htmlStringHistoryItem)
    {
        NSString *urlString = [[history currentItem] URLString];
        contentURL = [[NSURL alloc] initWithString:urlString];
    }
}

- (void)setPropertyChangeListener:(id)listener
{
    if (propertyChangeListener == listener)
        return;

    // Release the previous property change listener, if any. This window retains its listener and releases it here.
    if (propertyChangeListener != nil)
        [propertyChangeListener release];

    propertyChangeListener = listener;

    // WebViewWindow takes ownership of its property change listener, if any. We retain it here and release it either
    // here when another listener is specified, otherwise we release it in dealloc.
    if (propertyChangeListener != nil)
        [propertyChangeListener retain];
}

- (void)firePropertyChange
{
    SEL selector = @selector(propertyChange);

    if (propertyChangeListener != nil && [propertyChangeListener respondsToSelector:selector] == YES)
    {
        [propertyChangeListener performSelector:selector];
    }
}

//**************************************************************//
//********************  WebFrameLoadDelegate  ******************//
//**************************************************************//

- (void)onFrameLoaded
{
    // Set this window's content update time and schedule the WebViewWindowController to call makeDisplay after a delay
    // of 10 milliseconds. This ensures that this window's content info is updated after its layout is computed.
    contentUpdateTime = [WebViewWindow currentTimeMillis];
    [[WebViewWindowController sharedInstance] displayWindowsAfterDelay:DISPLAY_WINDOWS_DELAY];
}

- (void)webView:(WebView *)sender didFinishLoadForFrame:(WebFrame *)frame
{
    [self onFrameLoaded];
}

- (void)webView:(WebView *)sender didFailLoadWithError:(NSError *)error forFrame:(WebFrame *)frame
{
    [self onFrameLoaded];
}

- (void)webView:(WebView *)sender didFailProvisionalLoadWithError:(NSError *)error forFrame:(WebFrame *)frame
{
    [self onFrameLoaded];
}

//**************************************************************//
//********************  WebPolicyDelegate  *********************//
//**************************************************************//

/*
    Called when the mime type of a navigation event is known, in order to determine the appropriate action for that mime
    type. This performs the default navigation action if the URL is a file URL, or if the WebView can display the
    content type. Otherwise this downloads the contents of the URL.
 */
- (void)webView:(WebView *)webView decidePolicyForMIMEType:(NSString *)type request:(NSURLRequest *)request
    frame:(WebFrame *)frame decisionListener:(id < WebPolicyDecisionListener >)listener
{
    if ([[request URL] isFileURL])
    {
        BOOL isDirectory = NO;
        BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:[[request URL] path] isDirectory:&isDirectory];

        if (exists && !isDirectory && [WebView canShowMIMEType:type])
            [listener use];
        else
            [listener ignore];
    }
    else if ([WebView canShowMIMEType:type])
    {
        [listener use];
    }
    else
    {
        [listener download];
    }
}

/*
    Called when a navigation event has a target that instructs the browser to open a new window. Ignores the navigation
    event to suppress the WebView from opening a new window. The application must be in control of which WebView windows
    are created and displayed. Applications can open new windows by catching link clicked events with a new window
    target, and displaying the link's URL in a new window or the system browser.
 */
- (void)webView:(WebView *)webView decidePolicyForNewWindowAction:(NSDictionary *)actionInformation
    request:(NSURLRequest *)request newFrameName:(NSString *)frameName
    decisionListener:(id < WebPolicyDecisionListener >)listener
{
    [listener ignore];
}

//**************************************************************//
//********************  WebUIDelegate  *************************//
//**************************************************************//

/*
    Called when the user right-clicks or control-clicks an element to display the context menu associated with the
    element. Returns nil to suppress the context menu from appearing in the WebView. A return value of nil is
    interpreted as a context menu with no elements.
 */
- (NSArray *)webView:(WebView *)sender contextMenuItemsForElement:(NSDictionary *)element
    defaultMenuItems:(NSArray *)defaultMenuItems
{
    return nil;
}

/*
    Called when JavaScript code executes the function window.showModalDialog. Returns nil to suppress a script from
    opening a modal dialog.
 */
- (WebView *)webView:(WebView *)sender createWebViewModalDialogWithRequest:(NSURLRequest *)request
{
    return nil;
}

/*
    Called when JavaScript code executes the function window.open. Returns nil to suppress the WebView from opening
    a new window. The application must be in control of which WebView windows are created and displayed. Applications
    can open new windows by catching link clicked events with a new window target, and displaying the link's URL in a
    new window or the system browser.
 */
- (WebView *)webView:(WebView *)sender createWebViewWithRequest:(NSURLRequest *)request
{
    return nil;
}

/*
    Called when a user or a script wants to print all or part of the WebView. Does nothing to suppress a script from
    printing the WebView's content.
 */
- (void)webView:(WebView *)sender printFrameView:(WebFrameView *)frameView
{
}


//**************************************************************//
//********************  WebResourceLoadDelegate  ***************//
//**************************************************************//

- (NSURLRequest *)webView:(WebView *)sender resource:(id)identifier willSendRequest:(NSURLRequest *)request
    redirectResponse:(NSURLResponse *)redirectResponse fromDataSource:(WebDataSource *)dataSource
{
    // Attempt to resolve requests for relative URLs. Relative URLs are either resolved to an absolute URL or stripped
    // of the "applewebdata" scheme added by the WebView. If the URL is relative and can be resolved, we return a new
    // NSURLRequest with the resolved URL, but with the the same cache policy and timeout interval as the original
    // request. The NSURL returned by resolve is autoreleased; we let the current autorelease pool reclaim it. The
    // NSURLRequest returned by requestWithURL is autoreleased. We do not retain it here because we do not own it. The
    // caller retains it if necessary.

    NSURL *resolvedURL = [self resolve:[request URL]];
    if (resolvedURL != nil)
    {
        return [NSURLRequest requestWithURL:resolvedURL cachePolicy:[request cachePolicy]
            timeoutInterval:[request timeoutInterval]];
    }

    return request;
}

@end
