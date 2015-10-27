/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;

/**
 * Displays a terrain profile graph in a screen corner. <p/> <p> Usage: do setEventSource(wwd) to have the graph
 * activated and updated with position changes. See public properties for options: keepProportions, follow, unit, start
 * and end latlon... </p>
 *
 * @author Patrick Murris
 * @version $Id: TerrainProfileLayer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class TerrainProfileLayer extends AbstractLayer implements PositionListener, SelectListener
{
    // Units constants
    public final static String UNIT_METRIC = "gov.nasa.worldwind.TerrainProfileLayer.Metric";
    public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.TerrainProfileLayer.Imperial";
    public final static double METER_TO_FEET = 3.280839895;
    // Follow constants
    public final static String FOLLOW_VIEW = "gov.nasa.worldwind.TerrainProfileLayer.FollowView";
    public final static String FOLLOW_EYE = "gov.nasa.worldwind.TerrainProfileLayer.FollowEye";
    public final static String FOLLOW_CURSOR = "gov.nasa.worldwind.TerrainProfileLayer.FollowCursor";
    public final static String FOLLOW_NONE = "gov.nasa.worldwind.TerrainProfileLayer.FollowNone";
    public final static String FOLLOW_OBJECT = "gov.nasa.worldwind.TerrainProfileLayer.FollowObject";
    public final static String FOLLOW_PATH = "gov.nasa.worldwind.TerrainProfileLayer.FollowPath";
    // Graph size when minimized
    protected final static int MINIMIZED_SIZE = 32;

    // GUI pickable objects
    protected final String buttonMinimize = "gov.nasa.worldwind.TerrainProfileLayer.ButtonMinimize";
    protected final String buttonMaximize = "gov.nasa.worldwind.TerrainProfileLayer.ButtonMaximize";

    // Display parameters
    protected Dimension size = new Dimension(250, 100);
    protected Color color = Color.white;
    protected int borderWidth = 20;
    protected String position = AVKey.SOUTHWEST;
    protected String resizeBehavior = AVKey.RESIZE_SHRINK_ONLY;
    protected String unit = UNIT_METRIC;
    protected Font defaultFont = Font.decode("Arial-PLAIN-12");
    protected double toViewportScale = 1;
    protected Point locationCenter = null;
    protected Vec4 locationOffset = null;
    protected PickSupport pickSupport = new PickSupport();
    protected boolean initialized = false;
    protected boolean isMinimized = false;     // True when graph is minimized to an icon
    protected boolean isMaximized = false;     // True when graph is 'full screen'
    protected boolean showProfileLine = true;  // True to show the profile path line on the ground
    protected boolean showPickedLine = true;   // True to show the picked position line on the terrain

    protected int pickedSample = -1;           // Picked sample number if not -1
    Polyline selectionShape;                 // Shape showing section on the ground
    Polyline pickedShape;                    // Shape showing actual pick position on the ground
    protected boolean keepProportions = false; // Keep graph distance/elevation proportions
    protected boolean zeroBased = true;        // Pad graph elevation scale to include sea level if true
    protected String follow = FOLLOW_VIEW;     // Profile position follow behavior
    protected boolean showEyePosition = false; // When FOLLOW_EYE, draw the eye position on graph when true
    protected double profileLengthFactor = 1;  // Applied to default profile length (zoom on profile)
    protected LatLon startLatLon;              // Section start lat/lon when FOLLOW_NONE
    protected LatLon endLatLon;                // Section end lat/lon when FOLLOW_NONE
    protected Position objectPosition;         // Object position if FOLLOW_OBJECT
    protected Angle objectHeading;             // Object heading if FOLLOW_OBJECT
    protected ArrayList<? extends LatLon> pathPositions;  // Path position list if FOLLOW_PATH
    protected int pathType = Polyline.GREAT_CIRCLE;

    // Terrain profile data
    protected int samples = 250;              // Number of position samples
    protected double minElevation;            // Minimum elevation along the profile
    protected double maxElevation;            // Maximum elevation along the profile
    protected double length;                  // Profile length along great circle in meter
    protected Position positions[];           // Position list

    // Worldwind
    protected WorldWindow wwd;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    protected OrderedIcon orderedImage = new OrderedIcon();

    protected class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            TerrainProfileLayer.this.drawProfile(dc);
        }

        public void render(DrawContext dc)
        {
            TerrainProfileLayer.this.drawProfile(dc);
        }
    }

    /** Renders a terrain profile graphic in a screen corner. */
    public TerrainProfileLayer()
    {
    }

    // ** Public properties ************************************************************

    /**
     * Get whether the profile graph is minimized.
     *
     * @return true if the profile graph is minimized.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean getIsMinimized()
    {
        return this.isMinimized;
    }

    /**
     * Set whether the profile graph should be minimized. <p>Note that the graph can be both minimized and maximized at
     * the same time. The minimized state will take precedence and the graph will display as an icon. When
     * 'un-minimized' it will display as maximized.</p>
     *
     * @param state true if the profile should be minimized.
     */
    public void setIsMinimized(boolean state)
    {
        this.isMinimized = state;
        this.pickedSample = -1;  // Clear picked position
    }

    /**
     * Get whether the profile graph is maximized - displays over the whole viewport.
     *
     * @return true if the profile graph is maximized.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean getIsMaximized()
    {
        return this.isMaximized;
    }

    /**
     * Set wheter the profile graph should be maximized - displays over the whole viewport.
     *
     * @param state true if the profile should be maximized.
     */
    public void setIsMaximized(boolean state)
    {
        this.isMaximized = state;
    }

    /**
     * Get the graphic Dimension (in pixels).
     *
     * @return the scalebar graphic Dimension.
     */
    public Dimension getSize()
    {
        return this.size;
    }

    /**
     * Set the graphic Dimension (in pixels).
     *
     * @param size the graphic Dimension.
     */
    public void setSize(Dimension size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.size = size;
    }

    /**
     * Get the graphic color.
     *
     * @return the graphic Color.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the graphic Color.
     *
     * @param color the graphic Color.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.color = color;
    }

    /**
     * Sets the layer opacity, which is applied to the layer's chart. The opacity is modified slightly for various
     * elements of the chart to distinguish among them.
     *
     * @param opacity the current opacity value, which is ignored by this layer.
     *
     * @see #setColor
     */
    @Override
    public void setOpacity(double opacity)
    {
        super.setOpacity(opacity);
    }

    /**
     * Returns the layer's opacity value. The opacity is modified slightly for various elements of the chart to
     * distinguish among them.
     *
     * @return The layer opacity, a value between 0 and 1.
     *
     * @see #getColor
     */
    @Override
    public double getOpacity()
    {
        return super.getOpacity();
    }

    /**
     * Returns the graphic-to-viewport scale factor.
     *
     * @return the graphic-to-viewport scale factor.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public double getToViewportScale()
    {
        return toViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the graphic. This scale
     * factor is used only when the layer's resize behavior is {@link AVKey#RESIZE_STRETCH} or {@link
     * AVKey#RESIZE_SHRINK_ONLY}. The graphic's width is adjusted to occupy the proportion of the viewport's width
     * indicated by this factor. The graphic's height is adjusted to maintain the graphic's Dimension aspect ratio.
     *
     * @param toViewportScale the graphic to viewport scale factor.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setToViewportScale(double toViewportScale)
    {
        this.toViewportScale = toViewportScale;
    }

    public String getPosition()
    {
        return this.position;
    }

    /**
     * Sets the relative viewport location to display the graphic. Can be one of {@link AVKey#NORTHEAST}, {@link
     * AVKey#NORTHWEST}, {@link AVKey#SOUTHEAST}, or {@link AVKey#SOUTHWEST} (the default). These indicate the corner of
     * the viewport.
     *
     * @param position the desired graphic position.
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.position = position;
    }

    /**
     * Get the screen location of the graph center if set (can be null).
     *
     * @return the screen location of the graph center if set (can be null).
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Point getLocationCenter()
    {
        return this.locationCenter;
    }

    /**
     * Set the screen location of the graph center - overrides {@link #setPosition} if not null.
     *
     * @param point the screen location of the graph center (can be null).
     */
    public void setLocationCenter(Point point)
    {
        this.locationCenter = point;
    }

    /**
     * Returns the current location offset. See {@link #setLocationOffset} for a description of the offset and its
     * values.
     *
     * @return the location offset. Will be null if no offset has been specified.
     */
    @SuppressWarnings({"UnusedDeclaration"})
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
     * @see #setLocationCenter
     * @see #setPosition
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
    }

    /**
     * Returns the layer's resize behavior.
     *
     * @return the layer's resize behavior.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the graphic when the viewport size changes, typically when the World
     * Wind window is resized. If the value is {@link AVKey#RESIZE_KEEP_FIXED_SIZE}, the graphic size is kept to the
     * size specified in its Dimension scaled by the layer's current icon scale. If the value is {@link
     * AVKey#RESIZE_STRETCH}, the graphic is resized to have a constant size relative to the current viewport size. If
     * the viewport shrinks the graphic size decreases; if it expands then the graphic enlarges. If the value is {@link
     * AVKey#RESIZE_SHRINK_ONLY} (the default), graphic sizing behaves as for {@link AVKey#RESIZE_STRETCH} but it will
     * not grow larger than the size specified in its Dimension.
     *
     * @param resizeBehavior the desired resize behavior
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setResizeBehavior(String resizeBehavior)
    {
        this.resizeBehavior = resizeBehavior;
    }

    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * Sets the graphic offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the graphic from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public String getUnit()
    {
        return this.unit;
    }

    /**
     * Sets the unit the graphic uses to display distances and elevations. Can be one of {@link #UNIT_METRIC} (the
     * default), or {@link #UNIT_IMPERIAL}.
     *
     * @param unit the desired unit.
     */
    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    /**
     * Get the graphic legend Font.
     *
     * @return the graphic legend Font.
     */
    public Font getFont()
    {
        return this.defaultFont;
    }

    /**
     * Set the graphic legend Font.
     *
     * @param font the graphic legend Font.
     */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.defaultFont = font;
    }

    /**
     * Get whether distance/elevation proportions are maintained.
     *
     * @return true if the graph maintains distance/elevation proportions.
     */
    public boolean getKeepProportions()
    {
        return this.keepProportions;
    }

    /**
     * Set whether distance/elevation proportions are maintained.
     *
     * @param state true if the graph should maintains distance/elevation proportions.
     */
    public void setKeepProportions(boolean state)
    {
        this.keepProportions = state;
    }

    /**
     * Get the graph center point placement behavior.
     *
     * @return the graph center point placement behavior.
     */
    public String getFollow()
    {
        return this.follow;
    }

    /**
     * Set the graph center point placement behavior. Can be one of {@link #FOLLOW_VIEW} (the default), {@link
     * #FOLLOW_CURSOR}, {@link #FOLLOW_EYE}, {@link #FOLLOW_OBJECT}, {@link #FOLLOW_NONE} or {@link #FOLLOW_PATH}. If
     * {@link #FOLLOW_NONE} the profile will be computed between startLatLon and endLatLon. If {@link #FOLLOW_PATH} the
     * profile will be computed along the path defined with {@link #setPathPositions(java.util.ArrayList)}.
     *
     * @param behavior the graph center point placement behavior.
     */
    public void setFollow(String behavior)
    {
        this.follow = behavior;
    }

    /**
     * Get whether the eye or object position is shown on the graph when {@link #FOLLOW_EYE} or {@link #FOLLOW_OBJECT}.
     *
     * @return true if the eye or object position is shown on the graph.
     */
    public boolean getShowEyePosition()
    {
        return this.showEyePosition;
    }

    /**
     * Set whether the eye or object position should be shown on the graph when {@link #FOLLOW_EYE} or {@link
     * #FOLLOW_OBJECT}.
     *
     * @param state if true the eye or object position should be shown on the graph.
     */
    public void setShowEyePosition(Boolean state)
    {
        this.showEyePosition = state;
    }

    /**
     * Set the profile length factor - has no effect if {@link #FOLLOW_NONE} or {@link #FOLLOW_PATH}
     *
     * @param factor the new profile length factor.
     */
    public void setProfileLengthFactor(double factor)
    {
        this.profileLengthFactor = factor;
    }

    /**
     * Get the profile length factor.
     *
     * @return the profile length factor.
     */
    public double getProfileLenghtFactor()
    {
        return this.profileLengthFactor;
    }

    /**
     * Get the profile start position lat/lon when {@link #FOLLOW_NONE}.
     *
     * @return the profile start position lat/lon.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public LatLon getStartLatLon()
    {
        return this.startLatLon;
    }

    /**
     * Set the profile start position lat/lon when {@link #FOLLOW_NONE}.
     *
     * @param latLon the profile start position lat/lon.
     */
    public void setStartLatLon(LatLon latLon)
    {
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.startLatLon = latLon;
    }

    /**
     * Get the profile end position lat/lon when {@link #FOLLOW_NONE}.
     *
     * @return the profile end position lat/lon.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public LatLon getEndLatLon()
    {
        return this.endLatLon;
    }

    /**
     * Set the profile end position lat/lon when {@link #FOLLOW_NONE}.
     *
     * @param latLon the profile end position lat/lon.
     */
    public void setEndLatLon(LatLon latLon)
    {
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.endLatLon = latLon;
    }

    /**
     * Get the number of elevation samples in the profile.
     *
     * @return the number of elevation samples in the profile.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public int getSamples()
    {
        return this.samples;
    }

    /**
     * Set the number of elevation samples in the profile.
     *
     * @param number the number of elevation samples in the profile.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setSamples(int number)
    {
        this.samples = Math.abs(number);
    }

    /**
     * Get whether the profile graph should include sea level.
     *
     * @return whether the profile graph should include sea level.
     */
    public boolean getZeroBased()
    {
        return this.zeroBased;
    }

    /**
     * Set whether the profile graph should include sea level. True is the default.
     *
     * @param state true if the profile graph should include sea level.
     */
    public void setZeroBased(boolean state)
    {
        this.zeroBased = state;
    }

    /**
     * Get the object position the graph follows when it is set to {@link #FOLLOW_OBJECT}.
     *
     * @return the object position the graph follows.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Position getObjectPosition()
    {
        return this.objectPosition;
    }

    /**
     * Set the object position the graph follows when it is set to {@link #FOLLOW_OBJECT}.
     *
     * @param pos the object position the graph follows.
     */
    public void setObjectPosition(Position pos)
    {
        this.objectPosition = pos;
    }

    /**
     * Get the object heading the graph follows when it is set to {@link #FOLLOW_OBJECT}. The profile graph will be
     * computed perpendicular to the object heading direction.
     *
     * @return the object heading the graph follows.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Angle getObjectHeading()
    {
        return this.objectHeading;
    }

    /**
     * Set the object heading the graph follows when it is set to {@link #FOLLOW_OBJECT}. The profile graph will be
     * computed perpendicular to the object heading direction.
     *
     * @param heading the object heading the graph follows.
     */
    public void setObjectHeading(Angle heading)
    {
        this.objectHeading = heading;
    }

    /**
     * Get the path positions that the profile follows if set to {@link #FOLLOW_PATH}.
     *
     * @return the path positions that the profile follows.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<? extends LatLon> getPathPositions()
    {
        return this.pathPositions;
    }

    /**
     * Set the path positions that the profile should follow if  {@link #FOLLOW_PATH}.
     *
     * @param positions the path positions that the profile should follow.
     */
    public void setPathPositions(ArrayList<? extends LatLon> positions)
    {
        if (positions == null)
        {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.pathPositions = positions;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getPathType()
    {
        return this.pathType;
    }

    /**
     * Sets the type of path to follow, one of {@link Polyline#GREAT_CIRCLE}, which computes each segment of the path as
     * a great circle, or {@link Polyline#RHUMB_LINE}, which computes each segment of the path as a line of constant
     * heading.
     *
     * @param type the type of path to follow.
     */
    public void setPathType(int type)
    {
        this.pathType = type;
    }

    /**
     * Get the <code>Polyline</code> used to render the profile line on the ground.
     *
     * @return the <code>Polyline</code> used to render the profile line on the ground.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Polyline getProfileLine()
    {
        return this.selectionShape;
    }

    /**
     * Get the <code>Polyline</code> used to render the picked position on the terrain.
     *
     * @return the <code>Polyline</code> used to render the picked position on the terrain.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Polyline getPickedLine()
    {
        return this.selectionShape;
    }

    /**
     * Get whether the profile line is displayed over the ground.
     *
     * @return true is the profile line is displayed over the ground.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean isShowProfileLine()
    {
        return this.showProfileLine;
    }

    /**
     * Set whether the profile line should be displayed over the terrain.
     *
     * @param state true if the profile line should be displayed over the terrain.
     */
    public void setShowProfileLine(boolean state)
    {
        this.showProfileLine = state;
    }

    /**
     * Get whether the picked line is displayed over the ground.
     *
     * @return true if the picked line is displayed over the ground.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean isShowPickedLine()
    {
        return this.showPickedLine;
    }

    /**
     * Set whether the picked line should be displayed over the ground.
     *
     * @param state if the picked line should be displayed over the ground.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setShowPickedLine(boolean state)
    {
        this.showPickedLine = state;
    }

    // ** Rendering ************************************************************

    @Override
    public void doRender(DrawContext dc)
    {
        // Delegate graph rendering to OrderedRenderable list
        dc.addOrderedRenderable(this.orderedImage);
        // Render section line on the ground now
        if (!isMinimized && this.positions != null && this.selectionShape != null)
        {
            if (this.showProfileLine)
                this.selectionShape.render(dc);
            // If picking in progress, render pick indicator
            if (this.showPickedLine && this.pickedSample != -1 && this.pickedShape != null)
                this.pickedShape.render(dc);
        }
    }

    @Override
    public void doPick(DrawContext dc, Point pickPoint)
    {
        dc.addOrderedRenderable(this.orderedImage);
    }

    protected void initialize(DrawContext dc)
    {
        if (this.initialized || this.positions != null)
            return;

        if (this.wwd != null)
        {
            this.computeProfile(dc);
//            this.expirySupport.restart(dc);
        }

        if (this.positions != null)
            this.initialized = true;
    }

    // Profile graph rendering - ortho

    public void drawProfile(DrawContext dc)
    {
        this.computeProfile(dc);

        if ((this.positions == null || (this.minElevation == 0 && this.maxElevation == 0))
            && !this.initialized)
            this.initialize(dc);

        if (this.positions == null || (this.minElevation == 0 && this.maxElevation == 0))
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_TRANSFORM_BIT
                | GL2.GL_VIEWPORT_BIT
                | GL2.GL_CURRENT_BIT);
            attribsPushed = true;

            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            Rectangle viewport = dc.getView().getViewport();
            Dimension drawSize = isMinimized ? new Dimension(MINIMIZED_SIZE, MINIMIZED_SIZE) :
                isMaximized ? new Dimension(viewport.width - this.borderWidth * 2,
                    viewport.height * 2 / 3 - this.borderWidth * 2) : this.size;
            double width = drawSize.width;
            double height = drawSize.height;

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            // Scale to a width x height space
            // located at the proper position on screen
            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);
            gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
            gl.glScaled(scale, scale, 1d);

            if (!dc.isPickingMode())
            {
                // Draw grid - Set color using current layer opacity
                this.drawGrid(dc, drawSize);

                // Draw profile graph
                this.drawGraph(dc, drawSize);

                if (!isMinimized)
                {
                    // Draw GUI buttons
                    drawGUI(dc, drawSize);

                    // Draw labels
                    String label = String.format("min %.0fm   max %.0fm", this.minElevation, this.maxElevation);
                    if (this.unit.equals(UNIT_IMPERIAL))
                        label = String.format("min %.0fft   max %.0fft", this.minElevation * METER_TO_FEET,
                            this.maxElevation * METER_TO_FEET);
                    gl.glLoadIdentity();
                    gl.glDisable(GL.GL_CULL_FACE);
                    drawLabel(dc, label, locationSW.add3(new Vec4(0, -12, 0)), -1); // left aligned
                    if (this.pickedSample != -1)
                    {
                        double pickedElevation = positions[this.pickedSample].getElevation();
                        label = String.format("%.0fm", pickedElevation);
                        if (this.unit.equals(UNIT_IMPERIAL))
                            label = String.format("%.0fft", pickedElevation * METER_TO_FEET);
                        drawLabel(dc, label, locationSW.add3(new Vec4(width, -12, 0)), 1); // right aligned
                    }
                }
            }
            else
            {
                // Picking
                this.pickSupport.clearPickList();
                this.pickSupport.beginPicking(dc);
                // Draw unique color across the rectangle
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                if (!isMinimized)
                {
                    // Update graph pick point
                    computePickPosition(dc, locationSW, new Dimension((int) (width * scale), (int) (height * scale)));
                    // Draw GUI buttons
                    drawGUI(dc, drawSize);
                }
                else
                {
                    // Add graph to the pickable list for 'un-minimize' click
                    this.pickSupport.addPickableObject(colorCode, this);
                    gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                    gl.glBegin(GL2.GL_POLYGON);
                    gl.glVertex3d(0, 0, 0);
                    gl.glVertex3d(width, 0, 0);
                    gl.glVertex3d(width, height, 0);
                    gl.glVertex3d(0, height, 0);
                    gl.glVertex3d(0, 0, 0);
                    gl.glEnd();
                }
                // Done picking
                this.pickSupport.endPicking(dc);
                this.pickSupport.resolvePick(dc, dc.getPickPoint(), this);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    // Draw grid graphic

    protected void drawGrid(DrawContext dc, Dimension dimension)
    {
        // Background color
        Color backColor = getBackgroundColor(this.color);
        drawFilledRectangle(dc, new Vec4(0, 0, 0), dimension, new Color(backColor.getRed(),
            backColor.getGreen(), backColor.getBlue(), (int) (backColor.getAlpha() * .5))); // Increased transparency
        // Grid - minimal
        float[] colorRGB = this.color.getRGBColorComponents(null);
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());
        drawVerticalLine(dc, dimension, 0);
        drawVerticalLine(dc, dimension, dimension.getWidth());
        drawHorizontalLine(dc, dimension, 0);
    }

    // Draw profile graphic

    protected void drawGraph(DrawContext dc, Dimension dimension)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        // Adjust min/max elevation for the graph
        double min = this.minElevation;
        double max = this.maxElevation;
        if (this.showEyePosition && this.follow.equals(FOLLOW_EYE))
            max = Math.max(max, dc.getView().getEyePosition().getElevation());
        if (this.showEyePosition && (this.follow.equals(FOLLOW_OBJECT) || this.follow.equals(FOLLOW_PATH))
            && this.objectPosition != null)
            max = Math.max(max, this.objectPosition.getElevation());
        if (this.zeroBased)
        {
            if (min > 0)
                min = 0;
            if (max < 0)
                max = 0;
        }
        int i;
        double stepX = dimension.getWidth() / this.length;
        double stepY = dimension.getHeight() / (max - min);
        if (this.keepProportions)
        {
            stepX = Math.min(stepX, stepY);
            //noinspection SuspiciousNameCombination
            stepY = stepX;
        }
        double lengthStep = this.length / (this.samples - 1);
        double x = 0, y;
        // Filled graph
        gl.glColor4ub((byte) this.color.getRed(), (byte) this.color.getGreen(),
            (byte) this.color.getBlue(), (byte) 100);
        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        for (i = 0; i < this.samples; i++)
        {
            x = i * lengthStep * stepX;
            y = (this.positions[i].getElevation() - min) * stepY;
            gl.glVertex3d(x, 0, 0);
            gl.glVertex3d(x, y, 0);
        }
        gl.glEnd();
        // Line graph
        float[] colorRGB = this.color.getRGBColorComponents(null);
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 0; i < this.samples; i++)
        {
            x = i * lengthStep * stepX;
            y = (this.positions[i].getElevation() - min) * stepY;
            gl.glVertex3d(x, y, 0);
        }
        gl.glEnd();
        // Middle vertical line
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity() * .3); // increased transparency here
        if (!this.follow.equals(FOLLOW_PATH))
            drawVerticalLine(dc, dimension, x / 2);
        // Eye or object position
        double eyeX = -1, eyeY = -1;
        if ((this.follow.equals(FOLLOW_EYE) ||
            (this.follow.equals(FOLLOW_OBJECT) && this.objectPosition != null) ||
            (this.follow.equals(FOLLOW_PATH) && this.objectPosition != null))
            && this.showEyePosition)
        {
            eyeX = x / 2;
            eyeY = (dc.getView().getEyePosition().getElevation() - min) * stepY;
            if (this.follow.equals(FOLLOW_PATH))
                eyeX = computeObjectSample(this.objectPosition) * lengthStep * stepX;
            if (this.follow.equals(FOLLOW_OBJECT) || this.follow.equals(FOLLOW_PATH))
                eyeY = (this.objectPosition.getElevation() - min) * stepY;
            if (eyeX >= 0 && eyeY >= 0)
                this.drawFilledRectangle(dc, new Vec4(eyeX - 2, eyeY - 2, 0), new Dimension(5, 5), this.color);
            // Vertical line at object position when follow path
            if (this.follow.equals(FOLLOW_PATH) && eyeX >= 0)
                drawVerticalLine(dc, dimension, eyeX);
        }
        // Selected/picked vertical and horizontal lines
        if (this.pickedSample != -1)
        {
            double pickedX = this.pickedSample * lengthStep * stepX;
            double pickedY = (positions[this.pickedSample].getElevation() - min) * stepY;
            gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2] * .5, this.getOpacity() * .8); // yellower color
            drawVerticalLine(dc, dimension, pickedX);
            drawHorizontalLine(dc, dimension, pickedY);
            // Eye or object - picked position line
            if (eyeX >= 0 && eyeY >= 0)
            {
                // Line
                drawLine(dc, pickedX, pickedY, eyeX, eyeY);
                // Distance label
                double distance = dc.getView().getEyePoint().distanceTo3(
                    dc.getGlobe().computePointFromPosition(positions[this.pickedSample]));
                if (this.follow.equals(FOLLOW_OBJECT) || this.follow.equals(FOLLOW_PATH))
                    distance = dc.getGlobe().computePointFromPosition(this.objectPosition).distanceTo3(
                        dc.getGlobe().computePointFromPosition(positions[this.pickedSample]));
                String label = String.format("Dist %.0fm", distance);
                if (this.unit.equals(UNIT_IMPERIAL))
                    label = String.format("Dist %.0fft", distance * METER_TO_FEET);
                drawLabel(dc, label, new Vec4(pickedX + 5, pickedY - 12, 0), -1); // left aligned
            }
        }
        // Min elevation horizontal line
        if (this.minElevation != min)
        {
            y = (this.minElevation - min) * stepY;
            gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity() * .5);  // medium transparency
            drawHorizontalLine(dc, dimension, y);
        }
        // Max elevation horizontal line
        if (this.maxElevation != max)
        {
            y = (this.maxElevation - min) * stepY;
            gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity() * .5);  // medium transparency
            drawHorizontalLine(dc, dimension, y);
        }
        // Sea level in between positive elevations only (not across land)
        if (min < 0 && max >= 0)
        {
            gl.glColor4d(colorRGB[0] * .7, colorRGB[1] * .7, colorRGB[2], this.getOpacity() * .5); // bluer color
            y = -this.minElevation * stepY;
            double previousX = -1;
            for (i = 0; i < this.samples; i++)
            {
                x = i * lengthStep * stepX;
                if (this.positions[i].getElevation() > 0 || i == this.samples - 1)
                {
                    if (previousX >= 0)
                    {
                        gl.glBegin(GL2.GL_LINE_STRIP);
                        gl.glVertex3d(previousX, y, 0);
                        gl.glVertex3d(x, y, 0);
                        gl.glEnd();
                        previousX = -1;
                    }
                }
                else
                    previousX = previousX < 0 ? x : previousX;
            }
        }
    }

    protected void drawGUI(DrawContext dc, Dimension dimension)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        int buttonSize = 16;
        int hs = buttonSize / 2;
        int buttonBorder = 4;
        Dimension buttonDimension = new Dimension(buttonSize, buttonSize);
        Color highlightColor = new Color(color.getRed(), color.getGreen(),
            color.getBlue(), (int) (color.getAlpha() * .5));
        Color backColor = getBackgroundColor(this.color);
        backColor = new Color(backColor.getRed(), backColor.getGreen(),
            backColor.getBlue(), (int) (backColor.getAlpha() * .5)); // Increased transparency
        Color drawColor;
        int y = dimension.height - buttonDimension.height - buttonBorder;
        int x = dimension.width;
        Object pickedObject = dc.getPickedObjects() != null ? dc.getPickedObjects().getTopObject() : null;
        // Maximize button
        if (!isMaximized)
        {
            x -= buttonDimension.width + buttonBorder;
            if (dc.isPickingMode())
            {
                drawColor = dc.getUniquePickColor();
                int colorCode = drawColor.getRGB();
                this.pickSupport.addPickableObject(colorCode, this.buttonMaximize, null, false);
            }
            else
                drawColor = this.buttonMaximize == pickedObject ? highlightColor : backColor;
            drawFilledRectangle(dc, new Vec4(x, y, 0), buttonDimension, drawColor);
            if (!dc.isPickingMode())
            {
                gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
                    (byte) color.getBlue(), (byte) color.getAlpha());
                // Draw '+'
                drawLine(dc, x + 3, y + hs, x + buttonDimension.width - 3, y + hs); // Horizontal line
                drawLine(dc, x + hs, y + 3, x + hs, y + buttonDimension.height - 3); // Vertical line
            }
        }

        // Minimize button
        x -= buttonDimension.width + buttonBorder;
        if (dc.isPickingMode())
        {
            drawColor = dc.getUniquePickColor();
            int colorCode = drawColor.getRGB();
            this.pickSupport.addPickableObject(colorCode, this.buttonMinimize, null, false);
        }
        else
            drawColor = this.buttonMinimize == pickedObject ? highlightColor : backColor;
        drawFilledRectangle(dc, new Vec4(x, y, 0), buttonDimension, drawColor);
        if (!dc.isPickingMode())
        {
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
                (byte) color.getBlue(), (byte) color.getAlpha());
            // Draw '-'
            drawLine(dc, x + 3, y + hs, x + buttonDimension.width - 3, y + hs); // Horizontal line
        }
    }

    protected void drawHorizontalLine(DrawContext dc, Dimension dimension, double y)
    {
        drawLine(dc, 0, y, dimension.getWidth(), y);
    }

    protected void drawVerticalLine(DrawContext dc, Dimension dimension, double x)
    {
        drawLine(dc, x, 0, x, dimension.getHeight());
    }

    protected void drawFilledRectangle(DrawContext dc, Vec4 origin, Dimension dimension, Color color)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(),
            (byte) color.getBlue(), (byte) color.getAlpha());
        gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3d(origin.x, origin.y, 0);
        gl.glVertex3d(origin.x + dimension.getWidth(), origin.y, 0);
        gl.glVertex3d(origin.x + dimension.getWidth(), origin.y + dimension.getHeight(), 0);
        gl.glVertex3d(origin.x, origin.y + dimension.getHeight(), 0);
        gl.glVertex3d(origin.x, origin.y, 0);
        gl.glEnd();
    }

    protected void drawLine(DrawContext dc, double x1, double y1, double x2, double y2)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3d(x1, y1, 0);
        gl.glVertex3d(x2, y2, 0);
        gl.glEnd();
    }

    // Draw a text label
    // Align = -1: left, 0: center and 1: right

    protected void drawLabel(DrawContext dc, String text, Vec4 screenPoint, int align)
    {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            this.defaultFont);

        Rectangle2D nameBound = textRenderer.getBounds(text);
        int x = (int) screenPoint.x();  // left
        if (align == 0)
            x = (int) (screenPoint.x() - nameBound.getWidth() / 2d);  // centered
        if (align > 0)
            x = (int) (screenPoint.x() - nameBound.getWidth());  // right
        int y = (int) screenPoint.y();

        textRenderer.begin3DRendering();

        textRenderer.setColor(this.getBackgroundColor(this.color));
        textRenderer.draw(text, x + 1, y - 1);
        textRenderer.setColor(this.color);
        textRenderer.draw(text, x, y);

        textRenderer.end3DRendering();
    }

    // Compute background color for best contrast

    protected Color getBackgroundColor(Color color)
    {
        float[] compArray = new float[4];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if (compArray[2] > 0.5)
            return new Color(0, 0, 0, (int) (this.color.getAlpha() * 0.7f));
        else
            return new Color(255, 255, 255, (int) (this.color.getAlpha() * 0.7f));
    }

    // ** Dimensions and positionning ************************************************************

    protected double computeScale(java.awt.Rectangle viewport)
    {
        if (this.resizeBehavior.equals(AVKey.RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.toViewportScale) * viewport.width / this.size.width);
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_STRETCH))
        {
            return (this.toViewportScale) * viewport.width / this.size.width;
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_KEEP_FIXED_SIZE))
        {
            return 1d;
        }
        else
        {
            return 1d;
        }
    }

    protected Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
    {
        double scaledWidth = scale * (isMinimized ? MINIMIZED_SIZE :
            isMaximized ? viewport.width - this.borderWidth * 2 : this.size.width);
        double scaledHeight = scale * (isMinimized ? MINIMIZED_SIZE :
            isMaximized ? viewport.height * 2 / 3 - this.borderWidth * 2 : this.size.height);

        double x;
        double y;

        if (this.locationCenter != null)
        {
            x = this.locationCenter.x - scaledWidth / 2;
            y = this.locationCenter.y - scaledHeight / 2;
        }
        else if (this.position.equals(AVKey.NORTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST))
        {
            x = 0d + this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST))
        {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else // use North East
        {
            x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
            y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Vec4(x, y, 0);
    }

    /**
     * Computes the Position of the pickPoint over the graph and updates pickedSample indice
     *
     * @param dc         the current DrawContext
     * @param locationSW the screen location of the bottom left corner of the graph
     * @param mapSize    the graph screen dimension in pixels
     *
     * @return the picked Position
     */
    protected Position computePickPosition(DrawContext dc, Vec4 locationSW, Dimension mapSize)
    {
        Position pickPosition = null;
        this.pickedSample = -1;
        Point pickPoint = dc.getPickPoint();
        if (pickPoint != null && this.positions != null && !this.follow.equals(FOLLOW_CURSOR))
        {
            Rectangle viewport = dc.getView().getViewport();
            // Check if pickpoint is inside the graph
            if (pickPoint.getX() >= locationSW.getX()
                && pickPoint.getX() < locationSW.getX() + mapSize.width
                && viewport.height - pickPoint.getY() >= locationSW.getY()
                && viewport.height - pickPoint.getY() < locationSW.getY() + mapSize.height)
            {
                // Find sample - Note: only works when graph expends over the full width
                int sample = (int) (((double) (pickPoint.getX() - locationSW.getX()) / mapSize.width) * this.samples);
                if (sample >= 0 && sample < this.samples)
                {
                    pickPosition = this.positions[sample];
                    this.pickedSample = sample;
                    // Update polyline indicator
                    ArrayList<Position> posList = new ArrayList<Position>();
                    posList.add(positions[sample]);
                    posList.add(new Position(positions[sample].getLatitude(), positions[sample].getLongitude(),
                        positions[sample].getElevation() + this.length / 10));
                    if (this.pickedShape == null)
                    {
                        this.pickedShape = new Polyline(posList);
                        this.pickedShape.setPathType(Polyline.LINEAR);
                        this.pickedShape.setLineWidth(2);
                        this.pickedShape.setColor(new Color(this.color.getRed(),
                            this.color.getGreen(), (int) (this.color.getBlue() * .8), (int) (255 * .8)));
                    }
                    else
                        this.pickedShape.setPositions(posList);
                }
            }
        }
        return pickPosition;
    }

    /**
     * Compute the sample number along the path closest to the given <code>LatLon</code>.
     *
     * @param pos the object <code>LatLon</code>
     *
     * @return the sample number or -1 if not found.
     */
    protected int computeObjectSample(LatLon pos)
    {
        if (this.pathPositions.size() < 2)
            return -1;

        double radius = this.wwd.getModel().getGlobe().getRadius();
        double maxDistanceFromPath = 1000; // meters
        double distanceFromStart = 0;
        int segmentIndex = 0;
        LatLon pos1 = this.pathPositions.get(segmentIndex);
        for (int i = 1; i < this.pathPositions.size(); i++)
        {
            LatLon pos2 = this.pathPositions.get(i);
            double segmentLength = LatLon.greatCircleDistance(pos1, pos2).radians * radius;
            // Check wether the position is close to the segment line
            Vec4 v0 = new Vec4(pos.getLatitude().radians, pos.getLongitude().radians, 0, 0);
            Vec4 v1 = new Vec4(pos1.getLatitude().radians, pos1.getLongitude().radians, 0, 0);
            Vec4 v2 = new Vec4(pos2.getLatitude().radians, pos2.getLongitude().radians, 0, 0);
            Line line = new Line(v1, v2.subtract3(v1));
            if (line.distanceTo(v0) * radius <= maxDistanceFromPath)
            {
                // Check whether the position is inside the segment
                double length1 = LatLon.greatCircleDistance(pos1, pos).radians * radius;
                double length2 = LatLon.greatCircleDistance(pos2, pos).radians * radius;
                if (length1 <= segmentLength && length2 <= segmentLength)
                {
                    // Compute portion of segment length
                    distanceFromStart += length1 / (length1 + length2) * segmentLength;
                    break;
                }
                else
                    distanceFromStart += segmentLength;
            }
            else
                distanceFromStart += segmentLength;
            // Next segment
            pos1 = pos2;
        }
        double pathLength = this.computePathLength();
        return distanceFromStart < pathLength ? (int) (distanceFromStart / pathLength * this.samples) : -1;
    }

    // ** Position listener impl. ************************************************************

    public void moved(PositionEvent event)
    {
        this.positions = null;
    }

    // ** Select listener impl. ************************************************************

    public void selected(SelectEvent event)
    {
        if (event.hasObjects() && event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
                return;

            Object o = event.getTopObject();
            if (o == this.buttonMinimize)
            {
                if (this.isMaximized)
                    this.setIsMaximized(false);
                else
                    this.setIsMinimized(true);
            }

            else if (o == this.buttonMaximize)
            {
                this.setIsMaximized(true);
            }

            else if (o == this && this.isMinimized)
            {
                this.setIsMinimized(false);
            }
        }
    }

    // ** Property change listener ***********************************************************

    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.positions = null;
    }

    // Sets the wwd local reference and add us to the position listeners
    // the view and elevation model property change listener

    public void setEventSource(WorldWindow wwd)
    {
        if (this.wwd != null)
        {
            this.wwd.removePositionListener(this);
            this.wwd.getView().removePropertyChangeListener(this);
//            this.wwd.getModel().getGlobe().getElevationModel().removePropertyChangeListener(this);
            this.wwd.removeSelectListener(this);
        }
        this.wwd = wwd;
        if (this.wwd != null)
        {
            this.wwd.addPositionListener(this);
            this.wwd.getView().addPropertyChangeListener(this);
//            this.wwd.getModel().getGlobe().getElevationModel().addPropertyChangeListener(this);
            this.wwd.addSelectListener(this);
        }
    }

    // ** Profile data collection ************************************************************

    /**
     * Compute the terrain profile. <p> If {@link #FOLLOW_VIEW}, {@link #FOLLOW_EYE}, {@link #FOLLOW_CURSOR} or {@link
     * #FOLLOW_OBJECT}, collects terrain profile data along a great circle line centered at the current position (view,
     * eye, cursor or object) and perpendicular to the view heading - or object heading if {@link #FOLLOW_OBJECT}. <p>
     * If {@link #FOLLOW_NONE} the profile is computed in between start and end latlon. <p> If {@link #FOLLOW_PATH} the
     * profile is computed along the path provided with {@link #setPathPositions(ArrayList)}</p>
     *
     * @param dc the current <code>DrawContext</code>
     */
    protected void computeProfile(DrawContext dc)
    {
        if (this.wwd == null)
            return;

        try
        {
            // Find center position
            View view = this.wwd.getView();
            Position groundPos = null;
            if (this.follow.equals(FOLLOW_VIEW))
                groundPos = this.computeViewCenterPosition(dc);
            else if (this.follow.equals(FOLLOW_CURSOR))
                groundPos = this.computeCursorPosition(dc);
            else if (this.follow.equals(FOLLOW_EYE))
                groundPos = view.getEyePosition();
            else if (this.follow.equals(FOLLOW_OBJECT))
                groundPos = this.objectPosition;
            // Compute profile if we can
            if ((this.follow.equals(FOLLOW_VIEW) && groundPos != null) ||
                (this.follow.equals(FOLLOW_EYE) && groundPos != null) ||
                (this.follow.equals(FOLLOW_CURSOR) && groundPos != null) ||
                (this.follow.equals(FOLLOW_NONE) && this.startLatLon != null && this.endLatLon != null) ||
                (this.follow.equals(FOLLOW_OBJECT) && this.objectPosition != null && this.objectHeading != null) ||
                (this.follow.equals(FOLLOW_PATH) && this.pathPositions != null && this.pathPositions.size() >= 2))
            {
                this.positions = new Position[samples];
                this.minElevation = Double.MAX_VALUE;
                this.maxElevation = -Double.MAX_VALUE;
                // Compute profile positions
                if (this.follow.equals(FOLLOW_PATH))
                {
                    computePathPositions();
                }
                else
                {
                    computeMirroredPositions(groundPos);
                }
                // Update shape on ground
                if (this.selectionShape == null)
                {
                    this.selectionShape = new Polyline(Arrays.asList(this.positions));
                    this.selectionShape.setLineWidth(2);
                    this.selectionShape.setFollowTerrain(true);
                    this.selectionShape.setColor(new Color(this.color.getRed(),
                        this.color.getGreen(), (int) (this.color.getBlue() * .5), (int) (255 * .8)));
                }
                else
                    this.selectionShape.setPositions(Arrays.asList(this.positions));
            }
            else
            {
                // Off globe or something missing
                this.positions = null;
            }
        }
        catch (Exception e)
        {
            this.positions = null;
        }
    }

    protected Position computeViewCenterPosition(DrawContext dc)
    {
        View view = dc.getView();
        Line ray = view.computeRayFromScreenPoint(view.getViewport().getWidth() / 2,
            view.getViewport().getHeight() / 2);
        Intersection[] inters = dc.getSurfaceGeometry().intersect(ray);
        if (inters.length > 0)
            return dc.getGlobe().computePositionFromPoint(inters[0].getIntersectionPoint());

        return null;
    }

    protected void computeMirroredPositions(LatLon centerLatLon)
    {
        View view = this.wwd.getView();
        // Compute profile length
        if (view instanceof OrbitView)
        {
            this.length = Math.min(((OrbitView) view).getZoom() * .8 * this.profileLengthFactor,
                this.wwd.getModel().getGlobe().getRadius() * Math.PI);
        }
        else
        {
            this.length = Math.min(Math.abs(view.getEyePosition().getElevation()) * .8 * this.profileLengthFactor,
                this.wwd.getModel().getGlobe().getRadius() * Math.PI);
        }
        if (this.follow.equals(FOLLOW_NONE))
        {
            this.length = LatLon.greatCircleDistance(this.startLatLon, this.endLatLon).radians
                * this.wwd.getModel().getGlobe().getRadius();
        }
        else if (this.follow.equals(FOLLOW_OBJECT))
        {
            this.length = Math.min(Math.abs(this.objectPosition.getElevation()) * .8 * this.profileLengthFactor,
                this.wwd.getModel().getGlobe().getRadius() * Math.PI);
        }
        double lengthRadian = this.length / this.wwd.getModel().getGlobe().getRadius();

        // Iterate on both sides of the center point
        int i;
        double step = lengthRadian / (samples - 1);
        for (i = 0; i < this.samples; i++)
        {
            LatLon latLon = null;
            if (!this.follow.equals(FOLLOW_NONE))
            {
                // Compute segments perpendicular to view or object heading
                double azimuth = view.getHeading().subtract(Angle.POS90).radians;
                if (this.follow.equals(FOLLOW_OBJECT))
                    azimuth = this.objectHeading.subtract(Angle.POS90).radians;
                if (i > (float) (this.samples - 1) / 2f)
                {
                    //azimuth = view.getHeading().subtract(Angle.NEG90).radians;
                    azimuth += Math.PI;
                }
                double distance = Math.abs(((double) i - ((double) (this.samples - 1) / 2d)) * step);
                latLon = LatLon.greatCircleEndPosition(centerLatLon, azimuth, distance);
            }
            else if (this.follow.equals(FOLLOW_NONE) && this.startLatLon != null
                && this.endLatLon != null)
            {
                // Compute segments between start and end positions latlon
                latLon = LatLon.interpolate((double) i / (this.samples - 1), this.startLatLon, this.endLatLon);
            }
            this.setPosition(i, latLon);
        }
    }

    protected void computePathPositions()
    {
        this.length = computePathLength();
        double lengthRadians = this.length / this.wwd.getModel().getGlobe().getRadius();
        double step = lengthRadians / (this.samples - 1);
        int segmentIndex = 0;
        LatLon latLon = this.pathPositions.get(segmentIndex);
        // Set first sample at path start
        this.setPosition(0, latLon);
        // Compute in between samples
        double distance, azimuth;
        for (int i = 1; i < this.samples - 1; i++)
        {
            double stepToGo = step;
            while (stepToGo > 0)
            {
                if (this.pathType == Polyline.RHUMB_LINE)
                    distance = LatLon.rhumbDistance(latLon, this.pathPositions.get(segmentIndex + 1)).radians;
                else
                    distance = LatLon.greatCircleDistance(latLon, this.pathPositions.get(segmentIndex + 1)).radians;
                if (distance >= stepToGo)
                {
                    if (this.pathType == Polyline.RHUMB_LINE)
                    {
                        azimuth = LatLon.rhumbAzimuth(latLon, this.pathPositions.get(segmentIndex + 1)).radians;
                        latLon = LatLon.rhumbEndPosition(latLon, azimuth, stepToGo);
                    }
                    else
                    {
                        azimuth = LatLon.greatCircleAzimuth(latLon, this.pathPositions.get(segmentIndex + 1)).radians;
                        latLon = LatLon.greatCircleEndPosition(latLon, azimuth, stepToGo);
                    }

                    this.setPosition(i, latLon);
                    stepToGo = 0;
                }
                else
                {
                    segmentIndex++;
                    latLon = this.pathPositions.get(segmentIndex);
                    stepToGo -= distance;
                }
            }
        }
        // Set last sample at path end
        this.setPosition(this.samples - 1, this.pathPositions.get(this.pathPositions.size() - 1));
    }

    protected double computePathLength()
    {
        double pathLengthRadians = 0;
        LatLon pos1 = null;
        for (LatLon pos2 : this.pathPositions)
        {
            if (pos1 != null)
                pathLengthRadians += LatLon.greatCircleDistance(pos1, pos2).radians;
            pos1 = pos2;
        }
        return pathLengthRadians * this.wwd.getModel().getGlobe().getRadius();
    }

    protected void setPosition(int index, LatLon latLon)
    {
        Double elevation =
            this.wwd.getModel().getGlobe().getElevation(latLon.getLatitude(), latLon.getLongitude());
        this.minElevation = elevation < this.minElevation ? elevation : this.minElevation;
        this.maxElevation = elevation > this.maxElevation ? elevation : this.maxElevation;
        // Add position to the list
        positions[index] = new Position(latLon, elevation);
    }

    protected Position computeCursorPosition(DrawContext dc)
    {
        Position pos = this.wwd.getCurrentPosition();
        if (pos == null && dc.getPickPoint() != null)
        {
            // Current pos is null, try intersection with terrain geometry
            Line ray = dc.getView().computeRayFromScreenPoint(dc.getPickPoint().x, dc.getPickPoint().y);
            Intersection[] inter = dc.getSurfaceGeometry().intersect(ray);
            if (inter != null && inter.length > 0)
                pos = dc.getGlobe().computePositionFromPoint(inter[0].getIntersectionPoint());
        }
        if (pos == null && dc.getPickPoint() != null)
        {
            // Position is still null, fallback on intersection with ellipsoid.
            pos = dc.getView().computePositionFromScreenPoint(dc.getPickPoint().x, dc.getPickPoint().y);
        }
        return pos;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.TerrainProfileLayer.Name");
    }
}
