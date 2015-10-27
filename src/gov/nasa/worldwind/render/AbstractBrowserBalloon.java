/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.globes.GlobeStateKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.webview.*;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

/**
 * A <code>{@link gov.nasa.worldwind.render.Balloon}</code> that displays HTML, JavaScript, and Flash content using the
 * system's native browser. The balloon's HTML content is specified by calling <code>setText</code> with an HTML
 * formatted string. A browser balloon resolves relative <code>URLs</code> in the HTML content by consulting its
 * resource resolver. The resource resolver converts a relative <code>URL</code> to an absolute <code>URL</code> that
 * the browser can load. The resource resolver is specified by calling <code>setResourceResolver</code>, and may be one
 * of the following: a <code>{@link gov.nasa.worldwind.util.webview.WebResourceResolver}</code>, a <code>{@link
 * java.net.URL}</code>, or a <code>String</code> containing a valid URL description. If a browser balloon's resource
 * resolver is <code>null</code> or is an unrecognized type, the browser interprets relative <code>URLs</code> as
 * unresolved references.
 * <p/>
 * <b>Browser Controls</b>
 * <p/>
 * Browser balloons display three default browser controls that enable users to navigate the browser's history back and
 * forward, and to close the balloon. When the user selects one of these controls, a <code>SelectEvent</code> is
 * generated with the <code>PickedObject's</code> <code>AVKey.ACTION</code> value set to one of
 * <code>AVKey.CLOSE</code>, <code>AVKey.BACK</code>, or <code>AVKey.FORWARD</code>. These controls may be enabled or
 * disabled by calling <code>setDrawBrowserControls</code> (they are enabled by default), and may be customized by
 * adding or removing controls from the browser balloon. See <code>getBrowserControls</code>,
 * <code>addBrowserControl</code>, and <code>removeBrowserControl</code>.
 * <p/>
 * <b>Resize Control</b>
 * <p/>
 * Browser balloons provide a default resize control that is activated by dragging the balloon's border. When the user
 * drags the border, a <code>SelectEvent</code> is generated with the PickedObject's <code>AVKey.ACTION</code> value set
 * to <code>AVKey.RESIZE</code>. The <code>PickedObject's</code> <code>AVKey.BOUNDS</code> value holds the Balloon's
 * screen bounds in AWT coordinates (origin at the upper left corner) as a <code>java.awt.Rectangle</code>.  The resize
 * control may be enabled or disabled by calling <code>setDrawResizeControl</code> (it is enabled by default).
 * <p/>
 * <b>Balloon Size</b>
 * <p/>
 * The browser balloon's screen width and height are specified as a <code>{@link gov.nasa.worldwind.render.Size}</code>
 * object in its <code>BalloonAttributes</code>. This size may configured in one of the following three modes: <ul>
 * <li>Explicit size in pixels.</li> <li>Fraction of the <code>WorldWindow</code> size.</li> <li>Fit to the balloon's
 * HTML content.</li> </ul> The balloon's width and height may be configured independently, enabling any combination of
 * these three modes. The balloon's width and height are limited by its maximum size, which is also specified as a
 * <code>Size</code> object in its <code>BalloonAttributes</code>. If the maximum size is <code>null</code>, the
 * balloon's width and height are unlimited. The space provided for the balloon's HTML content equal to the balloon's
 * screen width and height minus the balloon's insets, also specified in its <code>BalloonAttributes</code>
 * <p/>
 * The balloon's width or height (or both) may be configured to fit to the balloon's HTML content by configuring its
 * <code>BalloonAttributes</code> with a <code>Size</code> who's width or height mode is
 * <code>Size.NATIVE_DIMENSION</code> or <code>Size.MAINTAIN_ASPECT_RATIO</code>. When configured in this mode, the
 * browser balloon's size always fits the HTML content specified at construction or by calling <code>setText</code>. If
 * a user action causes the balloon to navigate to another page, the balloon continues to fit to its current HTML
 * content.
 * <p/>
 * The balloon frame's corner radius and leader width specified in its <code>BalloonAttributes</code> are limited by the
 * balloon's size. The corner radius is first limited by the balloon's width and height, then the leader width is
 * limited by the balloon's width and height minus space taken by rounded corners. For example, if the corner radius is
 * 100 and the width and height are 50 and 100, the actual corner radius used is 25 - half of the rectangle's smallest
 * dimension. Similarly, if the leader is attached to the rectangle's bottom, its width is limited by the rectangle's
 * width minus any space used by the balloon's rounded corners.
 * <p/>
 * <b>Hiding the balloon</b>
 * <p/>
 * The balloon can be made visible or invisible by calling {@link #setVisible(boolean) setVisible}. The balloon's {@code
 * visibilityAction} determines what happens to the native web browser when the balloon is invisible. The balloon can
 * either release its native web browser, preventing the native browser from consuming system resources while the
 * balloon is invisible, or it can retain the native browser. Possible values of {@code visibilityAction} are: <ul> <li>
 * AVKey.VISIBILITY_ACTION_RELEASE (Default) &mdash; Release the native web browser when this balloon is invisible. The
 * browser will be recreated when the balloon becomes visible again. All browser state (navigation history, page scroll
 * position, etc) will be lost. </li><li> AVKey.VISIBILITY_ACTION_RETAIN &mdash; Do not release the native browser when
 * this balloon is invisible. This action will retain all browser state while this balloon is invisible, but the browser
 * will continue to consume system resources. Dynamic content on the page (such as animations and Flash video) will
 * continue to play while the balloon is invisible.</li></ul>
 *
 * @author dcollins
 * @version $Id: AbstractBrowserBalloon.java 2148 2014-07-14 16:27:49Z tgaskins $
 */
public abstract class AbstractBrowserBalloon extends AbstractBalloon implements HotSpot, Disposable
{

    public static class BrowserControl extends AVListImpl
    {
        protected static final Color DEFAULT_COLOR = new Color(255, 255, 255, 153);
        protected static final Color DEFAULT_HIGHLIGHT_COLOR = new Color(255, 255, 255, 255);

        protected boolean visible = true;
        protected Offset offset;
        protected Size size;
        protected Object imageSource;
        protected Color color;
        protected Color highlightColor;
        protected WWTexture texture;

