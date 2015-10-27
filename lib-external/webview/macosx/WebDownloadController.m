/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "WebDownloadController.h"
#import "WebDownloadView.h"

/*
    Version $Id: WebDownloadController.m 1171 2013-02-11 21:45:02Z dcollins $
 */
@implementation WebDownloadController

/* The initial capacity of the download views array. */
static const NSUInteger DOWNLOAD_VIEWS_INITIAL_CAPACITY = 2;
/* The initial width for the download window. */
static const NSUInteger DOWNLOAD_WINDOW_WIDTH = 487;
/* The initial height for the download window. */
static const NSUInteger DOWNLOAD_WINDOW_HEIGHT = 400;
/* The minimum width for the download window. */
static const NSUInteger DOWNLOAD_WINDOW_MIN_WIDTH = 487;
/* The minimum height for the download window. */
static const NSUInteger DOWNLOAD_WINDOW_MIN_HEIGHT = 106;

/* The title string for the download window. */
static NSString *DownloadWindowTitle = @"Downloads";

- (id)init
{
    [super init];

    downloadViews = [[NSMutableArray alloc] initWithCapacity:DOWNLOAD_VIEWS_INITIAL_CAPACITY];

    return self;
}

- (void)dealloc
{
    // Close the download window. This automatically sends a release message to the window, its content view, and the
    // views in its hierarchy.
    [downloadWindow close];

    // Cancel any active downloads and release the downloadViews array. This also sends a release message to any view
    // currently in the array.
    for (WebDownloadView *view in downloadViews)
    {
        [[view download] cancel];
    }
    [downloadViews release];

    [super dealloc];
}

- (void)beginDownload:(NSURLDownload *)download
{
    NSView *view = [self createDownloadView:download];
    if (view == nil)
        return;

    // Lazily create and initialize the download window. The NSWindow returned by createDownloadWindow is autoreleased,
    // we take ownership of it here and release it in dealloc or in endDownload.
    if (downloadWindow == nil)
    {
        downloadWindow = [self createDownloadWindow];
        [downloadWindow retain];
    }

    // Add the view to the end of the downloadViews array and as a subview of the window's document view. Both the
    // downloadViews array and the window retain the view.
    NSView *docView = [(NSScrollView *)[downloadWindow contentView] documentView];
    [downloadViews addObject:view];
    [docView addSubview:view];

    // Update the layout of the window's content view, and scroll to make the new frame is visible.
    [(CollectionView *)docView layout];
    [docView scrollRectToVisible:[view frame]];

    // If the download window is not at least partially visible on screen, make it the key window and bring it to the
    // front.
	if (![downloadWindow isVisible])
    {
        [downloadWindow makeKeyAndOrderFront:self];
    }
}

- (void)endDownload:(NSURLDownload *)download
{
    NSView *view = [self getDownloadView:download];
    if (view == nil)
        return;

    // Remove the view from the downloadViews array and the window's document view. Both the downloadViews array and the
    // window release the view.
    [downloadViews removeObject:view];
    [view removeFromSuperview];

    // Update the layout of the window's document view.
    NSView *docView = [(NSScrollView *)[downloadWindow contentView] documentView];
    [(CollectionView *)docView layout];

    // Close the download window if there are no active downloads. This automatically sends a release message to the
    // window, its content view, and the views in its hierarchy.
    if ([downloadViews count] == 0)
    {
        [downloadWindow close];
        downloadWindow = nil;
    }
}

- (NSView *)getDownloadView:(NSURLDownload *)download
{
    WebDownloadView *view = nil;

    for (WebDownloadView *o in downloadViews)
    {
        if ([o download] == download)
        {
            view = o;
            break;
        }
    }

    return view;
}

- (NSView *)createDownloadView:(NSURLDownload *)download
{
    WebDownloadView *view = [[WebDownloadView alloc] initWithDownload:download];
    [view setCancelDelegate:self];
    [view setAutoresizingMask:NSViewMaxXMargin|NSViewWidthSizable];
    [view autorelease]; // We autorelease the view because this method does not own it; it must be retained by the caller.

	return view;
}

