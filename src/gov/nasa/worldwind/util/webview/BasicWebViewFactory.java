/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Factory implementation for creating WebView instances. This implementation detected the operating system, and creates
 * an instance of WebView appropriate for that system. On Windows, it creates {@link WindowsWebView}. On Mac OS, it
 * creates {@link MacWebView}. Other operating systems are not supported at this time.
 *
 * @author dcollins
 * @version $Id: BasicWebViewFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicWebViewFactory implements WebViewFactory
{
    /** Create the factory. */
    public BasicWebViewFactory()
    {
    }

    /** {@inheritDoc} */
    public WebView createWebView(Dimension frameSize)
    {
        if (frameSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (Configuration.isLinuxOS())
            return this.createLinuxWebView(frameSize);

        else if (Configuration.isMacOS())
            return this.createMacWebView(frameSize);

        else if (Configuration.isWindowsOS())
            return this.createWindowsWebView(frameSize);

        return this.createUnknownOSWebView(frameSize);
    }

    /**
     * Create a WebView for Linux. This implementation throws UnsupportedOperationException. Subclasses may override
     * this method to add Linux support to the factory.
     *
     * @param frameSize The size in pixels of the WebView's window frame.
     *
     * @return WebView for Linux.
     *
     * @throws UnsupportedOperationException Linux WebView is not supported at this time.
     */
    protected WebView createLinuxWebView(Dimension frameSize)
    {
        return this.createUnknownOSWebView(frameSize); // TODO: implement native WebView for Linux.
    }

    /**
     * Create a WebView for Mac OS.
     *
     * @param frameSize The size in pixels of the WebView's window frame.
     *
     * @return WebView instance for Mac.
     */
    protected WebView createMacWebView(Dimension frameSize)
    {
        return new MacWebView(frameSize);
    }

    /**
     * Create a WebView for Windows.
     *
     * @param frameSize The size in pixels of the WebView's window frame.
     *
     * @return WebView instance for Windows.
     */
    protected WebView createWindowsWebView(Dimension frameSize)
    {
        return new WindowsWebView(frameSize);
    }

    /**
     * Create a WebView for an operating system other than Windows, Mac, or Linux. This implementation throws
     * UnsupportedOperationException. Subclasses may override this method to add support for another operating system to
     * the factory.
     *
     * @param frameSize The size in pixels of the WebView's window frame.
     *
     * @return WebView instance
     *
     * @throws UnsupportedOperationException WebView is only implemented for Windows and Mac at this time.
     */
    @SuppressWarnings( {"UnusedDeclaration"})
    protected WebView createUnknownOSWebView(Dimension frameSize)
    {
        String message = Logging.getMessage("NativeLib.UnsupportedOperatingSystem", "WebView",
            System.getProperty("os.name"));
        Logging.logger().severe(message);
        throw new UnsupportedOperationException(message);
    }
}
