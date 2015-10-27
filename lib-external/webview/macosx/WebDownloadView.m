/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "WebDownloadView.h"

/*
    Version $Id: WebDownloadView.m 1171 2013-02-11 21:45:02Z dcollins $
 */
@implementation WebDownloadView

/* The initial width for a download view. */
static const NSUInteger DOWNLOAD_VIEW_WIDTH = 487;
/* The initial height for a download view. */
static const NSUInteger DOWNLOAD_VIEW_HEIGHT = 106;
/* The floating point fraction that converts a number in bytes to a number in megabytes. */
static const double BYTES_TO_MEGABYTES = 1.0 / 1048576.0;
/* The time interval between progress updates: every 250 milliseconds. */
static const NSTimeInterval PROGRESS_UPDATE_INTERVAL = 0.25;

/* The title string for the cancel button. */
static NSString *CancelButtonTitle = @"Cancel";
/* The path for the default filename image. */
static NSString *DefaultFilenameImagePath = @"/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/GenericDocumentIcon.icns";
/* The format string for a progress text. */
static NSString *ProgressTextFormat = @"%.1f of %.1f MB";
/* The format string for progress text of an indeterminate download length. */
static NSString *ProgressTextIndeterminateFormat = @"%.1f MB (Unknown size)";

- (void)cancelButtonPushed
{
    if (cancelDelegate == nil)
        return;

    SEL selector = @selector(downloadCancelRequested:);
    if ([cancelDelegate respondsToSelector:selector])
    {
        [cancelDelegate performSelector:selector withObject:webDownload];
    }
}

- (id)initWithDownload:(NSURLDownload *)download;
{
    [super initWithFrame:NSMakeRect(0, 0, DOWNLOAD_VIEW_WIDTH, DOWNLOAD_VIEW_HEIGHT)];

    webDownload = download;
    [webDownload retain];

    filenameImage = [[NSImageView alloc] initWithFrame:NSMakeRect(20, 29, 48, 48)];
    filenameLabel = [[NSTextField alloc] initWithFrame:NSMakeRect(76, 69, 299, 17)];
    progressIndicator = [[NSProgressIndicator alloc] initWithFrame:NSMakeRect(76, 45, 299, 16)];
    progressLabel = [[NSTextField alloc] initWithFrame:NSMakeRect(76, 20, 299, 17)];
    NSButton *cancelButton = [[NSButton alloc] initWithFrame:NSMakeRect(383, 41, 84, 24)];
    [cancelButton autorelease]; // Retained by the superclass NSView.

    // Configure the user interface elements to correctly resize within their parent view.
    [filenameImage setAutoresizingMask:NSViewMaxXMargin|NSViewMinYMargin];
    [filenameLabel setAutoresizingMask:NSViewWidthSizable|NSViewMinYMargin];
    [progressIndicator setAutoresizingMask:NSViewWidthSizable|NSViewMinYMargin];
    [progressLabel setAutoresizingMask:NSViewWidthSizable|NSViewMinYMargin];
    [cancelButton setAutoresizingMask:NSViewMinXMargin|NSViewMinYMargin];

    // Configure the text fields to display as simple non-editable and non-bordered labels.
    [filenameLabel setEditable:NO];
    [filenameLabel setDrawsBackground:NO];
    [filenameLabel setBezeled:NO];
    [progressLabel setEditable:NO];
    [progressLabel setDrawsBackground:NO];
    [progressLabel setBezeled:NO];

    // Configure the progress indicator to display in indeterminate mode by default, and animate the indeterminate
    // progress bar.
    [progressIndicator setIndeterminate:YES];
    [progressIndicator startAnimation:self];

    // Configure the cancel button to send the message cancelButtonPushed to this WebDownloadView when the button is
    // pushed.
    [cancelButton setButtonType:NSMomentaryPushInButton];
    [cancelButton setBezelStyle:NSRoundedBezelStyle];
    [cancelButton setTitle:CancelButtonTitle];
    [cancelButton setAction:@selector(cancelButtonPushed)];
    [cancelButton setTarget:self];

    [self addSubview:filenameImage];
    [self addSubview:filenameLabel];
    [self addSubview:progressIndicator];
    [self addSubview:progressLabel];
    [self addSubview:cancelButton];

    return self;
}

- (void)dealloc
{
    [webDownload release];
    [filenameImage release];
    [filenameLabel release];
    [progressIndicator release];
    [progressLabel release];

    [super dealloc];
}

- (NSURLDownload *)download
{
    return webDownload;
}

- (id)cancelDelegate
{
    return cancelDelegate;
}

- (void)setCancelDelegate:(id)delegate
{
    cancelDelegate = delegate;
}

- (void)didReceiveResponse:(NSURLResponse *)response
{
    haveDestination = NO;
    progressUpdateTime = 0.0;
    expectedLength = [response expectedContentLength];
    bytesReceived = 0;

    [filenameImage setImage:nil];
    [filenameLabel setStringValue:[NSString string]];

    if (expectedLength == NSURLResponseUnknownLength)
    {
        [progressIndicator setIndeterminate:YES];
        [progressIndicator startAnimation:self]; // Animate the indeterminate progress bar.
        [progressLabel setStringValue:[NSString string]];
    }
    else
    {
        [progressIndicator stopAnimation:self]; // Stop any indeterminate progress animation.
        [progressIndicator setIndeterminate:NO];
        [progressIndicator setMinValue:0];
        [progressIndicator setMaxValue:expectedLength];
        [progressIndicator setDoubleValue:bytesReceived];
        [progressLabel setStringValue:[NSString string]];
    }
}

- (void)didReceiveDataOfLength:(NSUInteger)length
{
    bytesReceived += length;

    if (!haveDestination)
        return;

    NSTimeInterval time = [NSDate timeIntervalSinceReferenceDate];
    if (time - progressUpdateTime < PROGRESS_UPDATE_INTERVAL)
        return;

    if (expectedLength == NSURLResponseUnknownLength)
    {
        // The progress is indeterminate; just show the number of bytes received.
        [progressLabel setStringValue:[NSString stringWithFormat:ProgressTextIndeterminateFormat, (double) bytesReceived * BYTES_TO_MEGABYTES]];
    }
    else
    {
        [progressIndicator setDoubleValue:bytesReceived];
        [progressLabel setStringValue:[NSString stringWithFormat:ProgressTextFormat, (double) bytesReceived * BYTES_TO_MEGABYTES, (double) expectedLength * BYTES_TO_MEGABYTES]];
    }

    progressUpdateTime = time;
}

- (void)didCreateDestination:(NSString *)path
{
    NSString *filename = [path lastPathComponent];
    if (filename == nil)
        filename = [NSString string]; // Default to the empty string.
    [filenameLabel setStringValue:filename];

    NSImage *image = nil;
    if ([filename pathExtension] != nil)
        image = [[NSWorkspace sharedWorkspace] iconForFileType:[filename pathExtension]];
    if (image == nil)
        image = [[NSWorkspace sharedWorkspace] iconForFile:DefaultFilenameImagePath]; // Default to the generic document icon.
    // NSWorkspace iconForFileType returns a 32x32 image; we make it 48x48 to fit in the space provided.
    if (image != nil)
        [image setSize:NSMakeSize(48, 48)];
    [filenameImage setImage:image];

    haveDestination = YES;
}

@end