- (NSWindow *)createDownloadWindow
{
    NSRect frameRect;

    if ([NSApp mainWindow] != nil)
    {
        // Place the download window horizontally so there is a 20 pixel gap between the left side of the main window
        // and the right side of the download window, and vertically such that the distance from the bottom of the main
        // window to the bottom of the download window is twice the distance from the top of the main window to the top
        // of the download window. See the Apple's Human Interface Guidelines for details:
        // http://developer.apple.com/library/mac/#documentation/UserExperience/Conceptual/AppleHIGuidelines/XHIGWindows/XHIGWindows.html
        NSRect windowRect = [[NSApp mainWindow] frame];
        frameRect = NSMakeRect(
            NSMinX(windowRect) - DOWNLOAD_WINDOW_WIDTH - 20,
            NSMinY(windowRect) + 2.0/3.0 *(NSHeight(windowRect) - DOWNLOAD_WINDOW_HEIGHT),
            DOWNLOAD_WINDOW_WIDTH, DOWNLOAD_WINDOW_HEIGHT);
    }
    else
    {
        // If the application has no main window, place the download window centered horizontally on screen, and
        // vertically positioned such that the distance from the top of the dock to the bottom of the window is twice
        // the distance from the bottom of the menu to the top of the window. See the Apple's Human Interface Guidelines
        // for details:
        // http://developer.apple.com/library/mac/#documentation/UserExperience/Conceptual/AppleHIGuidelines/XHIGWindows/XHIGWindows.html
        NSRect screenRect = [[NSScreen mainScreen] visibleFrame];
        frameRect = NSMakeRect(
            NSMidX(screenRect) - DOWNLOAD_WINDOW_WIDTH / 2.0,
            NSMinY(screenRect) + 2.0/3.0 * (NSHeight(screenRect) - DOWNLOAD_WINDOW_HEIGHT),
            DOWNLOAD_WINDOW_WIDTH, DOWNLOAD_WINDOW_HEIGHT);
    }

    // Configure the downloads window as an NSPanel that displays the download user interface elements as follows:
    // - Configure the window so the user cannot close it, since there is currently no way to re-open the window.
    // - Set the backing store type to NSBackingStoreBuffered. This is the only type recommended for use by the
    //   NSWindow documentation.
    // - Set the window to defer creation of any window resources until it is actually displayed on screen.
    // - Configure the window with a minimum size that keeps at least one download visible.
    // - Configure the window to release itself when this controller closes it.
    // - Prevent the window from hiding when the application is deactivated. Download windows should always be visible.
    // - Give the window the title "Downloads".
	NSUInteger styleMask = NSTitledWindowMask|NSMiniaturizableWindowMask|NSResizableWindowMask;
    NSRect contentRect = [NSWindow contentRectForFrameRect:frameRect styleMask:styleMask];
	NSWindow *window = [[NSPanel alloc] initWithContentRect:contentRect
	    styleMask:styleMask
	    backing:NSBackingStoreBuffered
	    defer:YES];
    [window setContentMinSize:NSMakeSize(DOWNLOAD_WINDOW_MIN_WIDTH, DOWNLOAD_WINDOW_MIN_HEIGHT)];
	[window setReleasedWhenClosed:YES];
    [window setHidesOnDeactivate:NO];
    [window setTitle:DownloadWindowTitle];
    [window autorelease]; // We autorelease the window because this method does not own it; it must be retained by the caller.

    // Create a scroll view that holds a table of download views as follows:
    // - Fills the entire download window.
    // - Resizes with the window.
    // - Displays a vertical scroll bar, and automatically hides it when it is not needed.
    // - Does not display a border.
    NSScrollView *scrollView = [[NSScrollView alloc] initWithFrame:[[window contentView] frame]];
    [scrollView setAutoresizingMask:NSViewWidthSizable|NSViewHeightSizable];
    [scrollView setHasVerticalScroller:YES];
    [scrollView setAutohidesScrollers:YES];
    [scrollView setBorderType:NSNoBorder];
    [scrollView autorelease]; // We autorelease the view because this method does not own it; it is retained by the window.
    [window setContentView:scrollView];

    // Give the scroll view a non-null empty document view.
    NSView *documentView = [[CollectionView alloc] initWithFrame:NSMakeRect(0, 0, [scrollView contentSize].width, 0)];
    [documentView setAutoresizingMask:NSViewWidthSizable];
    [scrollView setDocumentView:documentView];

    return window;
}

//**************************************************************//
//********************  WebDownload Delegate  ******************//
//**************************************************************//

- (void)didPresentErrorWithRecovery:(BOOL)didRecover contextInfo:(void *)contextInfo
{
    [self endDownload:(NSURLDownload *)contextInfo];
}

/*
    Called once when the WebView begins downloading a file.
 */
- (void)downloadDidBegin:(NSURLDownload *)download
{
    [self beginDownload:download];
}

