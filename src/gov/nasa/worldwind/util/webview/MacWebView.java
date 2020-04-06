/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.webview;

import com.jogamp.opengl.util.texture.Texture;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: MacWebView.java 1171 2013-02-11 21:45:02Z dcollins $
 * @deprecated 
 */
@Deprecated
public class MacWebView extends AbstractWebView
{
    /** The address of the native WebViewWindow object. Initialized during construction. */
    protected long webViewWindowPtr;

    public MacWebView(Dimension frameSize)
    {
        if (frameSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!Configuration.isMacOS())
        {
            String message = Logging.getMessage("NativeLib.UnsupportedOperatingSystem", "Mac WebView",
                System.getProperty("os.name"));
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        this.frameSize = frameSize;
        this.webViewWindowPtr = MacWebViewJNI.allocWebViewWindow(this.frameSize);

        MacWebViewJNI.setPropertyChangeListener(this.webViewWindowPtr, this);
    }

    public void dispose()
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.releaseWebViewWindow(this.webViewWindowPtr);
            this.webViewWindowPtr = 0;
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString)
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.setHTMLString(this.webViewWindowPtr, htmlString);
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString, URL baseURL)
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.setHTMLStringWithBaseURL(this.webViewWindowPtr, htmlString, baseURL);
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString, WebResourceResolver resourceResolver)
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.setHTMLStringWithResourceResolver(this.webViewWindowPtr, htmlString, resourceResolver);
        }
    }

    protected void doSetFrameSize(Dimension size)
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.setFrameSize(this.webViewWindowPtr, size);
        }
    }

    /** {@inheritDoc} */
    public Dimension getContentSize()
    {
        if (this.webViewWindowPtr != 0)
        {
            return MacWebViewJNI.getContentSize(this.webViewWindowPtr);
        }

        return null;
    }

    /** {@inheritDoc} */
    public Dimension getMinContentSize()
    {
        if (this.webViewWindowPtr != 0)
        {
            return MacWebViewJNI.getMinContentSize(this.webViewWindowPtr);
        }

        return null;
    }

    /** {@inheritDoc} */
    public void setMinContentSize(Dimension size)
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.setMinContentSize(this.webViewWindowPtr, size);
        }
    }

    /** {@inheritDoc} */
    public URL getContentURL()
    {
        if (this.webViewWindowPtr != 0)
        {
            return MacWebViewJNI.getContentURL(this.webViewWindowPtr);
        }

        return null;
    }

    /** {@inheritDoc} */
    public Iterable<AVList> getLinks()
    {
        if (this.webViewWindowPtr != 0)
        {
            AVList[] linkParams = MacWebViewJNI.getLinks(this.webViewWindowPtr);
            if (linkParams != null)
                return Arrays.asList(linkParams);
        }

        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    public void sendEvent(InputEvent event)
    {
        if (this.webViewWindowPtr != 0 && event != null)
        {
            MacWebViewJNI.sendEvent(this.webViewWindowPtr, event);
        }
    }

    /** {@inheritDoc} */
    public void goBack()
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.goBack(this.webViewWindowPtr);
        }
    }

    /** {@inheritDoc} */
    public void goForward()
    {
        if (this.webViewWindowPtr != 0)
        {
            MacWebViewJNI.goForward(this.webViewWindowPtr);
        }
    }

    /**
     * Not implemented. MacWebView generates transparent WebView textures, so setting a background color is not
     * necessary. The texture can be drawn over the desired background color.
     */
    public void setBackgroundColor(Color color)
    {
        // Do nothing
    }

    /**
     * Not implemented. MacWebView generates transparent WebView textures, so setting a background color is not
     * necessary. The texture can be drawn over the desired background color.
     */
    public Color getBackgroundColor()
    {
        return null;
    }

    //**********************************************************************//
    //********************  Texture Representation  ************************//
    //**********************************************************************//

    @Override
    protected WWTexture createTextureRepresentation(DrawContext dc)
    {
        BasicWWTexture texture = new MacWebViewTexture(this.getFrameSize(), false);
        texture.setUseAnisotropy(false); // Do not use anisotropic texture filtering.

        return texture;
    }

    protected class MacWebViewTexture extends WebViewTexture
    {
        /**
         * Indicates whether updating this <code>WebViewTexture's</code> OpenGL texture has failed. When
         * <code>true</code>, this <code>WebViewTexture's</code> stops attempting to update its texture. Initially
         * <code>false</code>.
         */
        protected boolean textureUpdateFailed;

        public MacWebViewTexture(Dimension frameSize, boolean useMipMaps)
        {
            super(frameSize, useMipMaps, true);
        }

        @Override
        protected void updateIfNeeded(DrawContext dc)
        {
            if (this.textureUpdateFailed)
                return;

            // Return immediately if the texture isn't in the texture cache, and wait to update until the texture is
            // initialized and placed in the cache. This method is called after the texture is bound, so we'll get
            // another chance to update as long as the WebView generates repaint events when it changes.
            Texture texture = this.getTextureFromCache(dc);
            if (texture == null)
                return;

            try
            {
                this.displayInTexture(dc, texture);
            }
            catch (Exception e)
            {
                // Log an exception indicating that updating the texture failed, but do not re-throw it. This is called
                // from within the rendering loop, and we want to avoid causing any other rendering code to fail.
                Logging.logger().log(Level.SEVERE, Logging.getMessage("WebView.ExceptionUpdatingTexture"), e);
                // Indicate that updating this WebViewTexture's OpenGL texture failed to avoid subsequent attempts.
                this.textureUpdateFailed = true;
            }
        }

        @SuppressWarnings({"UnusedParameters"})
        protected void displayInTexture(DrawContext dc, Texture texture)
        {
            // Return immediately if the native WebViewWindow has been released. This indicates the MacWebView has been
            // disposed, so there's nothing to do.
            long webViewWindowPtr = MacWebView.this.webViewWindowPtr;
            if (webViewWindowPtr == 0)
                return;

            // Load the WebViewWindow's current display pixels into the currently bound OGL texture if the native
            // WebView indicates that the display has changed since our last call to displayInTexture.
            if (MacWebViewJNI.mustDisplayInTexture(webViewWindowPtr))
            {
                MacWebViewJNI.displayInTexture(webViewWindowPtr, texture.getTarget());
            }
        }
    }
}
