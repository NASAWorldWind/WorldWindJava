/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.GL;
import java.awt.*;

/**
 * A <code>{@link gov.nasa.worldwind.render.ScreenBalloon}</code> that displays HTML, JavaScript, and Flash content
 * using the system's native browser, and who's origin is located at a point on the screen.
 *
 * @author pabercrombie
 * @version $Id: ScreenBrowserBalloon.java 2148 2014-07-14 16:27:49Z tgaskins $
 * @see gov.nasa.worldwind.render.AbstractBrowserBalloon
 * @deprecated 
 */
@Deprecated
public class ScreenBrowserBalloon extends AbstractBrowserBalloon implements ScreenBalloon
{
    /**
     * Indicates this balloon's screen location. The screen location's coordinate system has its origin in the upper
     * left corner of the <code>WorldWindow</code>, with the y-axis pointing right and the x-axis pointing down.
     * Initialized to a non-<code>null</code> value at construction.
     */
    protected Point screenLocation;

    /**
     * Constructs a new <code>ScreenBrowserBalloon</code> with the specified text content and screen location.
     *
     * @param text  the balloon's initial text content.
     * @param point the balloon's initial screen location, in AWT coordinates (origin at upper left corner of the
     *              <code>WorldWindow</code>).
     *
     * @throws IllegalArgumentException if either <code>text</code> or <code>point</code> are <code>null</code>.
     */
    public ScreenBrowserBalloon(String text, Point point)
    {
        super(text);

        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenLocation = point;
    }

    @Override
    protected OrderedBrowserBalloon createOrderedRenderable()
    {
        return new OrderedBrowserBalloon();
    }

    /** {@inheritDoc} */
    public Point getScreenLocation()
    {
        return this.screenLocation;
    }

    /** {@inheritDoc} */
    public void setScreenLocation(Point point)
    {
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenLocation = point;
    }

    /**
     * Computes and stores this balloon's screen coordinates. This assigns balloon coordinate properties as follows:
     * <ul> <li><code>screenOffset</code> - the balloon frame's screen-coordinate offset from this balloon's screen
     * location.</li> <li><code>screenRect</code> - the balloon frame's screen-coordinate rectangle.</li>
     * <li><code>screenExtent</code> - this balloon's screen-coordinate bounding rectangle.</li>
     * <li><code>screenPickExtent</code> - this balloon's screen-coordinate bounding rectangle, including area covered
     * by the balloon's pickable outline.</li> <li><code>webViewRect</code> - the WebView's screen-coordinate content
     * frame.</li> <li><code>eyeDistance</code> - always 0.</li></ul>
     *
     * @param dc the current draw context.
     */
    protected void computeBalloonPoints(DrawContext dc, OrderedBrowserBalloon obb)
    {
        this.screenOffset = null;
        obb.screenRect = null;
        obb.screenExtent = null;
        obb.screenPickExtent = null;
        obb.webViewRect = null;
        obb.eyeDistance = 0;

        BalloonAttributes activeAttrs = this.getActiveAttributes();
        Dimension size = this.computeSize(dc, activeAttrs);

        // Cache the screen offset computed from the active attributes.
        this.screenOffset = this.computeOffset(dc, activeAttrs, size.width, size.height);
        // Compute the screen rectangle given the current screen point, the current screen offset, and the current
        // screen size. Translate the screen y from AWT coordinates (origin at upper left) to GL coordinates (origin at
        // bottom left). Note: The screen offset denotes how to place the screen reference point relative to the frame.
        // For example, an offset of (-10, -10) in pixels places the reference point below and to the left of the frame.
        // Since the screen reference point is fixed, the frame appears to move relative to the reference point.
        int y = dc.getView().getViewport().height - this.screenLocation.y;
        obb.screenRect = new Rectangle(this.screenLocation.x - this.screenOffset.x, y - this.screenOffset.y,
            size.width, size.height);
        // Compute the screen extent as the rectangle containing the balloon's screen rectangle and its screen point.
        obb.screenExtent = new Rectangle(obb.screenRect);
        obb.screenExtent.add(this.screenLocation.x, y);
        // Compute the pickable screen extent as the screen extent, plus the width of the balloon's pickable outline.
        // This extent is used during picking to ensure that the balloon's outline is pickable when it exceeds the
        // balloon's screen extent.
        obb.screenPickExtent = this.computeFramePickRect(obb.screenExtent);
        // Compute the WebView rectangle as an inset of the screen rectangle, given the current inset values.
        obb.webViewRect = this.computeWebViewRectForFrameRect(activeAttrs, obb.screenRect);
        // The screen balloon has no eye distance; assign it to zero.
        obb.eyeDistance = 0;
    }

    /** {@inheritDoc} */
    protected void setupDepthTest(DrawContext dc, OrderedBrowserBalloon obb)
    {
        dc.getGL().glDisable(GL.GL_DEPTH_TEST);
    }
}