        public BrowserControl(String action, Offset offset, Object imageSource)
        {
            if (offset == null)
            {
                String message = Logging.getMessage("nullValue.OffsetIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (WWUtil.isEmpty(imageSource))
            {
                String message = Logging.getMessage("nullValue.ImageSource");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.setAction(action);
            this.offset = offset;
            this.size = new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null);
            this.imageSource = imageSource;
        }

        public BrowserControl(String action, Offset offset, Size size, Object imageSource)
        {
            if (offset == null)
            {
                String message = Logging.getMessage("nullValue.OffsetIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (size == null)
            {
                String message = Logging.getMessage("nullValue.SizeIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (WWUtil.isEmpty(imageSource))
            {
                String message = Logging.getMessage("nullValue.ImageSource");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.setValue(AVKey.ACTION, action);
            this.offset = offset;
            this.size = size;
            this.imageSource = imageSource;
        }

        public boolean isVisible()
        {
            return this.visible;
        }

        public void setVisible(boolean visible)
        {
            this.visible = visible;
        }

        public String getAction()
        {
            return this.getStringValue(AVKey.ACTION);
        }

        public void setAction(String action)
        {
            this.setValue(AVKey.ACTION, action);
        }

        public Offset getOffset()
        {
            return this.offset;
        }

        public void setOffset(Offset offset)
        {
            if (offset == null)
            {
                String message = Logging.getMessage("nullValue.OffsetIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.offset = offset;
        }

        public Size getSize()
        {
            return size;
        }

        public void setSize(Size size)
        {
            if (size == null)
            {
                String message = Logging.getMessage("nullValue.SizeIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.size = size;
        }

        public Color getColor()
        {
            return color;
        }

        public void setColor(Color color)
        {
            this.color = color;
        }

        public Color getHighlightColor()
        {
            return highlightColor;
        }

        public void setHighlightColor(Color highlightColor)
        {
            this.highlightColor = highlightColor;
        }

        public Object getImageSource()
        {
            return imageSource;
        }

        public void setImageSource(Object imageSource)
        {
            if (WWUtil.isEmpty(imageSource))
            {
                String message = Logging.getMessage("nullValue.ImageSource");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.imageSource = imageSource;
            this.texture = null; // Force a texture to be re-created with the new image source.
        }

        protected WWTexture getTexture()
        {
            if (this.texture == null && this.getImageSource() != null)
            {
                this.texture = new BasicWWTexture(this.getImageSource(), true);
            }

            return this.texture;
        }
    }

    /**
     * Holds the vertex data and the defining properties of a balloon's frame and leader geometry. The
     * <code>vertexBuffer</code> represents the screen-coordinate vertices of the balloon's frame. The
     * <code>size</code>, <code>offset</code>, <code>balloonShape</code>, <code>leaderShape</code>,
     * <code>leaderWidth</code>, and <code>cornerRadius</code> are the frame geometry's defining properties. These are
     * used to determine when the frame geometry is invalid and must be recomputed.
     */
    protected static class FrameGeometryInfo
    {
        protected FloatBuffer vertexBuffer;
        protected Dimension size;
        protected Point offset;
        protected String balloonShape;
        protected String leaderShape;
        protected int leaderWidth;
        protected int cornerRadius;

        public FrameGeometryInfo()
        {
        }
    }

    protected class OrderedBrowserBalloon implements OrderedRenderable
    {
        /** The location and size of the balloon's content frame in the viewport (on the screen). */
        protected Rectangle screenRect;
        /** The extent of the balloon's geometry in the viewport (on the screen). */
        protected Rectangle screenExtent;
        /**
         * The extend of the balloon's pickable geometry in the viewport (on the screen). Includes this balloon's outline
         * where it exceeds the screen extent.
         */
        protected Rectangle screenPickExtent;
        /** The location and size of the WebView's content frame in the viewport (on the screen). */
        protected Rectangle webViewRect;
        /** The balloon geometry vertices passed to OpenGL. */
        protected FrameGeometryInfo frameInfo;
        /** Used to order the balloon as an ordered renderable. */
        protected double eyeDistance;
        /** Identifies the frame used to calculate the balloon's geometry. */
        protected long geomTimeStamp = -1;
        /** Identifies the frame used to calculate the balloon's active attributes and points. */
        protected long frameTimeStamp = -1;

        @Override
        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint)
        {
            AbstractBrowserBalloon.this.pick(dc, pickPoint, this);
        }

        @Override
        public void render(DrawContext dc)
        {
            AbstractBrowserBalloon.this.drawOrderedRenderable(dc, this);
        }
    }

    /**
     * The browser balloon's default native size: 400x300. This default size is used when a balloon's size is configured
     * to use a native dimension, but the WebView either has not been created or its content size is not known. This
     * default chosen to minimize the popping effect when the balloon's size switches from the default to the WebView's
     * content size.
     */
    protected static final Dimension DEFAULT_NATIVE_SIZE = new Dimension(400, 300);
    /**
     * The default outline pick width in pixels. The default is 10 pixels, the maximum OpenGL line width supported by
     * most graphics cards.
     */
    protected static final int DEFAULT_OUTLINE_PICK_WIDTH = 10;
    /**
     * The class name of the default <code>{@link gov.nasa.worldwind.util.webview.WebViewFactory}</code> used to create
     * the balloon's internal <code>WebView</code>. This factory is used when the configuration does not specify a
     * WebView factory.
     */
    protected static final String DEFAULT_WEB_VIEW_FACTORY = BasicWebViewFactory.class.getName();
    /** The number of slices used to display a balloon frame as an ellipse: 64. */
    protected static final int FRAME_GEOMETRY_ELLIPSE_SLICES = 64;
    /** The number of slices used to display each of a rectangular balloon frame's rounded corners: 16. */
    protected static final int FRAME_GEOMETRY_RECTANGLE_CORNER_SLICES = 16;

    /**
     * Returns a list containing the browser balloon's three default browser controls, configured as follows:
     * <p/>
     * <table> <tr><th>Control</th><th>Action</th><th>Offset</th><th>Size</th><th>Image Source</th></tr>
     * <tr><td>Close</td><td><code>AVKey.CLOSE</code></td><td>(30, 25) pixels inset from the balloon's upper right
     * corner</td><td>Image source's native size in pixels (16x16)</td><td>images/browser-close-16x16.gif</td></tr>
     * <tr><td>Back</td><td><code>AVKey.BACK</code></td><td>(15, 25) pixels inset from the balloon's upper left
     * corner</td><td>Image source's native size in pixels (16x16)</td><td>images/browser-back-16x16.gif</td></tr>
     * <tr><td>Forward</td><td><code>AVKey.FORWARD</code></td><td>(35, 25) pixels inset from the balloon's upper left
     * corner</td><td>Image source's native size in pixels (16x16)</td><td>images/browser-forward-16x16.gif</td></tr>
     * </table>
     *
     * @return a list containing the browser balloon's default browser controls.
     */
    protected static List<BrowserControl> createDefaultBrowserControls()
    {
        return Arrays.asList(
            new BrowserControl(AVKey.CLOSE, new Offset(30.0, 25.0, AVKey.INSET_PIXELS, AVKey.INSET_PIXELS),
                "images/browser-close-16x16.gif"),
            new BrowserControl(AVKey.BACK, new Offset(15.0, 25.0, AVKey.PIXELS, AVKey.INSET_PIXELS),
                "images/browser-back-16x16.gif"),
            new BrowserControl(AVKey.FORWARD, new Offset(35.0, 25.0, AVKey.PIXELS, AVKey.INSET_PIXELS),
                "images/browser-forward-16x16.gif")
        );
    }

    /** Action that will occur when the balloon is made invisible. */
    protected String visibilityAction = AVKey.VISIBILITY_ACTION_RELEASE;
    protected boolean drawTitleBar = true;
    protected boolean drawBrowserControls = true;
    protected boolean drawResizeControl = true;
    /**
     * The line width used to draw the the balloon's outline during picking. Initially set to <code>{@link
     * #DEFAULT_OUTLINE_PICK_WIDTH}</code>.
     */
    protected int outlinePickWidth = DEFAULT_OUTLINE_PICK_WIDTH;
    protected List<BrowserControl> browserControls = new ArrayList<BrowserControl>(createDefaultBrowserControls());
    /**
     * Indicates the object used to resolve relative resource paths in this browser balloon's HTML content. May be one
     * of the following: <code>{@link gov.nasa.worldwind.util.webview.WebResourceResolver}</code>, <code>{@link
     * java.net.URL}</code>, <code>{@link String}</code> containing a valid URL description, or <code>null</code> to
     * specify that relative paths should be interpreted as unresolved references. Initially <code>null</code>.
     */
    protected Object resourceResolver;
    /** Identifies the time when the balloon text was updated. Initially -1. */
    protected long textUpdateTime = -1;
    /**
     * Denotes whether or not an attempt at WebView creation failed. When <code>true</code> the balloon does not perform
     * subsequent attempts to create the WebView. Initially <code>false</code>.
     */
    protected boolean webViewCreationFailed;
    /** Interface for interacting with the operating system's web browser control. Initially <code>null</code>. */
    protected WebView webView;
    /** Identifies the frame used to update the WebView's state. */
    protected long webViewTimeStamp = -1;
    /** The location of the balloon's content frame relative to the balloon's screen point in the viewport. */
    protected Point screenOffset;
    /**
     * The size of the WebView's HTML content size, in pixels. This is the size that the WebView can be displayed at
     * without the need for scroll bars. May be <code>null</code> or <code>(0, 0)</code>, indicating that the WebView's
     * HTML content size is unknown. Initially <code>null</code>.
     */
    protected Dimension webViewContentSize;
    /** The layer active during the most recent pick pass. */
    protected Layer pickLayer;
    /** The screen coordinate of the last <code>SelectEvent</code> sent to this balloon's <code>select</code> method. */
    protected Point lastPickPoint;
    /** Support for setting up and restoring picking state, and resolving the picked object. */
    protected PickSupport pickSupport = new PickSupport();
    /** Support for setting up and restoring OpenGL state during rendering. */
    protected OGLStackHandler osh = new OGLStackHandler();
    protected long screenBalloonPickFrame;
    protected long screenBalloonRenderFrame;

    protected HashMap<GlobeStateKey, OrderedBrowserBalloon>
        orderedRenderables = new HashMap<GlobeStateKey, OrderedBrowserBalloon>(1);

    protected AbstractBrowserBalloon(String text)
    {
        super(text);
    }

    protected abstract OrderedBrowserBalloon createOrderedRenderable();

    /**
     * Computes and stores the balloon's model-coordinate and screen-coordinate points.
     *
     * @param dc the current draw context.
     */
    protected abstract void computeBalloonPoints(DrawContext dc, OrderedBrowserBalloon obb);

    protected abstract void setupDepthTest(DrawContext dc, OrderedBrowserBalloon obb);

    /**
     * Disposes the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>. This does nothing
     * if the balloon is already disposed.
     */
    public void dispose()
    {
        this.disposeWebView();
    }

    public boolean isDrawTitleBar()
    {
        return this.drawTitleBar;
    }

    public void setDrawTitleBar(boolean draw)
    {
        this.drawTitleBar = draw;
    }

    public boolean isDrawBrowserControls()
    {
        return this.drawBrowserControls;
    }

    public void setDrawBrowserControls(boolean draw)
    {
        this.drawBrowserControls = draw;
    }

    public boolean isDrawResizeControl()
    {
        return this.drawResizeControl;
    }

    public void setDrawResizeControl(boolean draw)
    {
        this.drawResizeControl = draw;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * When this balloon is set to invisible, the {@code visibilityAction} determines what happens to the native web
     * browser that backs the balloon. By default, the browser resources are released when the balloon is not visible.
     *
     * @see #setVisibilityAction(String)
     */
    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);

        // If the balloon is not visible and the visibility action indicates to release the browser, dispose of the web
        // view to release native resources.
        if (!this.isVisible() && AVKey.VISIBILITY_ACTION_RELEASE.equals(this.getVisibilityAction()))
        {
            this.disposeWebView();
        }
    }

    /**
     * Indicates the outline line width (in pixels) used during picking.  A larger width than normal typically makes the
     * outline easier to pick.
     *
     * @return the outline line width (in pixels) used during picking.
     *
     * @see #setOutlinePickWidth(int)
     */
    public int getOutlinePickWidth()
    {
        return this.outlinePickWidth;
    }

    /**
     * Specifies the outline line width (in pixels) to use during picking. The specified <code>width</code> must be zero
     * or a positive integer. Specifying a pick width of zero effectively disables the picking of the balloon's outline
     * and its resize control. A larger width than normal typically makes the outline easier to pick.
     * <p/>
     * When the the balloon's resize control is enabled, the outline becomes the resize control and is drawn in the
     * specified <code>width</code>. Therefore this value also controls the balloon's resize control width. If the
     * resize control is disabled by calling <code>{@link #setDrawResizeControl(boolean)}</code> with a value of
     * <code>false</code>, this has no effect on the balloon's resize control until it is enabled.
     *
     * @param width the outline line width (in pixels) to use during picking.
     *
     * @throws IllegalArgumentException if <code>width</code> is less than zero.
     * @see #getOutlinePickWidth()
     * @see #setDrawResizeControl(boolean)
     */
    public void setOutlinePickWidth(int width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlinePickWidth = width;
    }

    public Iterable<BrowserControl> getBrowserControls()
    {
        return this.browserControls;
    }

    public void addBrowserControl(BrowserControl browserControl)
    {
        if (browserControl == null)
        {
            String message = Logging.getMessage("nullValue.BrowserControlIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.browserControls.add(browserControl);
    }

    public BrowserControl addBrowserControl(String action, Offset offset, Object imageSource)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(imageSource))
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BrowserControl browserControl = new BrowserControl(action, offset, imageSource);
        this.addBrowserControl(browserControl);

        return browserControl;
    }

    public BrowserControl addBrowserControl(String action, Offset offset, Size size, Object imageSource)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(imageSource))
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BrowserControl browserControl = new BrowserControl(action, offset, size, imageSource);
        this.addBrowserControl(browserControl);

        return browserControl;
    }

    public void addAllBrowserControls(Iterable<? extends BrowserControl> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (BrowserControl browserControl : iterable)
        {
            if (browserControl != null)
            {
                this.browserControls.add(browserControl);
            }
        }
    }

    public void removeBrowserControl(BrowserControl browserControl)
    {
        if (browserControl == null)
        {
            String message = Logging.getMessage("nullValue.BrowserControlIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.browserControls.remove(browserControl);
    }

    public void removeAllBrowserControls()
    {
        this.browserControls.clear();
    }

    /**
     * Indicates the object used to resolve relative resource paths in this browser balloon's HTML content.
     *
     * @return the object used to resolve relative resource paths in HTML content. One of the following: <code>{@link
     *         gov.nasa.worldwind.util.webview.WebResourceResolver}</code>, <code>{@link java.net.URL}</code>,
     *         <code>{@link String}</code> containing a valid URL description, or <code>null</code> to indicate that
     *         relative paths are interpreted as unresolved references.
     *
     * @see #setResourceResolver(Object)
     */
    public Object getResourceResolver()
    {
        return this.resourceResolver;
    }

    /**
     * Specifies a the object to use when resolving relative resource paths in this browser balloon's HTML content. The
     * <code>resourceResolver</code> may be one of the following:
     * <p/>
     * <ul> <li>a <code>{@link gov.nasa.worldwind.util.webview.WebResourceResolver}</code></li> <li>a <code>{@link
     * java.net.URL}</code></li> <li>a <code>{@link String}</code> containing a valid URL description</li> </ul>
     * <p/>
     * If the <code>resourceResolver</code> is <code>null</code> or is not one of the recognized types, this browser
     * balloon interprets relative resource paths as unresolved references.
     *
     * @param resourceResolver the object to use when resolving relative resource paths in HTML content. May be one of
     *                         the following: <code>{@link gov.nasa.worldwind.util.webview.WebResourceResolver}</code>,
     *                         <code>{@link java.net.URL}</code>, <code>{@link String}</code> containing a valid URL
     *                         description, or <code>null</code> to specify that relative paths should be interpreted as
     *                         unresolved references.
     *
     * @see #getResourceResolver()
     */
    public void setResourceResolver(Object resourceResolver)
    {
        this.resourceResolver = resourceResolver;

        // Setting a new resource resolver may change how the WebView content is rendered. Set the textUpdate time to
        // ensure that the WebView content will be reset on the next frame.
        this.textUpdateTime = -1;
    }

    /**
     * Indicates the the action that occurs when the BrowserBalloon is set to invisible. See {@link
     * #setVisibilityAction(String) setVisibilityAction} for a description of the possible actions.
     *
     * @return A string that indicates the action that will occur when the balloon is set to invisible.
     *
     * @see #setVisibilityAction(String)
     * @see #setVisible(boolean)
     */
    public String getVisibilityAction()
    {
        return visibilityAction;
    }

    /**
     * Specifies the action that occurs when this balloon is set to invisible. Possible actions are: <ul> <li>
     * AVKey.VISIBILITY_ACTION_RELEASE (Default) &mdash; Release the native web browser when the balloon is invisible.
     * The browser will be recreated when the balloon becomes visible. Note that all browser state (navigation history,
     * page scroll position, etc) will be lost. </li><li> AVKey.VISIBILITY_ACTION_RETAIN &mdash; Do not release the
     * native browser when this balloon is invisible. This action will retain all browser state while the balloon is
     * invisible, but the browser will continue to consume system resources while is is invisible. Dynamic content on
     * the page (for example, Flash video) will continue to play.</li></ul>
     *
     * @param visibilityAction Either {@link AVKey#VISIBILITY_ACTION_RELEASE} or {@link AVKey#VISIBILITY_ACTION_RETAIN}.
     */
    public void setVisibilityAction(String visibilityAction)
    {
        if (visibilityAction == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.visibilityAction = visibilityAction;
    }

    /** Navigate the browser to the previous page in the browsing history. Has no effect if there is previous page. */
    public void goBack()
    {
        if (this.webView != null)
            this.webView.goBack();
    }

    /** Navigate the browser to the next page in the browsing history. Has no effect if there is no next page. */
    public void goForward()
    {
        if (this.webView != null)
            this.webView.goForward();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to suppress <code>AVKey.REPAINT</code> property change events sent by the balloon's internal
     * <code>{@link gov.nasa.worldwind.util.webview.WebView}</code> when <code>isVisible</code> returns
     * <code>false</code>.
     */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        if (!this.isVisible() && propertyChangeEvent != null
            && AVKey.REPAINT.equals(propertyChangeEvent.getPropertyName()))
        {
            return;
        }

        super.propertyChange(propertyChangeEvent);
    }

    /** {@inheritDoc} */
    public Rectangle getBounds(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrderedBrowserBalloon obb = this.orderedRenderables.get(dc.getGlobe().getGlobeStateKey());
        if (obb == null)
            obb = this.createOrderedRenderable();

        // Update the balloon's active attributes and points if that hasn't already been done this frame.
        this.updateRenderStateIfNeeded(dc, obb);

        // Return the balloon's screen extent computed in updateRenderStateIfNeeded. This may be null.
        return obb.screenExtent;
    }

    public void pick(DrawContext dc, Point pickPoint, OrderedBrowserBalloon obb)
    {
        // This method is called only when ordered renderables are being drawn.

        if (!this.isPickEnabled())
            return;

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.drawOrderedRenderable(dc, obb);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    public void render(DrawContext dc)
    {
        // This render method is called twice during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. These two calls determine
        // whether to add the placemark and its optional line to the ordered renderable list during pick and render.

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        this.makeOrderedRenderable(dc);
    }

    /**
     * Updates the balloon's per-frame rendering state, and determines whether to queue an ordered renderable for the
     * balloon. This queues an ordered renderable if the balloon intersects the current viewing frustum, and if the
     * balloon's internal rendering state can be computed. This updates the balloon's rendering state by calling
     * <code>updateRenderStateIfNeeded</code>, and updates its geometry by calling <code>computeGeometry</code>.
     * <p/>
     * BrowserBalloon separates render state updates from geometry updates for two reasons: <ul> <li>Geometry may be
     * updated based on different conditions.</li> <li>Rendering state potentially needs to be updated in
     * getBounds.</li> </ul>
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Prevent screen balloons from drawing more than once per frame for 2D continuous globes.
        if (this instanceof ScreenBrowserBalloon && dc.isContinuous2DGlobe())
        {
            if (dc.isPickingMode() && this.screenBalloonPickFrame == dc.getFrameTimeStamp())
                return;

            if (!dc.isPickingMode() && this.screenBalloonRenderFrame == dc.getFrameTimeStamp())
                return;
        }

        OrderedBrowserBalloon obb = this.orderedRenderables.get(dc.getGlobe().getGlobeStateKey());
        if (obb == null)
        {
            obb = this.createOrderedRenderable();
            this.orderedRenderables.put(dc.getGlobe().getGlobeStateKey(), obb);
        }

        // Update the balloon's active attributes and points if that hasn't already been done this frame.
        this.updateRenderStateIfNeeded(dc, obb);

        // Exit immediately if either the balloon's active attributes or its screen rectangle are null. In either case
        // we cannot compute the balloon's geometry nor can we determine where to render the balloon.
        if (this.getActiveAttributes() == null || obb.screenRect == null)
            return;

        // Re-use geometry already calculated this frame.
        if (dc.getFrameTimeStamp() != obb.geomTimeStamp)
        {
            // Recompute this balloon's geometry only when an attribute change requires us to.
            if (this.mustRegenerateGeometry(obb))
                this.computeGeometry(obb);
            obb.geomTimeStamp = dc.getFrameTimeStamp();
        }

        // Update the balloon's WebView to be current with the BrowserBalloon's properties. This must be done after
        // updating the render state; this balloon's active attributes are applied to the WebView. Re-use WebView state
        // already calculated this frame.
        if (dc.getFrameTimeStamp() != this.webViewTimeStamp)
        {
            this.updateWebView(dc, obb);
            this.webViewTimeStamp = dc.getFrameTimeStamp();
        }

        if (this.intersectsFrustum(dc, obb))
        {
            dc.addOrderedRenderable(obb);

            if (dc.isPickingMode())
                this.screenBalloonPickFrame = dc.getFrameTimeStamp();
            else
                this.screenBalloonRenderFrame = dc.getFrameTimeStamp();
        }

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();
    }

    /**
     * Update the balloon's active attributes and points, if that hasn't already been done this frame. This updates the
     * balloon's rendering state as follows: <ul> <li>Computes the balloon's active attributes by calling
     * <code>determineActiveAttributes</code> and stores the result in <code>activeAttributes</code>.</li> <li>Computes
     * the balloon's model-coordinate and screen-coordinate points by calling <code>computeBalloonPoints</code>.</li>
     * </ul>
     *
     * @param dc the current draw context.
     */
    protected void updateRenderStateIfNeeded(DrawContext dc, OrderedBrowserBalloon obb)
    {
        // Re-use rendering state values already calculated this frame.
        if (dc.getFrameTimeStamp() != obb.frameTimeStamp)
        {
            this.updateRenderState(dc, obb);
            obb.frameTimeStamp = dc.getFrameTimeStamp();
        }
    }

    protected void updateRenderState(DrawContext dc, OrderedBrowserBalloon obb)
    {
        this.determineActiveAttributes();
        if (this.getActiveAttributes() == null)
            return;

        this.determineWebViewContentSize();
        this.computeBalloonPoints(dc, obb);
    }

    /**
     * Computes the size of this balloon's frame in the viewport (on the screen). If this balloon's maximum size is not
     * <code>null</code>, the returned size is no larger than the maximum size.
     *
     * @param dc          the current draw context.
     * @param activeAttrs the attributes used to compute the balloon's size.
     *
     * @return this balloon frame's screen size, in pixels.
     */
    protected Dimension computeSize(DrawContext dc, BalloonAttributes activeAttrs)
    {
        // Determine the balloon's current native size. If the WebView's content size is non-null and nonzero, then use
        // that as the basis for the balloon's native size. Otherwise use a default native size. This handles the case
        // where the balloon's size is computed either before the WebView is created or before the WebView's content
        // size is known. The default native size is chosen to minimize the popping effect when the balloon's size
        // switches from the default to the WebView's content size. If the balloon's size is not configured to use a
        // native dimension, this size is ignored.
        Dimension nativeSize;
        if (this.webViewContentSize != null && this.webViewContentSize.width != 0
            && this.webViewContentSize.height != 0)
        {
            // Convert the WebView's content size to a balloon frame size.
            nativeSize = this.computeFrameRectForWebViewRect(activeAttrs,
                new Rectangle(this.webViewContentSize)).getSize();
        }
        else
        {
            nativeSize = DEFAULT_NATIVE_SIZE;
        }

        Dimension size = activeAttrs.getSize().compute(nativeSize.width, nativeSize.height,
            dc.getView().getViewport().width, dc.getView().getViewport().height);

        if (activeAttrs.getMaximumSize() != null)
        {
            Dimension maxSize = activeAttrs.getMaximumSize().compute(nativeSize.width, nativeSize.height,
                dc.getView().getViewport().width, dc.getView().getViewport().height);

            if (size.width > maxSize.width)
                size.width = maxSize.width;
            if (size.height > maxSize.height)
                size.height = maxSize.height;
        }

        return size;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Point computeOffset(DrawContext dc, BalloonAttributes activeAttrs, int width, int height)
    {
        Point2D.Double offset = activeAttrs.getOffset().computeOffset(width, height, 1d, 1d);
        return new Point((int) offset.getX(), (int) offset.getY());
    }

    /**
     * Indicates whether this balloon's screen-coordinate geometry must be recomputed as a result of a balloon attribute
     * changing.
     *
     * @return <code>true</code> if this balloon's geometry must be recomputed, otherwise <code>false</code>.
     */
    protected boolean mustRegenerateGeometry(OrderedBrowserBalloon obb)
    {
        if (obb.frameInfo == null)
            return true;

        if (!obb.screenRect.getSize().equals(obb.frameInfo.size) || !this.screenOffset.equals(obb.frameInfo.offset))
            return true;

        BalloonAttributes activeAttrs = this.getActiveAttributes();
        return !activeAttrs.getBalloonShape().equals(obb.frameInfo.balloonShape)
            || !activeAttrs.getLeaderShape().equals(obb.frameInfo.leaderShape)
            || activeAttrs.getLeaderWidth() != obb.frameInfo.leaderWidth
            || activeAttrs.getCornerRadius() != obb.frameInfo.cornerRadius;
    }

    /**
     * Updates the balloon's screen-coordinate geometry in <code>frameInfo</code> according to the current screen
     * bounds, screen offset, and active attributes.
     */
    protected void computeGeometry(OrderedBrowserBalloon obb)
    {
        if (obb.screenRect == null)
            return;

        BalloonAttributes activeAttrs = this.getActiveAttributes();

        if (obb.frameInfo == null)
            obb.frameInfo = new FrameGeometryInfo();

        // Regenerate the frame's vertex buffer.
        obb.frameInfo.vertexBuffer = this.createFrameVertices(obb);

        // Update the current attributes associated with FrameInfo's vertex buffer.
        obb.frameInfo.size = obb.screenRect.getSize();
        obb.frameInfo.offset = this.screenOffset;
        obb.frameInfo.balloonShape = activeAttrs.getBalloonShape();
        obb.frameInfo.leaderShape = activeAttrs.getLeaderShape();
        obb.frameInfo.leaderWidth = activeAttrs.getLeaderWidth();
        obb.frameInfo.cornerRadius = activeAttrs.getCornerRadius();
    }

    /**
     * Creates the balloon's frame vertex buffer according to the active attributes.
     *
     * @return a buffer containing the frame's x and y locations.
     */
    protected FloatBuffer createFrameVertices(OrderedBrowserBalloon obb)
    {
        BalloonAttributes activeAttrs = this.getActiveAttributes();

        if (AVKey.SHAPE_NONE.equals(activeAttrs.getBalloonShape()))
            return this.makeDefaultFrameVertices(obb);

        else if (AVKey.SHAPE_ELLIPSE.equals(activeAttrs.getBalloonShape()))
            return this.makeEllipseFrameVertices(obb);

        else // Default to AVKey.SHAPE_RECTANGLE
            return this.makeRectangleFrameVertices(obb);
    }

    protected FloatBuffer makeDefaultFrameVertices(OrderedBrowserBalloon obb)
    {
        BalloonAttributes activeAttrs = this.getActiveAttributes();
        GeometryBuilder gb = new GeometryBuilder();

        int x = obb.webViewRect.x - obb.screenRect.x;
        int y = obb.webViewRect.y - obb.screenRect.y;

        // Return a rectangle that represents the WebView's screen rectangle.
        if (AVKey.SHAPE_TRIANGLE.equals(activeAttrs.getLeaderShape()))
        {
            // The balloon's leader location is equivalent to its screen offset because the screen offset specifies the
            // location of the screen reference point relative to the frame, and the leader points from the frame to the
            // screen reference point.
            return gb.makeRectangleWithLeader(x, y, obb.webViewRect.width, obb.webViewRect.height,
                this.screenOffset.x, this.screenOffset.y, activeAttrs.getLeaderWidth());
        }
        else // Default to AVKey.SHAPE_NONE
        {
            return gb.makeRectangle(x, y, obb.webViewRect.width, obb.webViewRect.height);
        }
    }

    protected FloatBuffer makeEllipseFrameVertices(OrderedBrowserBalloon obb)
    {
        BalloonAttributes activeAttrs = this.getActiveAttributes();
        GeometryBuilder gb = new GeometryBuilder();

        int x = obb.screenRect.width / 2;
        int y = obb.screenRect.height / 2;
        int majorRadius = obb.screenRect.width / 2;
        int minorRadius = obb.screenRect.height / 2;

        // Return an ellipse centered at the balloon's center and with major and minor axes equal to the balloon's
        // width and height, respectively. We use integer coordinates for the center and the radii to ensure that
        // these vertices align image texels exactly with screen pixels when used as texture coordinates.
        if (AVKey.SHAPE_TRIANGLE.equals(activeAttrs.getLeaderShape()))
        {
            // The balloon's leader location is equivalent to its screen offset because the screen offset specifies the
            // location of the screen reference point relative to the frame, and the leader points from the frame to the
            // screen reference point.
            return gb.makeEllipseWithLeader(x, y, majorRadius, minorRadius, FRAME_GEOMETRY_ELLIPSE_SLICES,
                this.screenOffset.x, this.screenOffset.y, activeAttrs.getLeaderWidth());
        }
        else // Default to AVKey.SHAPE_NONE
        {
            return gb.makeEllipse(x, y, majorRadius, minorRadius, FRAME_GEOMETRY_ELLIPSE_SLICES);
        }
    }

    protected FloatBuffer makeRectangleFrameVertices(OrderedBrowserBalloon obb)
    {
        BalloonAttributes activeAttrs = this.getActiveAttributes();
        GeometryBuilder gb = new GeometryBuilder();

        // Return a rectangle that represents the balloon's screen rectangle, with optional rounded corners.
        if (AVKey.SHAPE_TRIANGLE.equals(activeAttrs.getLeaderShape()))
        {
            // The balloon's leader location is equivalent to its screen offset because the screen offset specifies the
            // location of the screen reference point relative to the frame, and the leader points from the frame to the
            // screen reference point.
            return gb.makeRectangleWithLeader(0, 0, obb.screenRect.width, obb.screenRect.height,
                activeAttrs.getCornerRadius(), FRAME_GEOMETRY_RECTANGLE_CORNER_SLICES, this.screenOffset.x,
                this.screenOffset.y, activeAttrs.getLeaderWidth());
        }
        else // Default to AVKey.SHAPE_NONE
        {
            return gb.makeRectangle(0, 0, obb.screenRect.width, obb.screenRect.height, activeAttrs.getCornerRadius(),
                FRAME_GEOMETRY_RECTANGLE_CORNER_SLICES);
        }
    }

    /**
     * Determines whether the balloon intersects the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return <code>true</code> If the balloon intersects the frustum, otherwise <code>false</code>.
     */
    protected boolean intersectsFrustum(DrawContext dc, OrderedBrowserBalloon obb)
    {
        // During picking, use the balloon's pickable screen extent. This extent includes this balloon's outline where
        // it exceeds the screen extent.
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(obb.screenPickExtent);

        return dc.getView().getViewport().intersects(obb.screenExtent);
    }

    protected void drawOrderedRenderable(DrawContext dc, OrderedBrowserBalloon obb)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc, obb);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attrMask =
            GL2.GL_COLOR_BUFFER_BIT // For alpha enable, blend enable, alpha func, blend func.
                | GL2.GL_CURRENT_BIT // For current color
                | GL2.GL_DEPTH_BUFFER_BIT // For depth test enable/disable, depth func, depth mask.
                | GL2.GL_LINE_BIT // For line smooth enable, line stipple enable, line width, line stipple factor,
                // line stipple pattern.
                | GL2.GL_POLYGON_BIT // For polygon mode.
                | GL2.GL_VIEWPORT_BIT; // For depth range.

        this.osh.clear(); // Reset the stack handler's internal state.
        this.osh.pushAttrib(gl, attrMask);
        this.osh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT); // For vertex array enable, pointers.
        this.osh.pushProjectionIdentity(gl);
        // The browser balloon is drawn using a parallel projection sized to fit the viewport.
        gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);
        this.osh.pushTextureIdentity(gl);
        this.osh.pushModelviewIdentity(gl);

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY); // All drawing uses vertex arrays.

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND); // Enable interior and outline alpha blending when not picking.
            OGLUtil.applyBlending(gl, false);
        }
    }

    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.osh.pop(gl);
    }

    protected void doDrawOrderedRenderable(DrawContext dc, OrderedBrowserBalloon obb)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            // Set up the pick color used during interior and outline rendering.
            Color pickColor = dc.getUniquePickColor();
            this.pickSupport.addPickableObject(this.createPickedObject(dc, pickColor));
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        // Translate to the balloon's screen origin. Use integer coordinates to ensure that the WebView texels are
        // aligned exactly with screen pixels.
        gl.glTranslatef(obb.screenRect.x, obb.screenRect.y, 0);

        if (!dc.isDeepPickingEnabled())
            this.setupDepthTest(dc, obb);

        // Draw the balloon frame geometry. This draws the WebView as a texture applied to the balloon frame's interior.
        this.drawFrame(dc, obb);

        if (this.isDrawTitleBar(dc))
            this.drawTitleBar(dc, obb);

        if (this.isDrawResizeControl(dc))
            this.drawResizeControl(dc, obb);

        if (this.isDrawBrowserControls(dc))
            this.drawBrowserControls(dc, obb);

        // We draw the links last to ensure that their picked objects are on top. We do this to ensure that link picking
        // is consistent with mouse events sent to the WebView. Currently, all select events that occur in this balloon
        // are send to the WebView. We want link pick areas to be on top to ensure that the application has a chance to
        // veto any link click select events before they are sent to the WebView.
        if (this.isDrawLinks(dc))
            this.drawLinks(dc, obb);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        PickedObject po = new PickedObject(pickColor.getRGB(),
            this.getDelegateOwner() != null ? this.getDelegateOwner() : this);

        // Attach the balloon to the picked object's AVList under the key HOT_SPOT. The application can then find that
        // the balloon is a HotSpot by looking in the picked object's AVList. This is critical when the delegate owner
        // is not null because the balloon is no longer the picked object. This would otherwise prevent the application
        // from interacting with the balloon via the HotSpot interface.
        po.setValue(AVKey.HOT_SPOT, this);

        return po;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected PickedObject createLinkPickedObject(DrawContext dc, Color pickColor, AVList linkParams)
    {
        PickedObject po = new PickedObject(pickColor.getRGB(), this);

        // Apply all of the link parameters to the picked object. This provides the application with the link's URL,
        // mime type, and target.
        po.setValues(linkParams);

        // Attach the balloon's context to the picked object to provide context for link clicked events. This supports
        // KML features that specify links with relative paths or fragments to the parent KML root.
        po.setValue(AVKey.CONTEXT, this.getValue(AVKey.CONTEXT));

        return po;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isDrawInterior(DrawContext dc)
    {
        return this.getActiveAttributes().isDrawInterior() && this.getActiveAttributes().getInteriorOpacity() > 0;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isDrawOutline(DrawContext dc)
    {
        return this.getActiveAttributes().isDrawOutline() && this.getActiveAttributes().getOutlineOpacity() > 0;
    }

    protected boolean isDrawTitleBar(DrawContext dc)
    {
        return this.isDrawTitleBar() && this.isDrawInterior(dc) && !dc.isPickingMode();
    }

    protected boolean isDrawResizeControl(DrawContext dc)
    {
        // There is no visible control so only proceed in picking mode.
        return this.isDrawResizeControl() && (this.isDrawInterior(dc) || this.isDrawOutline(dc)) && dc.isPickingMode();
    }

    protected boolean isDrawBrowserControls(DrawContext dc)
    {
        return this.isDrawBrowserControls() && this.isDrawInterior(dc);
    }

    protected boolean isDrawLinks(DrawContext dc)
    {
        return this.isDrawInterior(dc) && dc.isPickingMode();
    }

    protected void drawFrame(DrawContext dc, OrderedBrowserBalloon obb)
    {
        if (obb.frameInfo.vertexBuffer == null) // This should never happen, but we check anyway.
            return;

        // Bind the balloon's vertex buffer as source of GL vertex coordinates. This buffer is used by both interior
        // and outline rendering. We bind it once here to avoid loading the buffer twice.
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, obb.frameInfo.vertexBuffer);

        if (this.isDrawInterior(dc))
        {
            this.prepareToDrawInterior(dc);
            this.drawFrameInterior(dc, obb);
        }

        if (this.isDrawOutline(dc))
        {
            this.prepareToDrawOutline(dc);
            this.drawFrameOutline(dc, obb);
        }
    }

    /**
     * Draws this browser balloon's interior geometry in screen-coordinates, with the <code>WebView's</code> texture
     * representation applied as an OpenGL decal. OpenGL's texture decal mode uses the texture color where the texture's
     * alpha is 1, and uses the balloon's background color where it's 0. The texture's internal format must be RGBA to
     * work correctly, and this assumes that the WebView's texture format is RGBA.
     * <p/>
     * If the <code>WebView's</code> texture cannot be created or cannot be applied for any reason, this displays the
     * balloon's interior geometry in the balloon's background color without applying the <code>WebView's</code>
     * texture.
     * <p/>
     * If the specified draw context is in picking mode, this draws the balloon's interior geometry in the current color
     * and does not apply the <code>WebView's</code> texture.
     *
     * @param dc the current draw context.
     */
    protected void drawFrameInterior(DrawContext dc, OrderedBrowserBalloon obb)
    {
        if (obb.frameInfo.vertexBuffer == null) // This should never happen, but we check anyway.
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        boolean textureApplied = false;
        try
        {
            // Bind the WebView's texture representation as the current texture source if we're in normal rendering
            // mode. This also configures the texture matrix to transform texture coordinates from the balloon's vertex
            // coordinates to the WebView's screen rectangle. For this reason we use the balloon's vertex coordinates as
            // its texture coordinates.
            if (!dc.isPickingMode() && this.bindWebViewTexture(dc, obb))
            {
                // The WebView's texture is successfully bound. Enable GL texturing and set up the texture
                // environment to apply the texture in decal mode. Decal mode uses the texture color where the
                // texture's alpha is 1, and uses the balloon's background color where it's 0. The texture's
                // internal format must be RGBA to work correctly, and we assume that the WebView's texture format
                // is RGBA.
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, obb.frameInfo.vertexBuffer);
                // Denote that the texture has been applied and that we need to restore the default texture state.
                textureApplied = true;
            }

            // Draw the balloon's geometry as a triangle fan to display the interior. The balloon's vertices are 
            // represented by (x,y) pairs in screen coordinates. The number of vertices to draw is computed by dividing
            // the number of coordinates by 2, because each vertex has exactly two coordinates: x and y.
            gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, obb.frameInfo.vertexBuffer.remaining() / 2);
        }
        finally
        {
            // Restore the previous texture state and client array state. We do this to avoid pushing and popping the
            // texture attribute bit, which is expensive. We disable textures, disable texture coordinate arrays, bind
            // texture id 0, set the default texture environment mode, and and set the texture coord pointer to 0.
            if (textureApplied)
            {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, null);
            }
        }
    }

    protected void drawFrameOutline(DrawContext dc, OrderedBrowserBalloon obb)
    {
        if (obb.frameInfo.vertexBuffer == null) // This should never happen, but we check anyway.
            return;

        // Draw the balloon's geometry as a line loop to display the outline. The balloon's vertices are in screen
        // coordinates.
        dc.getGL().glDrawArrays(GL.GL_LINE_LOOP, 0, obb.frameInfo.vertexBuffer.remaining() / 2);
    }

    protected void prepareToDrawInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            // Apply the balloon's background color and opacity if we're in normal rendering mode.
            Color color = this.getActiveAttributes().getInteriorMaterial().getDiffuse();
            double opacity = this.getActiveAttributes().getInteriorOpacity();
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
        }
    }

