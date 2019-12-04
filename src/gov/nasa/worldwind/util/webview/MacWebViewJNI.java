/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
