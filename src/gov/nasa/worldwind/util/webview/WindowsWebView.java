/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.webview;

import com.jogamp.opengl.util.texture.Texture;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * {@link WebView} implementation for Windows. This implementation uses the Window's native web browser control and the
 * MSHTML library to render a web page and create an OpenGL texture from the web browser window.
 * <h2>Limits on the number of WebViews that can be created</h2> WindowsWebView creates a hidden
 * native window. Creating the native window can fail if the process runs out of Windows user object handles. Other GUI
 * elements in an application also consume these handles, so it is difficult to put a firm limit on how many WebViews
 * can be created. An application that creates only WebViews and no other windows can create about 1500 WebViews before
 * running out of handles. See <a href="http://msdn.microsoft.com/en-us/library/ms725486%28v=vs.85%29.aspx">MSDN</a> for
 * more information on User Objects and operating system limits.
 *
 * @author pabercrombie
 * @version $Id: WindowsWebView.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WindowsWebView extends AbstractWebView
{
    /** Lock to protect creation of the web view message loop thread. */
    protected static final Object webViewUILock = new Object();
    /**
     * Thread to run web view message loop. At most one message loop thread is running at any time, and it is shared by
     * all WebView instances.
     */
    protected static Thread webViewUI;
    /** Identifier for the message loop in native code. */
    protected static long webViewMessageLoop;

    /** The address of the native WindowsWebView object. Initialized during construction. */
    protected long webViewWindowPtr;
    /** The address of the native NotificationAdapter object. Initialized during construction. */
    protected long observerPtr;

    /**
     * Count of the number of active (non-disposed) WebView instances. The WebView UI thread is started when a WebView
     * is created (if it's not already running), and terminated when there are no active instances.
     */
    protected static AtomicInteger instances = new AtomicInteger();

    /** Flag to the indicate that the WebView has been disposed. */
    protected boolean disposed = false;

    protected Color backgroundColor;

    /**
     * Create a new WebView.
     *
     * @param frameSize The size of the WebView rectangle.
     *
     * @throws UnsupportedOperationException if this class is instantiated on a non-Windows operating system.
     * @throws WWRuntimeException            if creating the native web browser window fails for any reason. For
     *                                       example, because the process has run out of User Object handles (see
     *                                       documentation <a href="#limits">above</a>).
     */
    public WindowsWebView(Dimension frameSize)
    {
        if (frameSize == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!Configuration.isWindowsOS())
        {
            String message = Logging.getMessage("NativeLib.UnsupportedOperatingSystem", "Windows WebView",
                System.getProperty("os.name"));
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        this.frameSize = frameSize;

        try
        {
            // Increment the instance counter
            instances.incrementAndGet();

            // Make sure that the message loop thread is running
            this.ensureMessageLoopRunning();

            // Create the web view
            this.webViewWindowPtr = WindowsWebViewJNI.newWebViewWindow(webViewMessageLoop);
            if (this.webViewWindowPtr == 0)
            {
                String message = Logging.getMessage("WebView.NativeExceptionInitializingWebView");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            WindowsWebViewJNI.setFrameSize(this.webViewWindowPtr, this.frameSize.width, this.frameSize.height);

            this.observerPtr = WindowsWebViewJNI.newNotificationAdapter(this);

            WindowsWebViewJNI.addWindowUpdateObserver(this.webViewWindowPtr, observerPtr);
        }
        catch (RuntimeException e)
        {
            // If the WebView was not created successfully do not increment the instance counter.
            instances.decrementAndGet();
            this.handleWebViewCreationError();
            throw e;
        }
        catch (Error e)
        {
            // If the WebView was not created successfully do not increment the instance counter.
            instances.decrementAndGet();
            this.handleWebViewCreationError();
            throw e;
        }
    }

    /**
     * This method is called by the constructor if an exception is thrown creating the WebView. It gives the WebView a
     * change to cleanup static state that may have been set during the failed WebView construction.
     */
    protected void handleWebViewCreationError()
    {
        try
        {
            this.stopMessageLoopIfNoInstances();
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("WebView.ExceptionStoppingWebViewThread", t);
            Logging.logger().severe(message);
        }
    }

    /** {@inheritDoc} */
    public void dispose()
    {
        if (this.disposed) // Do not dispose the WebView multiple times
            return;

        try
        {
            // Remove the notification adapter
            if (webViewWindowPtr != 0 && observerPtr != 0)
                WindowsWebViewJNI.removeWindowUpdateObserver(webViewWindowPtr, observerPtr);
            // Free the native WebView object associated with this Java WebView object.
            if (webViewWindowPtr != 0)
            {
                WindowsWebViewJNI.releaseWebView(webViewWindowPtr);
                // Decrement the instance counter. Only do this if the webViewWindow pointer was non-zero, indicating
                // that native resources were actually allocated.
                instances.decrementAndGet();
            }
            if (observerPtr != 0)
                WindowsWebViewJNI.releaseComObject(observerPtr);

            this.webViewWindowPtr = 0;
            this.observerPtr = 0;

            // Terminate the message loop thread if this is the last active instance.
            this.stopMessageLoopIfNoInstances();

            this.disposed = true;
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage("generic.ExceptionAttemptingToDisposeRenderable"), e);
        }
    }

    /**
     * Ensure that the message loop thread is running. This method simply returns if the thread is already running. It
     * creates a new thread if the message thread is not running. This method does not return until the message loop is
     * initialized and ready for use.
     */
    protected void ensureMessageLoopRunning()
    {
        synchronized (webViewUILock)
        {
            if (webViewUI == null || !webViewUI.isAlive())
            {
                webViewMessageLoop = 0;

                // Create a new thread to run the web view message loop.
                webViewUI = new Thread("WebView UI")
                {
                    public void run()
                    {
                        try
                        {
                            // Create a message loop in native code. This call must return
                            // before any messages are sent to the WebView.
                            webViewMessageLoop = WindowsWebViewJNI.newMessageLoop();
                        }
                        catch (Throwable t)
                        {
                            webViewMessageLoop = -1;
                        }
                        finally
                        {
                            // Notify the outer thread that the message loop is ready or failed to start.
                            synchronized (webViewUILock)
                            {
                                webViewUILock.notify();
                            }
                        }

                        // Process messages in native code until the message loop
                        // is terminated.
                        WindowsWebViewJNI.runMessageLoop(webViewMessageLoop);
                    }
                };
                webViewUI.start();

                // Wait for the newly started thread to create the message loop. We cannot
                // safely use the WebView until the message loop has been initialized.
                while (webViewMessageLoop == 0)
                {
                    try
                    {
                        webViewUILock.wait(1000);
                    }
                    catch (InterruptedException ignored)
                    {
                    }
                }
            }
        }
    }

    /**
     * Terminate the message loop thread if there are no active (non-disposed) WebView instances. Has no effect if there
     * are active instances.
     */
    protected void stopMessageLoopIfNoInstances()
    {
        synchronized (webViewUILock)
        {
            if (instances.get() <= 0)
            {
                WindowsWebViewJNI.releaseMessageLoop(webViewMessageLoop);
                webViewMessageLoop = 0;
                webViewUI = null;
            }
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString)
    {
        if (this.webViewWindowPtr != 0)
        {
            WindowsWebViewJNI.setHTMLString(this.webViewWindowPtr, htmlString, null);
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString, URL baseURL)
    {
        if (this.webViewWindowPtr != 0)
        {
            WindowsWebViewJNI.setHTMLString(this.webViewWindowPtr, htmlString,
                baseURL != null ? baseURL.toString() : null);
        }
    }

    /** {@inheritDoc} */
    public void setHTMLString(String htmlString, WebResourceResolver resourceResolver)
    {
        if (this.webViewWindowPtr != 0)
        {
            WindowsWebViewJNI.setHTMLStringWithResourceResolver(this.webViewWindowPtr, htmlString, resourceResolver);
        }
    }

    /** {@inheritDoc} */
    public Dimension getContentSize()
    {
        return WindowsWebViewJNI.getContentSize(webViewWindowPtr);
    }

    /** {@inheritDoc} */
    public Dimension getMinContentSize()
    {
        if (this.webViewWindowPtr != 0)
        {
            return WindowsWebViewJNI.getMinContentSize(this.webViewWindowPtr);
        }

        return null;
    }

    /** {@inheritDoc} */
    public void setMinContentSize(Dimension size)
    {
        if (this.webViewWindowPtr != 0)
        {
            WindowsWebViewJNI.setMinContentSize(this.webViewWindowPtr, size.width, size.height);
        }
    }

    /** {@inheritDoc} */
    public URL getContentURL()
    {
        if (this.webViewWindowPtr != 0)
        {
            return WWIO.makeURL(WindowsWebViewJNI.getContentURL(this.webViewWindowPtr));
        }
        return null;
    }

    protected void doSetFrameSize(Dimension size)
    {
        if (this.webViewWindowPtr != 0)
            WindowsWebViewJNI.setFrameSize(this.webViewWindowPtr, size.width, size.height);
    }

    /** {@inheritDoc} */
    public void sendEvent(InputEvent event)
    {
        if (event != null)
        {
            // Convert OpenGL coordinates to Windows.
            if (event instanceof MouseEvent)
                event = convertToWindows((MouseEvent) event);

            // Send the AWT InputEvent to the native WebView object
            if (this.webViewWindowPtr != 0)
                WindowsWebViewJNI.sendEvent(this.webViewWindowPtr, event);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to apply the active state to the native WebView.
     */
    @Override
    public void setActive(boolean active)
    {
        super.setActive(active);

        if (this.webViewWindowPtr != 0)
            WindowsWebViewJNI.setActive(this.webViewWindowPtr, active);
    }

    /** {@inheritDoc} */
    public void goBack()
    {
        if (this.webViewWindowPtr != 0)
            WindowsWebViewJNI.goBack(this.webViewWindowPtr);
    }

    /** {@inheritDoc} */
    public void goForward()
    {
        if (this.webViewWindowPtr != 0)
            WindowsWebViewJNI.goForward(this.webViewWindowPtr);
    }

    /** {@inheritDoc} */
    public void setBackgroundColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Only set the color if it actually changed
        if (!color.equals(this.getBackgroundColor()))
        {
            this.backgroundColor = color;

            // Convert the color to an RGB hex triplet string that the WebBrowser will understand
            int rgb = (color.getRed() & 0xFF) << 16
                | (color.getGreen() & 0xFF) << 8
                | (color.getBlue() & 0xFF);
            String colorString = String.format("#%06X", rgb);

            WindowsWebViewJNI.setBackgroundColor(this.webViewWindowPtr, colorString);
        }
    }

    /** {@inheritDoc} */
    public Color getBackgroundColor()
    {
        return this.backgroundColor;
    }

    /** {@inheritDoc} */
    public Iterable<AVList> getLinks()
    {
        if (this.webViewWindowPtr != 0)
        {
            AVList[] links = WindowsWebViewJNI.getLinks(this.webViewWindowPtr);
            if (links != null)
                return Arrays.asList(links);
        }
        return Collections.emptyList();
    }

    /**
     * Converts the specified mouse event's screen point from WebView coordinates to Windows coordinates, and returns a
     * new event who's screen point is in Windows coordinates, with the origin at the upper left corner of the WebView
     * window.
     *
     * @param e The event to convert.
     *
     * @return A new mouse event in the Windows coordinate system.
     */
    protected MouseEvent convertToWindows(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();

        // Translate OpenGL screen coordinates to Windows by moving the Y origin from the lower left corner to
        // the upper left corner and flipping the direction of the Y axis.
        y = this.frameSize.height - y;

        if (e instanceof MouseWheelEvent)
        {
            return new MouseWheelEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y,
                e.getClickCount(), e.isPopupTrigger(), ((MouseWheelEvent) e).getScrollType(),
                ((MouseWheelEvent) e).getScrollAmount(), ((MouseWheelEvent) e).getWheelRotation());
        }
        else
        {
            return new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y,
                e.getClickCount(), e.isPopupTrigger(), e.getButton());
        }
    }

    //**********************************************************************//
    //********************  Texture Representation  ************************//
    //**********************************************************************//

    /** {@inheritDoc} */
    @Override
    protected WWTexture createTextureRepresentation(DrawContext dc)
    {
        BasicWWTexture texture = new WindowsWebViewTexture(this.getFrameSize(), false);
        texture.setUseAnisotropy(false); // Do not use anisotropic texture filtering.

        return texture;
    }

    protected class WindowsWebViewTexture extends WebViewTexture
    {
        protected long updateTime = -1;

        public WindowsWebViewTexture(Dimension frameSize, boolean useMipMaps)
        {
            super(frameSize, useMipMaps, true);
        }

        /**
         * Update the texture if the native WebView window has changed.
         *
         * @param dc Draw context
         */
        @Override
        protected void updateIfNeeded(DrawContext dc)
        {
            // Return immediately if the native WebViewWindow object isn't initialized, and wait to update until the
            // native object is initialized. This method is called after the texture is bound, so we'll get another
            // chance to update as long as the WebView generates repaint events when it changes.
            long webViewWindowPtr = WindowsWebView.this.webViewWindowPtr;
            if (webViewWindowPtr == 0)
                return;

            // Return immediately if the texture isn't in the texture cache, and wait to update until the texture is
            // initialized and placed in the cache. This method is called after the texture is bound, so we'll get
            // another chance to update as long as the WebView generates repaint events when it changes.
            Texture texture = this.getTextureFromCache(dc);
            if (texture == null)
                return;

            // Load the WebViewWindow's current display pixels into the currently bound OGL texture if our update time
            // is different than the WebViewWindow's update time.
            long newUpdateTime = WindowsWebViewJNI.getUpdateTime(webViewWindowPtr);
            if (newUpdateTime != this.updateTime)
            {
                WindowsWebViewJNI.loadDisplayInGLTexture(webViewWindowPtr, texture.getTarget());
                this.updateTime = newUpdateTime;
            }
        }
    }
}