    protected void prepareToDrawOutline(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            // Apply the balloon's outline color and opacity and apply the balloon's normal outline width if we're in
            // normal rendering mode.
            Color color = this.getActiveAttributes().getOutlineMaterial().getDiffuse();
            double opacity = this.getActiveAttributes().getOutlineOpacity();
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

            // Apply line smoothing if we're in normal rendering mode.
            if (this.getActiveAttributes().isEnableAntialiasing())
            {
                gl.glEnable(GL.GL_LINE_SMOOTH);
            }

            if (this.getActiveAttributes().getOutlineStippleFactor() > 0)
            {
                gl.glEnable(GL2.GL_LINE_STIPPLE);
                gl.glLineStipple(this.getActiveAttributes().getOutlineStippleFactor(),
                    this.getActiveAttributes().getOutlineStipplePattern());
            }
        }

        // Apply the balloon's outline width. Use the outline pick width if we're in picking mode and the pick width is
        // greater than the normal line width. Otherwise use the normal line width.
        if (dc.isPickingMode())
            gl.glLineWidth((float) this.computeOutlinePickWidth());
        else
            gl.glLineWidth((float) this.getActiveAttributes().getOutlineWidth());
    }

    protected Rectangle computeFramePickRect(Rectangle frameRect)
    {
        double outlinePickWidth = this.computeOutlinePickWidth();
        return new Rectangle(
            frameRect.x - (int) outlinePickWidth / 2,
            frameRect.y - (int) outlinePickWidth / 2,
            frameRect.width + (int) outlinePickWidth,
            frameRect.height + (int) outlinePickWidth);
    }

