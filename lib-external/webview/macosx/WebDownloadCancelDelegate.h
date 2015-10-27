#ifndef WEB_DOWNLOAD_CANCEL_DELEGATE_H
#define WEB_DOWNLOAD_CANCEL_DELEGATE_H

/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import <Cocoa/Cocoa.h>

/*
    Version $Id: WebDownloadCancelDelegate.h 1171 2013-02-11 21:45:02Z dcollins $
 */
@protocol WebDownloadCancelDelegate

- (void)downloadCancelRequested:(NSURLDownload *)download

@end


#endif /* WEB_DOWNLOAD_CANCEL_DELEGATE_H */