/*
    Called once when the WebView has successfully downloaded a file and has written its contents to disk. This is not
    called if the download failed (in which case download:didFailWithError is called) or if the download is canceled.
 */
- (void)downloadDidFinish:(NSURLDownload *)download
{
    [self endDownload:download];
}

/*
    Called once when the WebView encounters an error attempting to download a file or save it to disk. Any partially
    downloaded file is deleted. This is not called if the download succeeds (in which case downloadDidFinish is called)
    or if the download is canceled.
 */
- (void)download:(NSURLDownload *)download didFailWithError:(NSError *)error
{
    [NSApp presentError:error modalForWindow:downloadWindow
        delegate:self didPresentSelector:@selector(didPresentErrorWithRecovery:contextInfo:) contextInfo:download];
}

/*
    Called once when enough information is available to determine a suggested filename for the downloaded file. This is
    not called if the download is canceled.
 */
- (void)download:(NSURLDownload *)download decideDestinationWithSuggestedFilename:(NSString *)filename
{
    // Creates a standard Mac OS X save panel initialized with the default values. The panel returned by savePanel is
    // autoreleased. We do not retain it here because we do not own it; we let the current autorelease pool reclaim it.
    NSSavePanel *savePanel = [NSSavePanel savePanel];

    // If this is the first time we've displayed the save panel, set the current directory URL to the user's downloads
    // directory.
    static BOOL didSetDefaultDirectory = NO;
    if (!didSetDefaultDirectory)
    {
        NSArray *urls = [[NSFileManager defaultManager] URLsForDirectory:NSDownloadsDirectory
            inDomains:NSUserDomainMask];
        if (urls != nil && [urls count] >= 1)
        {
            [savePanel setDirectoryURL:[urls objectAtIndex:0]];
        }

        didSetDefaultDirectory = YES;
    }

    // If the suggested filename is not nil, use it as the save panel's default filename. Otherwise the save panel
    // displays with an empty filename and requires the user to enter a name.
    if (filename != nil)
    {
        [savePanel setNameFieldStringValue:filename];
    }

    NSInteger result = [savePanel runModal];
    if (result == NSFileHandlingPanelOKButton)
    {
        // The user has chosen to download the file. We specify the download file's location on disk according to the
        // user's selected save location.
        [download setDestination:[[savePanel URL] path] allowOverwrite:YES];
    }
    else // NSFileHandlingPanelCancelButton
    {
        // The user has chosen to cancel the download while choosing a file location. We cancel the download and clean
        // up the WebDownloadView we allocated to display its progress.
        [download cancel];
        [self endDownload:download];
    }
}

- (void)download:(NSURLDownload *)download didReceiveResponse:(NSURLResponse *)response
{
    WebDownloadView *view = (WebDownloadView *)[self getDownloadView:download];
    if (view != nil)
    {
        [view didReceiveResponse:response];
    }
}

- (void)download:(NSURLDownload *)download didReceiveDataOfLength:(NSUInteger)length
{
    WebDownloadView *view = (WebDownloadView *)[self getDownloadView:download];
    if (view != nil)
    {
        [view didReceiveDataOfLength:length];
    }
}

/*
    Called once when the download destination file is created on disk.
 */
- (void)download:(NSURLDownload *)download didCreateDestination:(NSString *)path
{
    WebDownloadView *view = (WebDownloadView *)[self getDownloadView:download];
    if (view != nil)
    {
        [view didCreateDestination:path];
    }
}

//**************************************************************//
//********************  WebDownloadCancelDelegate  *************//
//**************************************************************//

/*
    Called when the user requests to cancel a download by clicking the cancel button in a WebDownloadView.
 */
- (void)downloadCancelRequested:(NSURLDownload *)download
{
    [download cancel];
    [self endDownload:download];
}

@end

//**************************************************************//
//********************  CollectionView  ************************//
//**************************************************************//

@implementation CollectionView

- (BOOL)isFlipped
{
    return YES;
}

- (void)layout
{
    // Set this view's frame size to fit its subviews.
    CGFloat height = 0.0;
    for (NSView *subview in [self subviews])
    {
        height += NSHeight([subview frame]);
    }

    [self setFrameSize:NSMakeSize(NSWidth([self frame]), height)];

    // Set each subview's frame origin so that they appear vertically stacked with the first view at the top and the last view on the bottom.
    CGFloat y = 0.0;
    for (NSView *subview in [self subviews])
    {
        [subview setFrameOrigin:NSMakePoint(0, y)];
        y += NSHeight([subview frame]);
    }
}

@end