    /**
     * Computes the line width to use during picking (in pixels). Returns the larger of this balloon's outline width and
     * its <code>outlinePickWidth</code>.
     *
     * @return the line width to use during picking, in pixels.
     */
    protected double computeOutlinePickWidth()
    {
        return Math.max(this.getActiveAttributes().getOutlineWidth(), this.getOutlinePickWidth());
    }

    protected void updateWebView(DrawContext dc, OrderedBrowserBalloon obb)
    {
        // Attempt to create the balloon's WebView.
        if (this.webView == null)
        {
            this.makeWebView(dc, obb.webViewRect.getSize());

            // Exit immediately if WebView creation failed.
            if (this.webView == null)
                return;
        }

        // The WebView's frame size and background color can change each frame. Synchronize the WebView's background
        // color and frame size with the desired values before attempting to use the WebView's texture. The WebView
        // avoids doing unnecessary work when the same frame size or background color is specified.
        this.webView.setFrameSize(obb.webViewRect.getSize());
        this.webView.setBackgroundColor(this.getActiveAttributes().getInteriorMaterial().getDiffuse());

        // Update the WebView's text content each time the balloon's decoded string changes. We update the text even if
        // the user has navigated to a page other than the balloon's text content. This ensures that any changes the
        // application makes to the decoded text are reflected in the browser balloon's content. If we ignore those
        // changes when the user navigates to another page, the application cannot retain control over the balloon's
        // content.
        if (this.getTextDecoder().getLastUpdateTime() != this.textUpdateTime)
        {
            this.setWebViewContent();
            this.textUpdateTime = this.getTextDecoder().getLastUpdateTime();
        }
    }

