#ifndef WEB_DOWNLOAD_VIEW_H
#define WEB_DOWNLOAD_VIEW_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import <Cocoa/Cocoa.h>

/*
    Version $Id: WebDownloadView.h 1171 2013-02-11 21:45:02Z dcollins $
 */
@interface WebDownloadView : NSView
{
@protected
    NSURLDownload *webDownload;
    id cancelDelegate;
    // User interface properties.
    NSImageView *filenameImage;
    NSTextField *filenameLabel;
    NSProgressIndicator *progressIndicator;
    NSTextField *progressLabel;
    // Download progress properties.
    NSTimeInterval progressUpdateTime;
    BOOL haveDestination;
    long long expectedLength;
    long long bytesReceived;
}

- (id)initWithDownload:(NSURLDownload *)download;

- (id)cancelDelegate;

- (void)setCancelDelegate:(id)delegate;

- (id)initWithDownload:(NSURLDownload *)download;

- (NSURLDownload *)download;

- (void)didReceiveResponse:(NSURLResponse *)response;

- (void)didReceiveDataOfLength:(NSUInteger)length;

- (void)didCreateDestination:(NSString *)path;


@end


#endif /* WEB_DOWNLOAD_VIEW_H */
