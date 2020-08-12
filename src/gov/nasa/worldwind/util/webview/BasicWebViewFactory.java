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
 * @deprecated 
 */
@Deprecated
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