    protected void setWebViewContent()
    {
        String text = this.getTextDecoder().getDecodedText();
        Object resourceResolver = this.getResourceResolver();

        if (resourceResolver instanceof WebResourceResolver)
        {
            this.webView.setHTMLString(text, (WebResourceResolver) resourceResolver);
        }
        else if (resourceResolver instanceof URL)
        {
            this.webView.setHTMLString(text, (URL) resourceResolver);
        }
        else if (resourceResolver instanceof String)
        {
            // If the string is not a valid URL, then makeURL returns null and the WebView treats any relative paths as
            // unresolved references.
            URL url = WWIO.makeURL((String) resourceResolver);

            if (url == null)
            {
                Logging.logger().warning(Logging.getMessage("generic.URIInvalid", resourceResolver));
            }

            this.webView.setHTMLString(text, url);
        }
        else
        {
            if (resourceResolver != null)
            {
                Logging.logger().warning(Logging.getMessage("generic.UnrecognizedResourceResolver", resourceResolver));
            }

            this.webView.setHTMLString(text);
        }
    }

    protected void makeWebView(DrawContext dc, Dimension frameSize)
    {
        if (this.webView != null || this.webViewCreationFailed)
            return;

        try
        {
            // Attempt to get the WebViewFactory class name from configuration. Fall back on the BrowserBalloon's
            // default factory if the configuration does not specify a one.
            String className = Configuration.getStringValue(AVKey.WEB_VIEW_FACTORY, DEFAULT_WEB_VIEW_FACTORY);
            WebViewFactory factory = (WebViewFactory) WorldWind.createComponent(className);
            this.webView = factory.createWebView(frameSize);
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("WebView.ExceptionCreatingWebView", t);
            Logging.logger().severe(message);

            dc.addRenderingException(t);

            // Set flag to prevent retrying the web view creation. We assume that if this fails once it will continue to
            // fail.
            this.webViewCreationFailed = true;
        }

        // Configure the balloon to forward the WebView's property change events to its listeners.
        if (this.webView != null)
            this.webView.addPropertyChangeListener(this);
    }

