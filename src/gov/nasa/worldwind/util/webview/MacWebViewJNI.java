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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: MacWebViewJNI.java 1948 2014-04-19 20:02:38Z dcollins $
 * @deprecated 
 */
@Deprecated
public class MacWebViewJNI
{
    static
    {
        try
        {
            System.loadLibrary("webview");
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("WebView.ExceptionCreatingWebView", t);
            Logging.logger().log(Level.SEVERE, message, t);
        }
    }

    public static native long allocWebViewWindow(Dimension frameSize);

    public static native void releaseWebViewWindow(long webViewWindowPtr);

    public static native void setHTMLString(long webViewWindowPtr, String htmlString);

    public static native void setHTMLStringWithBaseURL(long webViewWindowPtr, String htmlString, URL baseURL);

    public static native void setHTMLStringWithResourceResolver(long webViewWindowPtr, String htmlString,
        WebResourceResolver resourceResolver);

    public static native Dimension getFrameSize(long webViewWindowPtr);

    public static native void setFrameSize(long webViewWindowPtr, Dimension size);

    public static native Dimension getContentSize(long webViewWindowPtr);

    public static native Dimension getMinContentSize(long webViewWindowPtr);

    public static native void setMinContentSize(long webViewWindowPtr, Dimension size);

    public static native URL getContentURL(long webViewWindowPtr);

    public static native AVList[] getLinks(long webViewWindowPtr);

    public static native void goBack(long webViewWindowPtr);

    public static native void goForward(long webViewWindowPtr);

    public static native void sendEvent(long webViewWindowPtr, InputEvent event);

    public static native boolean mustDisplayInTexture(long webViewWindowPtr);

    public static native void displayInTexture(long webViewWindowPtr, int target);

    public static native void setPropertyChangeListener(long webViewWindowPtr, PropertyChangeListener listener);
}
