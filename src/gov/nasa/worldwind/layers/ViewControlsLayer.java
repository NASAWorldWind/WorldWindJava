/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * This layer displays onscreen view controls. Controls are available for pan, zoom, heading, pitch, tilt, field-of-view
 * and vertical exaggeration. Each of the controls can be enabled or disabled independently.
 * <p>
 * An instance of this class depends on an instance of {@link ViewControlsSelectListener} to control it. The select
 * listener must be registered as such via {@link gov.nasa.worldwind.WorldWindow#addSelectListener(gov.nasa.worldwind.event.SelectListener)}.
 * <p>
 * <code>ViewControlsLayer</code> instances are not sharable among <code>WorldWindow</code>s.
 *
 * @author Patrick Murris
 * @version $Id: ViewControlsLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see ViewControlsSelectListener
 */
public class ViewControlsLayer extends RenderableLayer
{
    // The default images
    protected final static String IMAGE_PAN = "images/view-pan-64x64.png";
    protected final static String IMAGE_LOOK = "images/view-look-64x64.png";
    protected final static String IMAGE_HEADING_LEFT = "images/view-heading-left-32x32.png";
    protected final static String IMAGE_HEADING_RIGHT = "images/view-heading-right-32x32.png";
    protected final static String IMAGE_ZOOM_IN = "images/view-zoom-in-32x32.png";
    protected final static String IMAGE_ZOOM_OUT = "images/view-zoom-out-32x32.png";
    protected final static String IMAGE_PITCH_UP = "images/view-pitch-up-32x32.png";
    protected final static String IMAGE_PITCH_DOWN = "images/view-pitch-down-32x32.png";
    protected final static String IMAGE_FOV_NARROW = "images/view-fov-narrow-32x32.png";
    protected final static String IMAGE_FOV_WIDE = "images/view-fov-wide-32x32.png";
    protected final static String IMAGE_VE_UP = "images/view-elevation-up-32x32.png";
    protected final static String IMAGE_VE_DOWN = "images/view-elevation-down-32x32.png";

    // The annotations used to display the controls.
    protected ScreenAnnotation controlPan;
    protected ScreenAnnotation controlLook;
    protected ScreenAnnotation controlHeadingLeft;
    protected ScreenAnnotation controlHeadingRight;
    protected ScreenAnnotation controlZoomIn;
    protected ScreenAnnotation controlZoomOut;
    protected ScreenAnnotation controlPitchUp;
    protected ScreenAnnotation controlPitchDown;
    protected ScreenAnnotation controlFovNarrow;
    protected ScreenAnnotation controlFovWide;
    protected ScreenAnnotation controlVeUp;
    protected ScreenAnnotation controlVeDown;
    protected ScreenAnnotation currentControl;

    protected String position = AVKey.SOUTHWEST;
    protected String layout = AVKey.HORIZONTAL;
    protected Vec4 locationCenter = null;
    protected Vec4 locationOffset = null;
    protected double scale = 1;
    protected int borderWidth = 20;
    protected int buttonSize = 32;
    protected int panSize = 64;
    protected boolean initialized = false;
    protected Rectangle referenceViewport;

    protected boolean showPanControls = true;
    protected boolean showLookControls = false;
    protected boolean showZoomControls = true;
    protected boolean showHeadingControls = true;
    protected boolean showPitchControls = true;
    protected boolean showFovControls = false;
    protected boolean showVeControls = true;

    public int getBorderWidth()
    {
        return this.borderWidth;
    }

    /**
     * Sets the view controls offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the view controls from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
        clearControls();
    }

    /**
     * Get the controls display scale.
     *
     * @return the controls display scale.
     */
    public double getScale()
    {
        return this.scale;
    }

    /**
     * Set the controls display scale.
     *
     * @param scale the controls display scale.
     */
    public void setScale(double scale)
    {
        this.scale = scale;
        clearControls();
    }

    protected int getButtonSize()
    {
        return buttonSize;
    }

    protected void setButtonSize(int buttonSize)
    {
        this.buttonSize = buttonSize;
        clearControls();
    }

    protected int getPanSize()
    {
        return panSize;
    }

    protected void setPanSize(int panSize)
    {
        this.panSize = panSize;
        clearControls();
    }

    /**
     * Returns the current relative view controls position.
     *
     * @return the current view controls position.
     */
    public String getPosition()
    {
        return this.position;
    }

    /**
     * Sets the relative viewport location to display the view controls. Can be one of {@link AVKey#NORTHEAST}, {@link
     * AVKey#NORTHWEST}, {@link AVKey#SOUTHEAST}, or {@link AVKey#SOUTHWEST} (the default). These indicate the corner of
     * the viewport to place view controls.
     *
     * @param position the desired view controls position, in screen coordinates.
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.position = position;
        clearControls();
    }

    /**
     * Returns the current layout. Can be one of {@link AVKey#HORIZONTAL} or {@link AVKey#VERTICAL}.
     *
     * @return the current layout.
     */
    public String getLayout()
    {
        return this.layout;
    }

    /**
     * Sets the desired layout. Can be one of {@link AVKey#HORIZONTAL} or {@link AVKey#VERTICAL}.
     *
     * @param layout the desired layout.
     */
    public void setLayout(String layout)
    {
        if (layout == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!this.layout.equals(layout))
        {
            this.layout = layout;
            clearControls();
        }
    }

    /**
     * Layer opacity is not applied to layers of this type. Opacity is controlled by the alpha values of the operation
     * images.
     *
     * @param opacity the current opacity value, which is ignored by this layer.
     */
    @Override
    public void setOpacity(double opacity)
    {
        super.setOpacity(opacity);
    }

    /**
     * Returns the layer's opacity value, which is ignored by this layer. Opacity is controlled by the alpha values of
     * the operation images.
     *
     * @return The layer opacity, a value between 0 and 1.
     */
    @Override
    public double getOpacity()
    {
        return super.getOpacity();
    }

    /**
     * Returns the current layer image location.
     *
     * @return the current location center. May be null.
     */
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    /**
     * Specifies the screen location of the layer, relative to the image's center. May be null. If this value is
     * non-null, it overrides the position specified by {@link #setPosition(String)}. The location is specified in
     * pixels. The origin is the window's lower left corner. Positive X values are to the right of the origin, positive
     * Y values are upwards from the origin. The final image location will be affected by the currently specified
     * location offset if a non-null location offset has been specified (see {@link
     * #setLocationOffset(gov.nasa.worldwind.geom.Vec4)} )}.
     *
     * @param locationCenter the location center. May be null.
     *
     * @see #setPosition(String)
     * @see #setLocationOffset(gov.nasa.worldwind.geom.Vec4)
     */
    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
        clearControls();
    }

    /**
     * Returns the current location offset. See #setLocationOffset for a description of the offset and its values.
     *
     * @return the location offset. Will be null if no offset has been specified.
     */
    public Vec4 getLocationOffset()
    {
        return locationOffset;
    }

    /**
     * Specifies a placement offset from the layer position on the screen.
     *
     * @param locationOffset the number of pixels to shift the layer image from its specified screen position. A
     *                       positive X value shifts the image to the right. A positive Y value shifts the image up. If
     *                       null, no offset is applied. The default offset is null.
     *
     * @see #setLocationCenter(gov.nasa.worldwind.geom.Vec4)
     * @see #setPosition(String)
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
        clearControls();
    }

    public boolean isShowPanControls()
    {
        return this.showPanControls;
    }

    public void setShowPanControls(boolean state)
    {
        if (this.showPanControls != state)
        {
            this.showPanControls = state;
            clearControls();
        }
    }

    public boolean isShowLookControls()
    {
        return this.showLookControls;
    }

    public void setShowLookControls(boolean state)
    {
        if (this.showLookControls != state)
        {
            this.showLookControls = state;
            clearControls();
        }
    }

    public boolean isShowHeadingControls()
    {
        return this.showHeadingControls;
    }

    public void setShowHeadingControls(boolean state)
    {
        if (this.showHeadingControls != state)
        {
            this.showHeadingControls = state;
            clearControls();
        }
    }

    public boolean isShowZoomControls()
    {
        return this.showZoomControls;
    }

    public void setShowZoomControls(boolean state)
    {
        if (this.showZoomControls != state)
        {
            this.showZoomControls = state;
            clearControls();
        }
    }

    public boolean isShowPitchControls()
    {
        return this.showPitchControls;
    }

    public void setShowPitchControls(boolean state)
    {
        if (this.showPitchControls != state)
        {
            this.showPitchControls = state;
            clearControls();
        }
    }

    public boolean isShowFovControls()
    {
        return this.showFovControls;
    }

    public void setShowFovControls(boolean state)
    {
        if (this.showFovControls != state)
        {
            this.showFovControls = state;
            clearControls();
        }
    }

    public void setShowVeControls(boolean state)
    {
        if (this.showVeControls != state)
        {
            this.showVeControls = state;
            clearControls();
        }
    }

    public boolean isShowVeControls()
    {
        return this.showVeControls;
    }

    /**
     * Get the control type associated with the given object or null if unknown.
     *
     * @param control the control object
     *
     * @return the control type. Can be one of {@link AVKey#VIEW_PAN}, {@link AVKey#VIEW_LOOK}, {@link
     *         AVKey#VIEW_HEADING_LEFT}, {@link AVKey#VIEW_HEADING_RIGHT}, {@link AVKey#VIEW_ZOOM_IN}, {@link
     *         AVKey#VIEW_ZOOM_OUT}, {@link AVKey#VIEW_PITCH_UP}, {@link AVKey#VIEW_PITCH_DOWN}, {@link
     *         AVKey#VIEW_FOV_NARROW} or {@link AVKey#VIEW_FOV_WIDE}. <p> Returns null if the object is not a view
     *         control associated with this layer. </p>
     */
    public String getControlType(Object control)
    {
        if (control == null || !(control instanceof ScreenAnnotation))
            return null;

        if (showPanControls && controlPan.equals(control))
            return AVKey.VIEW_PAN;
        else if (showLookControls && controlLook.equals(control))
            return AVKey.VIEW_LOOK;
        else if (showHeadingControls && controlHeadingLeft.equals(control))
            return AVKey.VIEW_HEADING_LEFT;
        else if (showHeadingControls && controlHeadingRight.equals(control))
            return AVKey.VIEW_HEADING_RIGHT;
        else if (showZoomControls && controlZoomIn.equals(control))
            return AVKey.VIEW_ZOOM_IN;
        else if (showZoomControls && controlZoomOut.equals(control))
            return AVKey.VIEW_ZOOM_OUT;
        else if (showPitchControls && controlPitchUp.equals(control))
            return AVKey.VIEW_PITCH_UP;
        else if (showPitchControls && controlPitchDown.equals(control))
            return AVKey.VIEW_PITCH_DOWN;
        else if (showFovControls && controlFovNarrow.equals(control))
            return AVKey.VIEW_FOV_NARROW;
        else if (showFovControls && controlFovWide.equals(control))
            return AVKey.VIEW_FOV_WIDE;
        else if (showVeControls && controlVeUp.equals(control))
            return AVKey.VERTICAL_EXAGGERATION_UP;
        else if (showVeControls && controlVeDown.equals(control))
            return AVKey.VERTICAL_EXAGGERATION_DOWN;

        return null;
    }

    /**
     * Indicates the currently highlighted control, if any.
     *
     * @return the currently highlighted control, or null if no control is highlighted.
     */
    public Object getHighlightedObject()
    {
        return this.currentControl;
    }

    /**
     * Specifies the control to highlight. Any currently highlighted control is un-highlighted.
     *
     * @param control the control to highlight.
     */
    public void highlight(Object control)
    {
        // Manage highlighting of controls.
        if (this.currentControl == control)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.currentControl != null)
        {
            this.currentControl.getAttributes().setImageOpacity(-1); // use default opacity
            this.currentControl = null;
        }

        // Turn on highlight if object selected.
        if (control != null && control instanceof ScreenAnnotation)
        {
            this.currentControl = (ScreenAnnotation) control;
            this.currentControl.getAttributes().setImageOpacity(1);
        }
    }

    @Override
    public void doRender(DrawContext dc)
    {
        if (!this.initialized)
            initialize(dc);

        if (!this.referenceViewport.equals(dc.getView().getViewport()))
            updatePositions(dc);

        super.doRender(dc);
    }

    protected boolean isInitialized()
    {
        return initialized;
    }

    protected void initialize(DrawContext dc)
    {
        if (this.initialized)
            return;

        // Setup user interface - common default attributes
        AnnotationAttributes ca = new AnnotationAttributes();
        ca.setAdjustWidthToText(AVKey.SIZE_FIXED);
        ca.setInsets(new Insets(0, 0, 0, 0));
        ca.setBorderWidth(0);
        ca.setCornerRadius(0);
        ca.setSize(new Dimension(buttonSize, buttonSize));
        ca.setBackgroundColor(new Color(0, 0, 0, 0));
        ca.setImageOpacity(.5);
        ca.setScale(scale);

        final String NOTEXT = "";
        final Point ORIGIN = new Point(0, 0);
        if (this.showPanControls)
        {
            // Pan
            controlPan = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlPan.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PAN);
            controlPan.getAttributes().setImageSource(getImageSource(AVKey.VIEW_PAN));
            controlPan.getAttributes().setSize(new Dimension(panSize, panSize));
            this.addRenderable(controlPan);
        }
        if (this.showLookControls)
        {
            // Look
            controlLook = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlLook.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_LOOK);
            controlLook.getAttributes().setImageSource(getImageSource(AVKey.VIEW_LOOK));
            controlLook.getAttributes().setSize(new Dimension(panSize, panSize));
            this.addRenderable(controlLook);
        }
        if (this.showZoomControls)
        {
            // Zoom
            controlZoomIn = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlZoomIn.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_IN);
            controlZoomIn.getAttributes().setImageSource(getImageSource(AVKey.VIEW_ZOOM_IN));
            this.addRenderable(controlZoomIn);
            controlZoomOut = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlZoomOut.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_ZOOM_OUT);
            controlZoomOut.getAttributes().setImageSource(getImageSource(AVKey.VIEW_ZOOM_OUT));
            this.addRenderable(controlZoomOut);
        }
        if (this.showHeadingControls)
        {
            // Heading
            controlHeadingLeft = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlHeadingLeft.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_HEADING_LEFT);
            controlHeadingLeft.getAttributes().setImageSource(getImageSource(AVKey.VIEW_HEADING_LEFT));
            this.addRenderable(controlHeadingLeft);
            controlHeadingRight = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlHeadingRight.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_HEADING_RIGHT);
            controlHeadingRight.getAttributes().setImageSource(getImageSource(AVKey.VIEW_HEADING_RIGHT));
            this.addRenderable(controlHeadingRight);
        }
        if (this.showPitchControls)
        {
            // Pitch
            controlPitchUp = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlPitchUp.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PITCH_UP);
            controlPitchUp.getAttributes().setImageSource(getImageSource(AVKey.VIEW_PITCH_UP));
            this.addRenderable(controlPitchUp);
            controlPitchDown = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlPitchDown.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_PITCH_DOWN);
            controlPitchDown.getAttributes().setImageSource(getImageSource(AVKey.VIEW_PITCH_DOWN));
            this.addRenderable(controlPitchDown);
        }
        if (this.showFovControls)
        {
            // Field of view FOV
            controlFovNarrow = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlFovNarrow.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_FOV_NARROW);
            controlFovNarrow.getAttributes().setImageSource(getImageSource(AVKey.VIEW_FOV_NARROW));
            this.addRenderable(controlFovNarrow);
            controlFovWide = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlFovWide.setValue(AVKey.VIEW_OPERATION, AVKey.VIEW_FOV_WIDE);
            controlFovWide.getAttributes().setImageSource(getImageSource(AVKey.VIEW_FOV_WIDE));
            this.addRenderable(controlFovWide);
        }
        if (this.showVeControls)
        {
            // Vertical Exaggeration
            controlVeUp = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlVeUp.setValue(AVKey.VIEW_OPERATION, AVKey.VERTICAL_EXAGGERATION_UP);
            controlVeUp.getAttributes().setImageSource(getImageSource(AVKey.VERTICAL_EXAGGERATION_UP));
            this.addRenderable(controlVeUp);
            controlVeDown = new ScreenAnnotation(NOTEXT, ORIGIN, ca);
            controlVeDown.setValue(AVKey.VIEW_OPERATION, AVKey.VERTICAL_EXAGGERATION_DOWN);
            controlVeDown.getAttributes().setImageSource(getImageSource(AVKey.VERTICAL_EXAGGERATION_DOWN));
            this.addRenderable(controlVeDown);
        }

        // Place controls according to layout and viewport dimension
        updatePositions(dc);

        this.initialized = true;
    }

    /**
     * Get a control image source.
     *
     * @param control the control type. Can be one of {@link AVKey#VIEW_PAN}, {@link AVKey#VIEW_LOOK}, {@link
     *                AVKey#VIEW_HEADING_LEFT}, {@link AVKey#VIEW_HEADING_RIGHT}, {@link AVKey#VIEW_ZOOM_IN}, {@link
     *                AVKey#VIEW_ZOOM_OUT}, {@link AVKey#VIEW_PITCH_UP}, {@link AVKey#VIEW_PITCH_DOWN}, {@link
     *                AVKey#VIEW_FOV_NARROW} or {@link AVKey#VIEW_FOV_WIDE}.
     *
     * @return the image source associated with the given control type.
     */
    protected Object getImageSource(String control)
    {
        if (control.equals(AVKey.VIEW_PAN))
            return IMAGE_PAN;
        else if (control.equals(AVKey.VIEW_LOOK))
            return IMAGE_LOOK;
        else if (control.equals(AVKey.VIEW_HEADING_LEFT))
            return IMAGE_HEADING_LEFT;
        else if (control.equals(AVKey.VIEW_HEADING_RIGHT))
            return IMAGE_HEADING_RIGHT;
        else if (control.equals(AVKey.VIEW_ZOOM_IN))
            return IMAGE_ZOOM_IN;
        else if (control.equals(AVKey.VIEW_ZOOM_OUT))
            return IMAGE_ZOOM_OUT;
        else if (control.equals(AVKey.VIEW_PITCH_UP))
            return IMAGE_PITCH_UP;
        else if (control.equals(AVKey.VIEW_PITCH_DOWN))
            return IMAGE_PITCH_DOWN;
        else if (control.equals(AVKey.VIEW_FOV_WIDE))
            return IMAGE_FOV_WIDE;
        else if (control.equals(AVKey.VIEW_FOV_NARROW))
            return IMAGE_FOV_NARROW;
        else if (control.equals(AVKey.VERTICAL_EXAGGERATION_UP))
            return IMAGE_VE_UP;
        else if (control.equals(AVKey.VERTICAL_EXAGGERATION_DOWN))
            return IMAGE_VE_DOWN;

        return null;
    }

    // Set controls positions according to layout and viewport dimension
    protected void updatePositions(DrawContext dc)
    {
        boolean horizontalLayout = this.layout.equals(AVKey.HORIZONTAL);

        // horizontal layout: pan button + look button beside 2 rows of 4 buttons
        int width = (showPanControls ? panSize : 0) +
            (showLookControls ? panSize : 0) +
            (showZoomControls ? buttonSize : 0) +
            (showHeadingControls ? buttonSize : 0) +
            (showPitchControls ? buttonSize : 0) +
            (showFovControls ? buttonSize : 0) +
            (showVeControls ? buttonSize : 0);
        int height = Math.max(panSize, buttonSize * 2);
        width = (int) (width * scale);
        height = (int) (height * scale);
        int xOffset = 0;
        int yOffset = (int) (buttonSize * scale);

        if (!horizontalLayout)
        {
            // vertical layout: pan button above look button above 4 rows of 2 buttons
            int temp = height;
            //noinspection SuspiciousNameCombination
            height = width;
            width = temp;
            xOffset = (int) (buttonSize * scale);
            yOffset = 0;
        }

        int halfPanSize = (int) (panSize * scale / 2);
        int halfButtonSize = (int) (buttonSize * scale / 2);

        Rectangle controlsRectangle = new Rectangle(width, height);
        Point locationSW = computeLocation(dc.getView().getViewport(), controlsRectangle);

        // Layout start point
        int x = locationSW.x;
        int y = horizontalLayout ? locationSW.y : locationSW.y + height;

        if (this.showPanControls)
        {
            if (!horizontalLayout)
                y -= (int) (panSize * scale);
            controlPan.setScreenPoint(new Point(x + halfPanSize, y));
            if (horizontalLayout)
                x += (int) (panSize * scale);
        }
        if (this.showLookControls)
        {
            if (!horizontalLayout)
                y -= (int) (panSize * scale);
            controlLook.setScreenPoint(new Point(x + halfPanSize, y));
            if (horizontalLayout)
                x += (int) (panSize * scale);
        }
        if (this.showZoomControls)
        {
            if (!horizontalLayout)
                y -= (int) (buttonSize * scale);
            controlZoomIn.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
            controlZoomOut.setScreenPoint(new Point(x + halfButtonSize, y));
            if (horizontalLayout)
                x += (int) (buttonSize * scale);
        }
        if (this.showHeadingControls)
        {
            if (!horizontalLayout)
                y -= (int) (buttonSize * scale);
            controlHeadingLeft.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
            controlHeadingRight.setScreenPoint(new Point(x + halfButtonSize, y));
            if (horizontalLayout)
                x += (int) (buttonSize * scale);
        }
        if (this.showPitchControls)
        {
            if (!horizontalLayout)
                y -= (int) (buttonSize * scale);
            controlPitchUp.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
            controlPitchDown.setScreenPoint(new Point(x + halfButtonSize, y));
            if (horizontalLayout)
                x += (int) (buttonSize * scale);
        }
        if (this.showFovControls)
        {
            if (!horizontalLayout)
                y -= (int) (buttonSize * scale);
            controlFovNarrow.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
            controlFovWide.setScreenPoint(new Point(x + halfButtonSize, y));
            if (horizontalLayout)
                x += (int) (buttonSize * scale);
        }
        if (this.showVeControls)
        {
            if (!horizontalLayout)
                y -= (int) (buttonSize * scale);
            controlVeUp.setScreenPoint(new Point(x + halfButtonSize + xOffset, y + yOffset));
            controlVeDown.setScreenPoint(new Point(x + halfButtonSize, y));
            if (horizontalLayout)
                x += (int) (buttonSize * scale);
        }

        this.referenceViewport = dc.getView().getViewport();
    }

    /**
     * Compute the screen location of the controls overall rectangle bottom right corner according to either the
     * location center if not null, or the screen position.
     *
     * @param viewport the current viewport rectangle.
     * @param controls the overall controls rectangle
     *
     * @return the screen location of the bottom left corner - south west corner.
     */
    protected Point computeLocation(Rectangle viewport, Rectangle controls)
    {
        double x;
        double y;

        if (this.locationCenter != null)
        {
            x = this.locationCenter.x - controls.width / 2;
            y = this.locationCenter.y - controls.height / 2;
        }
        else if (this.position.equals(AVKey.NORTHEAST))
        {
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST))
        {
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST))
        {
            x = 0d + this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST))
        {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else // use North East as default
        {
            x = viewport.getWidth() - controls.width - this.borderWidth;
            y = viewport.getHeight() - controls.height - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Point((int) x, (int) y);
    }

    protected void clearControls()
    {
        this.removeAllRenderables();

        this.controlPan = null;
        this.controlLook = null;
        this.controlHeadingLeft = null;
        this.controlHeadingRight = null;
        this.controlZoomIn = null;
        this.controlZoomOut = null;
        this.controlPitchUp = null;
        this.controlPitchDown = null;
        this.controlFovNarrow = null;
        this.controlFovWide = null;
        this.controlVeUp = null;
        this.controlVeDown = null;

        this.initialized = false;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.ViewControlsLayer.Name");
    }
}