    protected void disposeWebView()
    {
        if (this.webView == null)
            return;

        this.webView.removePropertyChangeListener(this);
        this.webView.dispose();
        this.webView = null;
        this.textUpdateTime = -1;
        this.webViewContentSize = null;
    }

    protected void determineWebViewContentSize()
    {
        // Update the WebView's HTML content size when the WebView is non-null and has not navigated to a another page
        // (indicated by a non-null URL). The latter case indicates that the WebView is not displaying this balloon's
        // text. We avoid updating the content size in this case to ensure that the balloon's size always fits the
        // balloon's text, and not content the user navigates to. Fitting the balloon's size to the WebView's current
        // content causes the balloon to abruptly change size as the user navigates. Note that the content size may be
        // null or (0, 0), indicating that the WebView does not know its content size. The balloon handles this by
        // falling back to a default content size.
        if (this.webView != null && this.webView.getContentURL() == null)
        {
            this.webViewContentSize = this.webView.getContentSize();
        }
    }

    protected Rectangle computeWebViewRectForFrameRect(BalloonAttributes activeAttrs, Rectangle frameRect)
    {
        // Compute the WebView rectangle as an inset of the balloon's screen rectangle, given the current inset values.
        Insets insets = activeAttrs.getInsets();
        return new Rectangle(
            frameRect.x + insets.left,
            frameRect.y + insets.bottom,
            frameRect.width - (insets.left + insets.right),
            frameRect.height - (insets.bottom + insets.top));
    }

    protected Rectangle computeFrameRectForWebViewRect(BalloonAttributes activeAttrs, Rectangle webViewRect)
    {
        Insets insets = activeAttrs.getInsets();
        return new Rectangle(
            webViewRect.x - insets.left,
            webViewRect.y - insets.bottom,
            webViewRect.width + (insets.left + insets.right),
            webViewRect.height + (insets.bottom + insets.top));
    }

    protected boolean bindWebViewTexture(DrawContext dc, OrderedBrowserBalloon obb)
    {
        if (this.webView == null)
            return false;

        WWTexture texture = this.webView.getTextureRepresentation(dc);
        if (texture == null)
            return false;

        if (!texture.bind(dc))
            return false;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Set up the texture matrix to transform texture coordinates from the balloon's screen space vertex
        // coordinates into WebView texture space. This places the WebView's texture in the WebView's screen
        // rectangle. Use integer coordinates when possible to ensure that the image texels are aligned
        // exactly with screen pixels. This transforms texture coordinates such that
        // (webViewRect.getMinX(), webViewRect.getMinY()) maps to (0, 0) - the texture's lower left corner,
        // and (webViewRect.getMaxX(), webViewRect.getMaxY()) maps to (1, 1) - the texture's upper right
        // corner. Since texture coordinates are generated relative to the screenRect origin and webViewRect
        // is in screen coordinates, we translate the texture coordinates by the offset from the screenRect
        // origin to the webViewRect origin.
        texture.applyInternalTransform(dc);
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glScalef(1f / obb.webViewRect.width, 1f / obb.webViewRect.height, 1f);
        gl.glTranslatef(obb.screenRect.x - obb.webViewRect.x, obb.screenRect.y - obb.webViewRect.y, 0f);
        // Restore the matrix mode.
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        return true;
    }

