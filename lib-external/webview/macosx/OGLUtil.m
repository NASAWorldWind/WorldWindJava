/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
#import "OGLUtil.h"

/*
 * Version $Id: OGLUtil.m 1171 2013-02-11 21:45:02Z dcollins $
 */

/*
    Loads the specified bitmap as the source of the currently bound OpenGL texture. If the bitmap is in any format other
    than tightly-packed RGB or RGBA, this creates a copy of the bitmap in a tightly-packed RGBA format ant loads the
    copy. The target parameter specifies OpenGL texture target to use when loading the texture data, and must be one of
    the following: GL_TEXTURE_2D, GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
    GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL_TEXTURE_CUBE_MAP_POSITIVE_Z, or
    GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
*/
void loadBitmapInGLTexture(GLenum target, NSBitmapImageRep *bitmap)
{
    // If the bitmap is formatted as sequential RGB or RGBA tuples, we load directly from the bitmap. Otherwise, we copy
    // it to a new bitmap with the desired format and load the contents of the copy.
    if ([bitmap isPlanar] == YES || ([bitmap samplesPerPixel] != 3 && [bitmap samplesPerPixel] != 4))
    {
        // Create a 32-bit RGBA image to copy the bitmap into. We autorelease this bitmap because this function does not
        // own it. We let the autorelease pool release it after this function exits.
        NSBitmapImageRep *copy = [[NSBitmapImageRep alloc] initWithBitmapDataPlanes:NULL
            pixelsWide:[bitmap pixelsWide]
            pixelsHigh:[bitmap pixelsHigh]
            bitsPerSample:8 // 8 bits per single component.
            samplesPerPixel:4 // 4 samples per pixel (RGBA).
            hasAlpha:YES // One of 4 samples is alpha.
            isPlanar:NO // Component values are interwoven in a single channel (sequential RGBA tuples).
            colorSpaceName:NSDeviceRGBColorSpace // Use the device's RGB color space of the device.
            bitmapFormat:0 // Alpha values are the last component, alpha values are premultiplied, and samples are integer values.
            bytesPerRow:(32 * [bitmap pixelsWide]) // Don't pad each row to a word or double-word boundary.
            bitsPerPixel:32]; // Bitmap is a meshed configuration, so the bits per pixel equals bps*spp.
        [copy autorelease];

        [NSGraphicsContext saveGraphicsState];
        @try
        {
            // Configure the current context to draw into the allocated bitmap. The NSGraphicsContext context by
            // graphicsContextWithBitmapImageRep is autoreleased, but we do not retain it because this function does not
            // own it. We let the autorelease pool release it after this function exits.
            [NSGraphicsContext setCurrentContext:[NSGraphicsContext graphicsContextWithBitmapImageRep:copy]];
            // Draw the bitmap into the current graphics context, completely replacing the destination pixels with
            // pixels from the bitmap. We autorelease the NSImage because this function does not own it. We let the
            // autorelease pool release it after this function exits.
            NSRect rect = NSMakeRect(0, 0, [bitmap pixelsWide], [bitmap pixelsHigh]);
            NSImage *image = [[NSImage alloc] initWithSize:[bitmap size]];
            [image addRepresentation:bitmap];
            [image drawInRect:rect fromRect:rect operation:NSCompositeCopy fraction:1.0];
            [image autorelease];
            // The copy now contains the same representation as the original bitmap, except in RGB or RGBA format.
            // Assign the bitmap to the copy and drop our reference to the original bitmap.
            bitmap = copy;
        }
        @finally
        {
            [NSGraphicsContext restoreGraphicsState];
        }
    }

    @try
    {
        // Configure the pixel unpack row length and byte alignment to load from the bitmap. The the row length is
        // necessary to load RGBA bitmaps with a row length greater than the number of bytes per row. The unpack
        // alignment is necessary to load RGB bitmaps which don't have a 4 byte alignment. For details, see the
        // following Apple documentation:
        // http://developer.apple.com/library/mac/#qa/qa2001/qa1325.html

        // Adjust the row length to account for the number of pixels in each row, which may be different than
        // the bitmap width (in pixels).
        glPixelStorei(GL_UNPACK_ROW_LENGTH, [bitmap bytesPerRow] / ([bitmap bitsPerPixel] / 8));
        // RGB bitmaps cannot use the default 4 byte byte alignment.
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        // Replace the bound texture's pixels with the contents of the bitmap.
        glTexSubImage2D(
            target,
            0, // level
            0, 0, // xoffset, yoffset
            [bitmap pixelsWide], [bitmap pixelsHigh],
            [bitmap samplesPerPixel] == 3 ? GL_RGB : GL_RGBA, // pixel data format
            GL_UNSIGNED_BYTE, // pixel data type
            [bitmap bitmapData]); // pointer to pixel data
    }
    @finally
    {
        // Restore the default pixel unpack row length and byte alignment. For reference, see the following OpenGL
        // documentation:
        // http://www.opengl.org/sdk/docs/man/xhtml/glPixelStore.xml
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    }
}