    protected void drawWebViewLinks(DrawContext dc, OrderedBrowserBalloon obb)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (this.webView == null)
            return;

        Iterable<AVList> links = this.webView.getLinks();
        if (links == null)
            return;

        for (AVList linkParams : links)
        {
            // This should never happen, but we check anyway.
            if (linkParams == null)
                continue;

            // Ignore any links that have no bounds or no rectangles; they cannot be drawn.
            if (linkParams.getValue(AVKey.BOUNDS) == null || linkParams.getValue(AVKey.RECTANGLES) == null)
                continue;

            // Translate the bounds from WebView coordinates to World Window screen coordinates.
            Rectangle bounds = new Rectangle((Rectangle) linkParams.getValue(AVKey.BOUNDS));
            bounds.translate(obb.webViewRect.x, obb.webViewRect.y);

            // Ignore link rectangles that do not intersect any of the current pick rectangles.
            if (!dc.getPickFrustums().intersectsAny(bounds))
                continue;

            Color pickColor = dc.getUniquePickColor();
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
            this.pickSupport.addPickableObject(this.createLinkPickedObject(dc, pickColor, linkParams));

            int x = obb.webViewRect.x - obb.screenRect.x;
            int y = obb.webViewRect.y - obb.screenRect.y;

            gl.glBegin(GL2.GL_QUADS);
            try
            {
                for (Rectangle rect : (Rectangle[]) linkParams.getValue(AVKey.RECTANGLES))
                {
                    // This should never happen, but we check anyway.
                    if (rect == null)
                        continue;

                    gl.glVertex2i(x + rect.x, y + rect.y);
                    gl.glVertex2i(x + rect.x + rect.width, y + rect.y);
                    gl.glVertex2i(x + rect.x + rect.width, y + rect.y + rect.height);
                    gl.glVertex2i(x + rect.x, y + rect.y + rect.height);
                }
            }
            finally
            {
                gl.glEnd();
            }
        }
    }

    /**
     * Draw pickable regions for the resize controls. A pickable region is drawn along the frame outline.
     *
     * @param dc Draw context.
     */
    protected void drawResizeControl(DrawContext dc, OrderedBrowserBalloon obb)
    {
        if (obb.frameInfo.vertexBuffer == null) // This should never happen, but we check anyway.
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Compute the screen rectangle in AWT coordinates (origin top left).
        Rectangle awtScreenRect = new Rectangle(obb.screenRect.x,
            dc.getView().getViewport().height - obb.screenRect.y - obb.screenRect.height,
            obb.screenRect.width, obb.screenRect.height);

        Color color = dc.getUniquePickColor();
        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

        // Set ACTION of PickedObject to RESIZE. Attach current bounds to the picked object so that the resize
        // controller will have enough information to interpret mouse drag events.
        PickedObject po = new PickedObject(color.getRGB(), this);
        po.setValue(AVKey.ACTION, AVKey.RESIZE);
        po.setValue(AVKey.BOUNDS, awtScreenRect);
        this.pickSupport.addPickableObject(po);

        gl.glLineWidth((float) this.computeOutlinePickWidth());
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, obb.frameInfo.vertexBuffer);
        gl.glDrawArrays(GL.GL_LINE_LOOP, 0, obb.frameInfo.vertexBuffer.remaining() / 2);
    }

    protected void drawBrowserControls(DrawContext dc, OrderedBrowserBalloon obb)
    {
        for (BrowserControl control : this.getBrowserControls())
        {
            if (control == null) // This should never happen, but we check anyway.
                continue;

            if (!control.isVisible())
                continue;

            try
            {
                this.drawBrowserControl(dc, control, obb);
            }
            catch (Exception e)
            {
                Logging.logger().severe(Logging.getMessage("generic.ExceptionWhileRenderingBrowserControl", control));
            }
        }
    }

    protected void drawBrowserControl(DrawContext dc, BrowserControl control, OrderedBrowserBalloon obb)
    {
        WWTexture texture = control.getTexture();
        if (texture == null)
            return;

        Point2D offset = control.getOffset().computeOffset(obb.screenRect.width, obb.screenRect.height, 1d, 1d);
        Dimension size = control.getSize().compute(texture.getWidth(dc), texture.getHeight(dc),
            obb.screenRect.width, obb.screenRect.height);

        Rectangle rect = new Rectangle(obb.screenRect.x + (int) offset.getX(), obb.screenRect.y + (int) offset.getY(),
            size.width, size.height);
        if (rect.isEmpty())
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushTextureIdentity(gl);
        ogsh.pushModelviewIdentity(gl);
        try
        {
            gl.glTranslated(rect.x, rect.y, 0);
            gl.glScaled(rect.width, rect.height, 1);

            if (dc.isPickingMode())
            {
                Color pickColor = dc.getUniquePickColor();
                gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());

                // Populate the picked object's AVList with all key-value pairs attached to the BrowserControl. This
                // typically includes the ACTION key used to represent the action associated with the control. We attach
                // the balloon as the picked object, because the application's balloon controller depends on the balloon
                // to execute the appropriate action.
                PickedObject po = new PickedObject(pickColor.getRGB(), this);
                po.setValues(control);
                this.pickSupport.addPickableObject(po);

                dc.drawUnitQuad();
            }
            else
            {
                // Determine the control's active color: either the highlight color or the normal color, depending on
                // whether a pick point is over the control.
                Color color;
                if (dc.getPickFrustums().intersectsAny(rect))
                {
                    color = control.getHighlightColor() != null ? control.getHighlightColor()
                        : BrowserControl.DEFAULT_HIGHLIGHT_COLOR;
                }
                else
                {
                    color = control.getColor() != null ? control.getColor() : BrowserControl.DEFAULT_COLOR;
                }

                float[] compArray = new float[4];
                color.getRGBComponents(compArray);

                // Multiply the color's opacity by the balloon's interior opacity so that controls maintain the same
                // relative opacity to the balloon's interior.
                float alpha = compArray[3] * (float) this.getActiveAttributes().getInteriorOpacity();
                if (alpha > 1f)
                    alpha = 1f;

                // Apply the control's color and enable blending in premultiplied alpha mode. We must enable use the
                // correct blending function for premultiplied alpha colors, because textures loaded by JOGL contain
                // premultiplied alpha colors.
                OGLUtil.applyBlending(gl, true);
                gl.glColor4f(compArray[0] * alpha, compArray[1] * alpha, compArray[2] * alpha, alpha);

                gl.glEnable(GL.GL_TEXTURE_2D);
                if (texture.bind(dc))
                {
                    dc.drawUnitQuad(texture.getTexCoords());
                }
            }
        }
        finally
        {
            ogsh.pop(gl);
            // Restore the previous texture state. We do this to avoid pushing and popping the texture attribute bit,
            // which is expensive. We disable textures and bind texture id 0.
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    protected void drawTitleBar(DrawContext dc, OrderedBrowserBalloon obb)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Apply the balloon's outline color, but use the interior opacity.
        Color color = this.getActiveAttributes().getOutlineMaterial().getDiffuse();
        double opacity = this.getActiveAttributes().getInteriorOpacity();
        gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
            (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

        // Disable line smoothing and specify a line with of 1.0. This ensures that the title separator line appears
        // sharp and thin.
        gl.glDisable(GL.GL_LINE_SMOOTH);
        gl.glLineWidth(1f);

        int x = obb.webViewRect.x - obb.screenRect.x;
        int y = obb.webViewRect.y - obb.screenRect.y;

        gl.glBegin(GL2.GL_LINES);
        try
        {
            gl.glVertex2i(x, y + obb.webViewRect.height);
            gl.glVertex2i(x + obb.webViewRect.width, y + obb.webViewRect.height);
        }
        finally
        {
            gl.glEnd();
        }
    }

    protected void drawLinks(DrawContext dc, OrderedBrowserBalloon obb)
    {
        this.drawWebViewLinks(dc, obb);
    }

    /** {@inheritDoc} */
    public void setActive(boolean active)
    {
        if (this.webView != null)
            this.webView.setActive(active);
    }

    /** {@inheritDoc} */
    public boolean isActive()
    {
        return (this.webView != null) && this.webView.isActive();
    }

    /**
     * Forwards the <code>MouseEvent</code> associated with the specified <code>event</code> to the balloon's internal
     * <code>WebView</code>. This does not consume the event, because the <code>{@link
     * gov.nasa.worldwind.event.InputHandler}</code> implements the policy for consuming or forwarding mouse clicked
     * events to other objects.
     *
     * @param event The event to handle.
     */
    public void selected(SelectEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleSelectEvent(event);
    }

    /**
     * Forwards the key typed event to the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>
     * and consumes the event. This consumes the event so the <code>{@link gov.nasa.worldwind.View}</code> doesn't
     * respond to it.
     *
     * @param event The event to forward.
     */
    public void keyTyped(KeyEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleKeyEvent(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Forwards the key pressed event to the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>
     * and consumes the event. This consumes the event so the <code>{@link gov.nasa.worldwind.View}</code> doesn't
     * respond to it. The
     *
     * @param event The event to forward.
     */
    public void keyPressed(KeyEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleKeyEvent(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Forwards the key released event to the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>
     * and consumes the event. This consumes the event so the <code>{@link gov.nasa.worldwind.View}</code> doesn't
     * respond to it.
     *
     * @param event The event to forward.
     */
    public void keyReleased(KeyEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleKeyEvent(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Does nothing; BrowserBalloon handles mouse clicked events in <code>selected</code>.
     *
     * @param event The event to handle.
     */
    public void mouseClicked(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon handles mouse pressed events in <code>selected</code>.
     *
     * @param event The event to handle.
     */
    public void mousePressed(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon handles mouse released events in <code>selected</code>.
     *
     * @param event The event to handle.
     */
    public void mouseReleased(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon does not handle mouse entered events.
     *
     * @param event The event to handle.
     */
    public void mouseEntered(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon does not handle mouse exited events.
     *
     * @param event The event to handle.
     */
    public void mouseExited(MouseEvent event)
    {
    }

    /**
     * Does nothing; BrowserBalloon handles mouse dragged events in <code>selected</code>.
     *
     * @param event The event to handle.
     */
    public void mouseDragged(MouseEvent event)
    {
    }

    /**
     * Forwards the mouse moved event to the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>.
     * This does not consume the event, because the <code>{@link gov.nasa.worldwind.event.InputHandler}</code>
     * implements the policy for consuming or forwarding mouse moved events to other objects.
     * <p/>
     * Unlike mouse clicked, mouse pressed, and mouse dragged events, mouse move events cannot be forwarded to the
     * WebView via SelectEvents in <code>selected</code>, because mouse movement events are not selection events.
     *
     * @param event The event to forward.
     */
    public void mouseMoved(MouseEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleMouseEvent(event);
    }

    /**
     * Forwards the mouse wheel event to the balloon's internal <code>{@link gov.nasa.worldwind.util.webview.WebView}</code>
     * and consumes the event. This consumes the event so the <code>{@link gov.nasa.worldwind.View}</code> doesn't
     * respond to it.
     * <p/>
     * Unlike mouse clicked, mouse pressed, and mouse dragged events, mouse wheel events cannot be forwarded to the
     * WebView via SelectEvents in <code>selected</code>, because mouse wheel events are not selection events.
     *
     * @param event The event to forward.
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
        if (event == null || event.isConsumed())
            return;

        this.handleMouseEvent(event);
        event.consume(); // Consume the event so the View doesn't respond to it.
    }

    /**
     * Returns a <code>null</code> Cursor, indicating the default cursor should be used when the BrowserBalloon is
     * active. The Cursor is set by the <code>{@link gov.nasa.worldwind.util.webview.WebView}</code> in response to
     * mouse moved events.
     *
     * @return A <code>null</code> Cursor.
     */
    public Cursor getCursor()
    {
        return null;
    }

    /**
     * Sends the specified <code>SelectEvent</code> to the balloon's internal <code>WebView</code>. The event's
     * <code>pickPoint</code> is converted from AWT coordinates to the WebView's local coordinate system.
     *
     * @param event the event to send.
     */
    protected void handleSelectEvent(SelectEvent event)
    {
        if (this.webView == null)
            return;

        // Ignore box selection events. These currently have no mapping to a WebView internal event.
        if (event.isBoxSelect())
            return;

        // Convert the mouse event's screen point to the WebView's local coordinate system. Note that we send the mouse
        // event to the WebView even when its screen point is outside the WebView's bounding rectangle. This gives the
        // WebView a chance to change its state or the cursor's state when the cursor it exits the WebView.
        Point pickPoint = event.getPickPoint();

        // The SelectEvent's pick point is null if its a drag end event. In this case, use pick point of the last
        // SelectEvent we received, which should be a drag event with a non-null pick point.
        if (pickPoint == null)
            pickPoint = this.lastPickPoint;

        // If the last SelectEvent's pick point is null and the current SelectEvent's pick point is null, then we cannot
        // send this event to the WebView.
        if (pickPoint == null)
            return;

        Point webViewPoint = this.convertToWebView(event.getSource(), pickPoint);

        if (event.isLeftPress() || event.isRightPress())
        {
            int modifiers = event.isLeftPress() ? MouseEvent.BUTTON1_DOWN_MASK : MouseEvent.BUTTON3_DOWN_MASK;
            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(), modifiers, // when, modifiers.
                    webViewPoint.x, webViewPoint.y, 0, // x, y, clickCount.
                    event.isRightPress(), // isPopupTrigger.
                    event.isRightPress() ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1));
        }
        else if (event.isLeftClick() || event.isRightClick() || event.isLeftDoubleClick())
        {
            int clickCount = event.isLeftDoubleClick() ? 2 : 1;
            int modifiers = (event.isLeftClick() || event.isLeftDoubleClick()) ? MouseEvent.BUTTON1_DOWN_MASK
                : MouseEvent.BUTTON3_DOWN_MASK;

            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), MouseEvent.MOUSE_RELEASED,
                    System.currentTimeMillis(), 0, // when, modifiers.
                    webViewPoint.x, webViewPoint.y, clickCount, // x, y, clickCount.
                    false, // isPopupTrigger.
                    event.isRightClick() ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1));
            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(), modifiers, // when, modifiers.
                    webViewPoint.x, webViewPoint.y, clickCount, // x, y, clickCount
                    false, // isPopupTrigger.
                    event.isRightClick() ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1));
        }
        else if (event.isDrag())
        {
            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), MouseEvent.MOUSE_DRAGGED,
                    System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK,  // when, modifiers.
                    webViewPoint.x, webViewPoint.y, 0, // x, y, clickCount.
                    false, // isPopupTrigger.
                    MouseEvent.BUTTON1));
        }
        else if (event.isDragEnd())
        {
            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), MouseEvent.MOUSE_RELEASED,
                    System.currentTimeMillis(), 0, // when, modifiers.
                    webViewPoint.x, webViewPoint.y, 0, // x, y, clickCount.
                    false, // isPopupTrigger.
                    MouseEvent.BUTTON1));
        }

        this.lastPickPoint = event.getPickPoint();

        // Consume the SelectEvent now that it has been passed on to the WebView as a mouse event. We avoid consuming
        // left press events, since doing so prevents the WorldWindow from gaining focus.
        if (!event.isLeftPress())
            event.consume();
    }

    /**
     * Sends the specified <code>KeyEvent</code> to the balloon's internal <code>WebView</code>.
     * <p/>
     * This does nothing if the balloon's internal <code>WebView</code> is uninitialized.
     *
     * @param event the event to send.
     */
    protected void handleKeyEvent(KeyEvent event)
    {
        if (this.webView != null)
            this.webView.sendEvent(event);
    }

    /**
     * Sends the specified <code>MouseEvent</code> to the balloon's internal <code>WebView</code>. The event's point is
     * converted from AWT coordinates to the WebView's local coordinate system.
     * <p/>
     * This does nothing if the balloon's internal <code>WebView</code> is uninitialized.
     *
     * @param event the event to send.
     */
    protected void handleMouseEvent(MouseEvent event)
    {
        if (this.webView == null)
            return;

        // Convert the mouse event's screen point to the WebView's local coordinate system. Note that we send the mouse
        // event to the WebView even when its screen point is outside the WebView's bounding rectangle. This gives the
        // WebView a chance to change its state or the cursor's state when the cursor it exits the WebView.
        Point webViewPoint = this.convertToWebView(event.getSource(), event.getPoint());

        // Send a copy of the mouse event using the point in the WebView's local coordinate system. 
        if (event instanceof MouseWheelEvent)
        {
            this.webView.sendEvent(
                new MouseWheelEvent((Component) event.getSource(), event.getID(), event.getWhen(), event.getModifiers(),
                    webViewPoint.x, webViewPoint.y, event.getClickCount(), event.isPopupTrigger(),
                    ((MouseWheelEvent) event).getScrollType(), ((MouseWheelEvent) event).getScrollAmount(),
                    ((MouseWheelEvent) event).getWheelRotation()));
        }
        else
        {
            this.webView.sendEvent(
                new MouseEvent((Component) event.getSource(), event.getID(), event.getWhen(), event.getModifiers(),
                    webViewPoint.x, webViewPoint.y, event.getClickCount(), event.isPopupTrigger(), event.getButton()));
        }
    }

    /**
     * Converts the specified screen point from AWT coordinates to local WebView coordinates.
     *
     * @param point   The point to convert.
     * @param context the component who's coordinate system the point is in.
     *
     * @return A new <code>Point</code> in the WebView's local coordinate system.
     */
    protected Point convertToWebView(Object context, Point point)
    {
        int x = point.x;
        int y = point.y;

        // Translate AWT coordinates to OpenGL screen coordinates by moving the Y origin from the upper left corner to
        // the lower left corner and flipping the direction of the Y axis.
        if (context instanceof Component)
            y = ((Component) context).getHeight() - point.y;

        // Find the ordered renderable that contains the point.
        Rectangle rect = null;
        for (OrderedBrowserBalloon obb : this.orderedRenderables.values())
        {
            rect = obb.webViewRect;
            if (x >= rect.x && x <= rect.x && y >= rect.y && y <= rect.y)
                break;
        }

        if (rect != null)
        {
            x -= rect.x;
            y -= rect.y;
        }

        return new Point(x, y);
    }
}
